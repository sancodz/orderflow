package com.sathiya.order_service.dto.external;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // Required for Jackson deserialization
public class InventoryDTO {
    private String sku;
    private Integer quantity;
    // Add other fields if needed from InventoryResponseDTO of Inventory Service
}