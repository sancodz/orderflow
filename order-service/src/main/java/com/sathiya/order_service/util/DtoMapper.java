package com.sathiya.order_service.util;

import com.sathiya.order_service.dto.response.OrderItemResponseDTO;
import com.sathiya.order_service.dto.response.OrderResponseDTO;
import com.sathiya.order_service.entity.Order;
import com.sathiya.order_service.entity.OrderItem;

import java.util.stream.Collectors;

public class DtoMapper {

    public static OrderItemResponseDTO toOrderItemResponseDTO(OrderItem item, String productName) {
        return OrderItemResponseDTO.builder()
                .id(item.getId())
                .productSku(item.getProductSku())
                .productName(productName) // Fetched externally
                .quantity(item.getQuantity())
                .priceAtPurchase(item.getPriceAtPurchase())
                .subtotal(item.getSubtotal())
                .build();
    }

    public static OrderResponseDTO toOrderResponseDTO(Order order, java.util.function.Function<String, String> productNameResolver) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .items(order.getOrderItems().stream()
                        .map(item -> toOrderItemResponseDTO(item, productNameResolver.apply(item.getProductSku())))
                        .collect(Collectors.toList()))
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    // Simpler version if product names are not resolved immediately
    public static OrderResponseDTO toOrderResponseDTOWithoutProductNames(Order order) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .items(order.getOrderItems().stream()
                        .map(item -> OrderItemResponseDTO.builder()
                                .id(item.getId())
                                .productSku(item.getProductSku())
                                .productName("N/A - Fetch Separately") // Placeholder
                                .quantity(item.getQuantity())
                                .priceAtPurchase(item.getPriceAtPurchase())
                                .subtotal(item.getSubtotal())
                                .build())
                        .collect(Collectors.toList()))
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}