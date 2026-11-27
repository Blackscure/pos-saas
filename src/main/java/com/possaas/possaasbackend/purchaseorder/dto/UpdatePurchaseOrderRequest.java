package com.possaas.possaasbackend.purchaseorder.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UpdatePurchaseOrderRequest(

        @NotNull(message = "Branch ID is required")
        UUID branchId,

        @NotNull(message = "Supplier ID is required")
        UUID supplierId,

        LocalDate expectedDeliveryDate,

        String notes,

        @NotEmpty(message = "Purchase order must contain at least one item")
        List<@Valid PurchaseOrderItemRequest> items

) {
}
