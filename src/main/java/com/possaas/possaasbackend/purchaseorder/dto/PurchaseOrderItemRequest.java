package com.possaas.possaasbackend.purchaseorder.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderItemRequest(

        @NotNull(message = "Product ID is required")
        UUID productId,

        UUID productVariantId,

        @NotNull(message = "Quantity ordered is required")
        @Positive(message = "Quantity ordered must be greater than zero")
        Integer quantityOrdered,

        @NotNull(message = "Unit cost is required")
        @DecimalMin(value = "0.00", message = "Unit cost cannot be negative")
        BigDecimal unitCost,

        @DecimalMin(value = "0.00", message = "Tax rate cannot be negative")
        BigDecimal taxRate

) {
}
