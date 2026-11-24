package com.possaas.possaasbackend.product.dto;

import com.possaas.possaasbackend.business.dto.BusinessResponse;
import com.possaas.possaasbackend.category.dto.CategoryResponse;
import com.possaas.possaasbackend.product.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(

        UUID id,

        BusinessResponse business,
        CategoryResponse category,

        String sku,
        String name,
        String description,
        String barcode,
        String imageUrl,

        BigDecimal purchasePrice,
        BigDecimal sellingPrice,
        BigDecimal taxRate,
        boolean taxInclusive,

        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {

    public static ProductResponse fromEntity(Product product) {

        return new ProductResponse(
                product.getId(),

                BusinessResponse.fromEntity(product.getBusiness()),

                product.getCategory() != null
                        ? CategoryResponse.fromEntity(product.getCategory())
                        : null,

                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getBarcode(),
                product.getImageUrl(),

                product.getPurchasePrice(),
                product.getSellingPrice(),
                product.getTaxRate(),
                product.isTaxInclusive(),

                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}