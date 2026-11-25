package com.possaas.possaasbackend.stockmovement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record StockMovementRequest(

        @NotNull(message = "Inventory ID is required")
        UUID inventoryId,

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.0001", message = "Quantity must be greater than zero")
        BigDecimal quantity,

        @NotBlank(message = "Reason is required")
        @Size(max = 1000, message = "Reason cannot exceed 1000 characters")
        String reason

) {
}