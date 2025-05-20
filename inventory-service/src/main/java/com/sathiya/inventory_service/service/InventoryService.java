package com.sathiya.inventory_service.service;

import com.sathiya.inventory_service.dto.InventoryRequestDTO;
import com.sathiya.inventory_service.dto.InventoryResponseDTO;
import com.sathiya.inventory_service.exception.DuplicateInventoryItemException;
import com.sathiya.inventory_service.exception.InsufficientStockException;
import com.sathiya.inventory_service.exception.InventoryItemNotFoundException;
import com.sathiya.inventory_service.entity.InventoryItem;
import com.sathiya.inventory_service.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;

    @Transactional
    public InventoryResponseDTO createInventoryItem(InventoryRequestDTO requestDTO) {
        log.info("Attempting to create inventory item for SKU: {}", requestDTO.getSku());
        if (inventoryItemRepository.existsBySku(requestDTO.getSku())) {
            log.warn("Creation failed: Inventory item for SKU {} already exists.", requestDTO.getSku());
            throw new DuplicateInventoryItemException("Inventory item for SKU " + requestDTO.getSku() + " already exists.");
        }

        InventoryItem inventoryItem = InventoryItem.builder()
                .sku(requestDTO.getSku())
                .quantity(requestDTO.getQuantity())
                .build();
        InventoryItem savedItem = inventoryItemRepository.save(inventoryItem);
        log.info("Inventory item created successfully for SKU: {} with ID: {}", savedItem.getSku(), savedItem.getId());
        return mapToResponseDTO(savedItem);
    }

    @Transactional(readOnly = true)
    public InventoryResponseDTO getInventoryBySku(String sku) {
        log.info("Fetching inventory item for SKU: {}", sku);
        InventoryItem item = inventoryItemRepository.findBySku(sku)
                .orElseThrow(() -> {
                    log.warn("Inventory item not found for SKU: {}", sku);
                    return new InventoryItemNotFoundException("Inventory item not found for SKU: " + sku);
                });
        return mapToResponseDTO(item);
    }

    @Transactional(readOnly = true)
    public InventoryResponseDTO getInventoryById(Long id) {
        log.info("Fetching inventory item by ID: {}", id);
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Inventory item not found for ID: {}", id);
                    return new InventoryItemNotFoundException("Inventory item not found for ID: " + id);
                });
        return mapToResponseDTO(item);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponseDTO> getAllInventoryItems() {
        log.info("Fetching all inventory items.");
        return inventoryItemRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventoryResponseDTO updateInventoryQuantity(String sku, Integer newQuantity) {
        log.info("Attempting to update quantity for SKU: {} to new quantity: {}", sku, newQuantity);
        if (newQuantity < 0) {
            log.warn("Update failed: New quantity {} for SKU {} cannot be negative.", newQuantity, sku);
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        InventoryItem item = inventoryItemRepository.findBySku(sku)
                .orElseThrow(() -> {
                    log.warn("Update failed: Inventory item not found for SKU: {}", sku);
                    return new InventoryItemNotFoundException("Inventory item not found for SKU: " + sku + " to update quantity.");
                });
        item.setQuantity(newQuantity);
        InventoryItem updatedItem = inventoryItemRepository.save(item);
        log.info("Quantity updated successfully for SKU: {} to {}", sku, newQuantity);
        return mapToResponseDTO(updatedItem);
    }

    @Transactional
    public InventoryResponseDTO incrementStock(String sku, int amountToIncrement) {
        log.info("Attempting to increment stock for SKU: {} by amount: {}", sku, amountToIncrement);
        if (amountToIncrement <= 0) {
            log.warn("Increment failed: Amount {} for SKU {} must be positive.", amountToIncrement, sku);
            throw new IllegalArgumentException("Amount to increment must be positive.");
        }
        InventoryItem item = inventoryItemRepository.findBySku(sku)
                .orElseThrow(() -> {
                    log.warn("Increment failed: Inventory item not found for SKU: {}", sku);
                    return new InventoryItemNotFoundException("Inventory item not found for SKU: " + sku + " to increment stock.");
                });
        item.setQuantity(item.getQuantity() + amountToIncrement);
        InventoryItem updatedItem = inventoryItemRepository.save(item);
        log.info("Stock incremented successfully for SKU: {} by {}. New quantity: {}", sku, amountToIncrement, updatedItem.getQuantity());
        return mapToResponseDTO(updatedItem);
    }

    @Transactional
    public InventoryResponseDTO decrementStock(String sku, int amountToDecrement) {
        log.info("Attempting to decrement stock for SKU: {} by amount: {}", sku, amountToDecrement);
        if (amountToDecrement <= 0) {
            log.warn("Decrement failed: Amount {} for SKU {} must be positive.", amountToDecrement, sku);
            throw new IllegalArgumentException("Amount to decrement must be positive.");
        }
        InventoryItem item = inventoryItemRepository.findBySku(sku)
                .orElseThrow(() -> {
                    log.warn("Decrement failed: Inventory item not found for SKU: {}", sku);
                    return new InventoryItemNotFoundException("Inventory item not found for SKU: " + sku + " to decrement stock.");
                });

        if (item.getQuantity() < amountToDecrement) {
            log.warn("Decrement failed: Insufficient stock for SKU {}. Current: {}, Requested: {}", sku, item.getQuantity(), amountToDecrement);
            throw new InsufficientStockException("Insufficient stock for SKU " + sku +
                    ". Available: " + item.getQuantity() + ", Requested: " + amountToDecrement);
        }
        item.setQuantity(item.getQuantity() - amountToDecrement);
        InventoryItem updatedItem = inventoryItemRepository.save(item);
        log.info("Stock decremented successfully for SKU: {} by {}. New quantity: {}", sku, amountToDecrement, updatedItem.getQuantity());
        return mapToResponseDTO(updatedItem);
    }

    @Transactional
    public void deleteInventoryItem(String sku) {
        log.info("Attempting to delete inventory item for SKU: {}", sku);
        InventoryItem item = inventoryItemRepository.findBySku(sku)
                .orElseThrow(() -> {
                    log.warn("Deletion failed: Inventory item not found for SKU: {}", sku);
                    return new InventoryItemNotFoundException("Inventory item not found for SKU: " + sku + " to delete.");
                });
        inventoryItemRepository.delete(item);
        log.info("Inventory item deleted successfully for SKU: {}", sku);
    }


    private InventoryResponseDTO mapToResponseDTO(InventoryItem item) {
        return InventoryResponseDTO.builder()
                .id(item.getId())
                .sku(item.getSku())
                .quantity(item.getQuantity())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}