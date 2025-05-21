package com.sathiya.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponseDTO {
    private Long id;
    private String productSku;
    private String productName; // To be fetched from Product Service
    private Integer quantity;
    private BigDecimal priceAtPurchase;
    private BigDecimal subtotal;
}