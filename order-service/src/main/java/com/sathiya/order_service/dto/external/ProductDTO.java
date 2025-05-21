package com.sathiya.order_service.dto.external;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor // Required for Jackson deserialization
public class ProductDTO {
    private Long id;
    private String sku;
    private String name;
    private BigDecimal price;
    // Add other fields if needed from ProductResponseDTO of Product Service
}