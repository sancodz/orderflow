package com.sathiya.order_service.service;

import com.sathiya.order_service.dto.external.InventoryDTO;
import com.sathiya.order_service.dto.external.ProductDTO;
import com.sathiya.order_service.dto.request.OrderItemRequestDTO;
import com.sathiya.order_service.dto.request.OrderRequestDTO;
import com.sathiya.order_service.dto.response.OrderResponseDTO;
import com.sathiya.order_service.exception.ExternalServiceCommunicationException;
import com.sathiya.order_service.exception.InsufficientStockForOrderException;
import com.sathiya.order_service.exception.OrderNotFoundException;
import com.sathiya.order_service.exception.ProductNotFoundForOrderException;
import com.sathiya.order_service.entity.Order;
import com.sathiya.order_service.entity.OrderItem;
import com.sathiya.order_service.entity.OrderStatus;
import com.sathiya.order_service.repository.OrderRepository;
import com.sathiya.order_service.service.client.InventoryServiceClient;
import com.sathiya.order_service.service.client.ProductServiceClient;
import com.sathiya.order_service.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final InventoryServiceClient inventoryServiceClient;
    // private final UserServiceClient userServiceClient; // For future user validation

    @Transactional // This method involves multiple operations, make it transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) {
        log.info("Attempting to create order for user ID: {}", orderRequestDTO.getUserId());

        // (Optional Phase 1 enhancement: Validate user exists by calling User Service)
        // userServiceClient.validateUser(orderRequestDTO.getUserId());

        Order order = Order.builder()
                .userId(orderRequestDTO.getUserId())
                .status(OrderStatus.PENDING) // Initial status
                .orderItems(new ArrayList<>())
                .build();

        BigDecimal totalOrderAmount = BigDecimal.ZERO;
        Map<String, ProductDTO> productCache = new HashMap<>(); // Cache to avoid multiple calls for same product

        // --- Phase 1: Process Order Items (Fetch Product Info & Check/Decrement Stock) ---
        for (OrderItemRequestDTO itemRequest : orderRequestDTO.getItems()) {
            log.info("Processing item SKU: {}, Quantity: {}", itemRequest.getSku(), itemRequest.getQuantity());

            // 1. Get Product Details (Price, Name)
            ProductDTO product = productCache.computeIfAbsent(itemRequest.getSku(),
                    sku -> productServiceClient.getProductBySku(sku));
            if (product == null) { // Should be handled by ProductNotFoundForOrderException in client
                log.error("Product details not found for SKU: {}", itemRequest.getSku());
                throw new ProductNotFoundForOrderException("Product with SKU " + itemRequest.getSku() + " not found during order creation.");
            }

            // 2. Check Stock Availability (before trying to decrement)
            InventoryDTO inventory = inventoryServiceClient.getInventoryBySku(itemRequest.getSku());
            if (inventory.getQuantity() < itemRequest.getQuantity()) {
                log.warn("Insufficient stock for SKU: {}. Available: {}, Requested: {}",
                        itemRequest.getSku(), inventory.getQuantity(), itemRequest.getQuantity());
                throw new InsufficientStockForOrderException("Insufficient stock for product SKU: " + itemRequest.getSku() +
                        ". Available: " + inventory.getQuantity() + ", Requested: " + itemRequest.getQuantity());
            }

            // 3. Create OrderItem
            BigDecimal itemSubtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            OrderItem orderItem = OrderItem.builder()
                    .productSku(itemRequest.getSku())
                    .quantity(itemRequest.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .subtotal(itemSubtotal)
                    .build();
            order.addOrderItem(orderItem); // Adds to list and sets bidirectional link
            totalOrderAmount = totalOrderAmount.add(itemSubtotal);
        }

        order.setTotalAmount(totalOrderAmount);

        // --- Phase 2: Persist Order (after all checks pass) ---
        // Only save if all items are processable. If an exception occurred above, transaction will roll back.
        Order savedOrder = orderRepository.save(order);
        log.info("Order entity saved with ID: {}", savedOrder.getId());


        // --- Phase 3: Decrement Stock (after successful order save) ---
        // This is a critical step. If this fails, we have a saved order but stock not decremented.
        // More advanced patterns (Saga) handle this better, but for Phase 1:
        try {
            for (OrderItemRequestDTO itemRequest : orderRequestDTO.getItems()) {
                inventoryServiceClient.decrementStock(itemRequest.getSku(), itemRequest.getQuantity());
            }
            // If stock decrement is successful for all items, we can update order status (optional)
            savedOrder.setStatus(OrderStatus.PROCESSING); // Or AWAITING_PAYMENT if payment is next
            orderRepository.save(savedOrder); // Save updated status
            log.info("Stock decremented successfully for all items in order ID: {}", savedOrder.getId());

        } catch (Exception e) {
            // This is a compensation problem area for Phase 1.
            // If decrementing stock fails after order is saved, the order might be in PENDING
            // but needs to be marked as FAILED or handled manually.
            log.error("CRITICAL: Failed to decrement stock for order ID: {}. Order is saved but stock not updated. Error: {}",
                    savedOrder.getId(), e.getMessage(), e);
            // For Phase 1, we might just mark the order as FAILED.
            savedOrder.setStatus(OrderStatus.FAILED);
            // Potentially add a note about the failure reason to the order if your model supports it.
            orderRepository.save(savedOrder);
            // Re-throw the exception so the client knows something went wrong.
            // Or throw a specific "OrderProcessingFailedException"
            throw new ExternalServiceCommunicationException("Order created (ID: " + savedOrder.getId() + ") but failed to update inventory: " + e.getMessage(), e);
        }


        log.info("Order created successfully with ID: {}. Total Amount: {}", savedOrder.getId(), savedOrder.getTotalAmount());
        // Resolve product names for the response
        return DtoMapper.toOrderResponseDTO(savedOrder, sku -> productCache.getOrDefault(sku, new ProductDTO()).getName());
    }


    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long orderId) {
        log.info("Fetching order by ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found with ID: {}", orderId);
                    return new OrderNotFoundException("Order not found with ID: " + orderId);
                });

        // For displaying order, we need product names. Fetch them if not already stored.
        // This simple implementation fetches product names on demand for the GET request.
        Map<String, String> productNames = new HashMap<>();
        for (OrderItem item : order.getOrderItems()) {
            if (!productNames.containsKey(item.getProductSku())) {
                try {
                    ProductDTO product = productServiceClient.getProductBySku(item.getProductSku());
                    productNames.put(item.getProductSku(), product.getName());
                } catch (ProductNotFoundForOrderException | ExternalServiceCommunicationException e) {
                    log.warn("Could not fetch product name for SKU {} for order {}: {}", item.getProductSku(), orderId, e.getMessage());
                    productNames.put(item.getProductSku(), "N/A - Product Info Unavailable");
                }
            }
        }
        return DtoMapper.toOrderResponseDTO(order, productNames::get);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByUserId(Long userId) {
        log.info("Fetching orders for user ID: {}", userId);
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (orders.isEmpty()) {
            log.info("No orders found for user ID: {}", userId);
            return new ArrayList<>();
        }
        // Similar to getOrderById, resolve product names for each order's items.
        // This can be N+1 if not careful or if product details are not cached.
        // For simplicity here, we'll use the DtoMapper that marks names as "N/A" if not resolved.
        // A better approach would be to batch fetch product details for all SKUs across all orders.
        return orders.stream()
                .map(DtoMapper::toOrderResponseDTOWithoutProductNames) // Simpler mapping for list view
                .collect(Collectors.toList());
    }
}