package com.sathiya.inventory_service.controller;

import com.sathiya.inventory_service.dto.InventoryRequestDTO;
import com.sathiya.inventory_service.dto.InventoryResponseDTO;
import com.sathiya.inventory_service.dto.StockAdjustmentDTO;
import com.sathiya.inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryResponseDTO> createInventoryItem(@Valid @RequestBody InventoryRequestDTO requestDTO) {
        log.info("Received request to create inventory item for SKU: {}", requestDTO.getSku());
        InventoryResponseDTO createdItem = inventoryService.createInventoryItem(requestDTO);
        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<InventoryResponseDTO> getInventoryBySku(@PathVariable(name = "sku") String sku) {
        log.info("Received request to get inventory for SKU: {}", sku);
        InventoryResponseDTO item = inventoryService.getInventoryBySku(sku);
        return ResponseEntity.ok(item);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponseDTO> getInventoryById(@PathVariable(name = "id") Long id) {
        log.info("Received request to get inventory by ID: {}", id);
        InventoryResponseDTO item = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponseDTO>> getAllInventoryItems() {
        log.info("Received request to get all inventory items.");
        List<InventoryResponseDTO> items = inventoryService.getAllInventoryItems();
        return ResponseEntity.ok(items);
    }

    // Endpoint to directly set the quantity for an SKU
    @PutMapping("/sku/{sku}")
    public ResponseEntity<InventoryResponseDTO> updateInventoryQuantity(@PathVariable(name = "sku") String sku,
                                                                        @Valid @RequestBody InventoryRequestDTO requestDTO) {
        // Ensure the SKU in path matches SKU in body, or only use quantity from body
        if (!sku.equals(requestDTO.getSku())) {
            log.warn("SKU in path ({}) does not match SKU in body ({}). Using SKU from path.", sku, requestDTO.getSku());
            // Or throw bad request, depends on desired behavior
        }
        log.info("Received request to update quantity for SKU: {} to {}", sku, requestDTO.getQuantity());
        InventoryResponseDTO updatedItem = inventoryService.updateInventoryQuantity(sku, requestDTO.getQuantity());
        return ResponseEntity.ok(updatedItem);
    }


    @PatchMapping("/sku/{sku}/increment")
    public ResponseEntity<InventoryResponseDTO> incrementStock(@PathVariable(name = "sku") String sku,
                                                               @Valid @RequestBody StockAdjustmentDTO adjustmentDTO) {
        log.info("Received request to increment stock for SKU: {} by {}", sku, adjustmentDTO.getAmount());
        InventoryResponseDTO updatedItem = inventoryService.incrementStock(sku, adjustmentDTO.getAmount());
        return ResponseEntity.ok(updatedItem);
    }

    @PatchMapping("/sku/{sku}/decrement")
    public ResponseEntity<InventoryResponseDTO> decrementStock(@PathVariable(name = "sku") String sku,
                                                               @Valid @RequestBody StockAdjustmentDTO adjustmentDTO) {
        log.info("Received request to decrement stock for SKU: {} by {}", sku, adjustmentDTO.getAmount());
        InventoryResponseDTO updatedItem = inventoryService.decrementStock(sku, adjustmentDTO.getAmount());
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/sku/{sku}")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable(name = "sku") String sku) {
        log.info("Received request to delete inventory item for SKU: {}", sku);
        inventoryService.deleteInventoryItem(sku);
        return ResponseEntity.noContent().build();
    }
}