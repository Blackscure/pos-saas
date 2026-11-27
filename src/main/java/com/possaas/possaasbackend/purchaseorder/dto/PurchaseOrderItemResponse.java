package com.possaas.possaasbackend.purchaseorder.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderItemResponse(

        UUID id,
        UUID productId,
        UUID productVariantId,
        Integer quantityOrdered,
        Integer quantityReceived,
        BigDecimal unitCost,
        BigDecimal taxRate,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount

) {
}
