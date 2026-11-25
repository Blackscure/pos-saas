package com.possaas.possaasbackend.stockmovement.dto;

import com.possaas.possaasbackend.stockmovement.StockMovement;
import com.possaas.possaasbackend.stockmovement.StockMovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockMovementResponse(

        UUID id,

        UUID tenantId,

        UUID businessId,

        UUID branchId,

        UUID inventoryId,

        UUID productId,

        UUID productVariantId,

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

    public static StockMovementResponse from(StockMovement movement) {

        return new StockMovementResponse(
                movement.getId(),
                movement.getTenantId(),
                movement.getBusinessId(),
                movement.getBranchId(),
                movement.getInventoryId(),
                movement.getProductId(),
                movement.getProductVariantId(),
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
}