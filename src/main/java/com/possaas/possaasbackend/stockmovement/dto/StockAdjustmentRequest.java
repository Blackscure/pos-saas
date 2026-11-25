package com.possaas.possaasbackend.stockmovement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record StockAdjustmentRequest(

        @NotNull(message = "Inventory ID is required")
        UUID inventoryId,

        @NotNull(message = "Actual quantity is required")
        @DecimalMin(value = "0.0000", message = "Actual quantity cannot be negative")
        BigDecimal quantity,

        @NotBlank(message = "Reason is required")
        @Size(max = 1000, message = "Reason cannot exceed 1000 characters")
        String reason

) {
}