package com.sathiya.order_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid // This enables validation of OrderItemRequestDTO objects within the list
    private List<OrderItemRequestDTO> items;
}