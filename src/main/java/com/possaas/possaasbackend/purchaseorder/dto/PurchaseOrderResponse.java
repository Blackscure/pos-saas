package com.possaas.possaasbackend.purchaseorder.dto;

import com.possaas.possaasbackend.purchaseorder.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderResponse(

        UUID id,
        UUID businessId,
        UUID branchId,
        UUID supplierId,
        String poNumber,
        PurchaseOrderStatus status,
        LocalDate orderDate,
        LocalDate expectedDeliveryDate,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String notes,
        UUID createdBy,
        UUID approvedBy,
        LocalDateTime approvedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<PurchaseOrderItemResponse> items

) {
}
