package com.possaas.possaasbackend.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateInventoryRequest(

        @NotNull(message = "Reorder level is required")
        @DecimalMin(value = "0.0", message = "Reorder level cannot be negative")
        BigDecimal reorderLevel

) {
}