package com.possaas.possaasbackend.stockmovement.dto;

import com.possaas.possaasbackend.branch.Branch;
import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.dto.BusinessResponse;
import com.possaas.possaasbackend.inventory.Inventory;
import com.possaas.possaasbackend.inventory.dto.InventoryResponse;
import com.possaas.possaasbackend.product.Product;
import com.possaas.possaasbackend.productvariant.ProductVariant;
import com.possaas.possaasbackend.stockmovement.StockMovement;
import com.possaas.possaasbackend.stockmovement.StockMovementType;
import com.possaas.possaasbackend.tenant.Tenant;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockMovementResponse(

        UUID id,

        BusinessResponse.TenantSummary tenant,

        InventoryResponse.BusinessSummary business,

        InventoryResponse.BranchSummary branch,

        InventorySummary inventory,

        InventoryResponse.ProductSummary product,

        InventoryResponse.ProductVariantSummary productVariant,

        StockMovementType movementType,

        BigDecimal quantity,

        BigDecimal quantityBefore,

        BigDecimal quantityAfter,

        String referenceType,

        UUID referenceId,

        String reason,

        UUID performedBy,

        LocalDateTime createdAt

) {

    public static StockMovementResponse from(
            StockMovement movement,
            Tenant tenant,
            Business business,
            Branch branch,
            Inventory inventory,
            Product product,
            ProductVariant productVariant
    ) {

        return new StockMovementResponse(
                movement.getId(),
                BusinessResponse.TenantSummary.fromEntity(tenant),
                InventoryResponse.BusinessSummary.from(business),
                InventoryResponse.BranchSummary.from(branch),
                InventorySummary.from(inventory),
                InventoryResponse.ProductSummary.from(product),
                InventoryResponse.ProductVariantSummary.from(productVariant),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getQuantityBefore(),
                movement.getQuantityAfter(),
                movement.getReferenceType(),
                movement.getReferenceId(),
                movement.getReason(),
                movement.getPerformedBy(),
                movement.getCreatedAt()
        );
    }

    // =========================================================
    // INVENTORY SUMMARY
    // =========================================================

    public record InventorySummary(
            UUID id,
            BigDecimal quantityOnHand,
            BigDecimal quantityReserved,
            BigDecimal quantityAvailable,
            BigDecimal reorderLevel,
            boolean lowStock
    ) {

        public static InventorySummary from(Inventory inventory) {

            if (inventory == null) {
                return null;
            }

            BigDecimal quantityOnHand = inventory.getQuantityOnHand() != null
                    ? inventory.getQuantityOnHand()
                    : BigDecimal.ZERO;

            BigDecimal quantityReserved = inventory.getQuantityReserved() != null
                    ? inventory.getQuantityReserved()
                    : BigDecimal.ZERO;

            BigDecimal reorderLevel = inventory.getReorderLevel() != null
                    ? inventory.getReorderLevel()
                    : BigDecimal.ZERO;

            BigDecimal quantityAvailable = quantityOnHand.subtract(quantityReserved);

            boolean lowStock = quantityAvailable.compareTo(reorderLevel) <= 0;

            return new InventorySummary(
                    inventory.getId(),
                    quantityOnHand,
                    quantityReserved,
                    quantityAvailable,
                    reorderLevel,
                    lowStock
            );
        }
    }
}