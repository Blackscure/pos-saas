package com.possaas.possaasbackend.productvariant.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductVariantResponse(

        UUID id,

        UUID businessId,

        UUID productId,

        String sku,

        String barcode,

        String name,

        BigDecimal purchasePrice,

        BigDecimal sellingPrice,

        BigDecimal effectivePurchasePrice,

        BigDecimal effectiveSellingPrice,

        Boolean active,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
