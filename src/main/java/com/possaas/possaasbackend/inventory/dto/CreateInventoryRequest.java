package com.possaas.possaasbackend.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateInventoryRequest(

        @NotNull(message = "Product ID is required")
        UUID productId,

        UUID productVariantId,

        @NotNull(message = "Reorder level is required")
        @DecimalMin(value = "0.0", message = "Reorder level cannot be negative")
        BigDecimal reorderLevel

) {
}