package com.possaas.possaasbackend.inventory.dto;

import com.possaas.possaasbackend.inventory.Inventory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryResponse(

        UUID id,
        UUID businessId,
        UUID branchId,
        UUID productId,
        UUID productVariantId,

        BigDecimal quantityOnHand,
        BigDecimal quantityReserved,
        BigDecimal quantityAvailable,
        BigDecimal reorderLevel,

        boolean lowStock,

        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {

    public static InventoryResponse from(Inventory inventory) {

        BigDecimal quantityAvailable = inventory.getQuantityOnHand()
                .subtract(inventory.getQuantityReserved());

        boolean lowStock = quantityAvailable.compareTo(inventory.getReorderLevel()) <= 0;

        return new InventoryResponse(
                inventory.getId(),
                inventory.getBusinessId(),
                inventory.getBranchId(),
                inventory.getProductId(),
                inventory.getProductVariantId(),
                inventory.getQuantityOnHand(),
                inventory.getQuantityReserved(),
                quantityAvailable,
                inventory.getReorderLevel(),
                lowStock,
                inventory.getCreatedAt(),
                inventory.getUpdatedAt()
        );
    }
}