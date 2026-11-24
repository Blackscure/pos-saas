package com.possaas.possaasbackend.inventory.dto;

import com.possaas.possaasbackend.branch.Branch;
import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.inventory.Inventory;
import com.possaas.possaasbackend.product.Product;
import com.possaas.possaasbackend.productvariant.ProductVariant;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryResponse(

        UUID id,

        BusinessSummary business,
        BranchSummary branch,
        ProductSummary product,
        ProductVariantSummary productVariant,

        BigDecimal quantityOnHand,
        BigDecimal quantityReserved,
        BigDecimal quantityAvailable,
        BigDecimal reorderLevel,

        boolean lowStock,

        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {

    public static InventoryResponse from(
            Inventory inventory,
            Business business,
            Branch branch,
            Product product,
            ProductVariant productVariant
    ) {

        BigDecimal quantityAvailable = inventory.getQuantityOnHand()
                .subtract(inventory.getQuantityReserved());

        boolean lowStock = quantityAvailable.compareTo(
                inventory.getReorderLevel()
        ) <= 0;

        return new InventoryResponse(
                inventory.getId(),
                BusinessSummary.from(business),
                BranchSummary.from(branch),
                ProductSummary.from(product),
                ProductVariantSummary.from(productVariant),
                inventory.getQuantityOnHand(),
                inventory.getQuantityReserved(),
                quantityAvailable,
                inventory.getReorderLevel(),

                lowStock,

                inventory.getCreatedAt(),
                inventory.getUpdatedAt()
        );
    }

    // =========================================================
    // BUSINESS SUMMARY
    // =========================================================

    public record BusinessSummary(
            UUID id,
            String name,
            String businessType,
            boolean active
    ) {

        public static BusinessSummary from(Business business) {

            if (business == null) {
                return null;
            }

            return new BusinessSummary(
                    business.getId(),
                    business.getName(),
                    business.getBusinessType() != null
                            ? business.getBusinessType().name()
                            : null,
                    business.isActive()
            );
        }
    }

    // =========================================================
    // BRANCH SUMMARY
    // =========================================================

    public record BranchSummary(
            UUID id,
            String name,
            String code,
            String phone,
            String address,
            boolean active
    ) {

        public static BranchSummary from(Branch branch) {

            if (branch == null) {
                return null;
            }

            return new BranchSummary(
                    branch.getId(),
                    branch.getName(),
                    branch.getCode(),
                    branch.getPhone(),
                    branch.getAddress(),
                    branch.isActive()
            );
        }
    }

    // =========================================================
    // PRODUCT SUMMARY
    // =========================================================

    public record ProductSummary(
            UUID id,
            String name,
            String sku,
            String barcode,
            BigDecimal sellingPrice,
            boolean active
    ) {

        public static ProductSummary from(Product product) {

            if (product == null) {
                return null;
            }

            return new ProductSummary(
                    product.getId(),
                    product.getName(),
                    product.getSku(),
                    product.getBarcode(),
                    product.getSellingPrice(),
                    product.isActive()
            );
        }
    }

    // =========================================================
    // PRODUCT VARIANT SUMMARY
    // =========================================================

    public record ProductVariantSummary(
            UUID id,
            String name,
            String sku,
            String barcode,
            BigDecimal sellingPrice,
            Boolean active
    ) {

        public static ProductVariantSummary from(ProductVariant variant) {

            if (variant == null) {
                return null;
            }

            return new ProductVariantSummary(
                    variant.getId(),
                    variant.getName(),
                    variant.getSku(),
                    variant.getBarcode(),
                    variant.getSellingPrice(),
                    variant.getActive()
            );
        }
    }
}
