package com.sathiya.inventory_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentDTO {
    @NotNull(message = "Amount to adjust cannot be null")
    @Min(value = 1, message = "Amount to adjust must be at least 1")
    private Integer amount;
}