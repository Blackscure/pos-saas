package com.possaas.possaasbackend.stockmovement;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "business_id", nullable = false, updatable = false)
    private UUID businessId;

    @Column(name = "branch_id", nullable = false, updatable = false)
    private UUID branchId;

    @Column(name = "inventory_id", nullable = false, updatable = false)
    private UUID inventoryId;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "product_variant_id", updatable = false)
    private UUID productVariantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, updatable = false, length = 30)
    private StockMovementType movementType;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal quantity;

    @Column(name = "quantity_before", nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal quantityBefore;

    @Column(name = "quantity_after", nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal quantityAfter;

    @Column(name = "reference_type", length = 50, updatable = false)
    private String referenceType;

    @Column(name = "reference_id", updatable = false)
    private UUID referenceId;

    @Column(name = "reason", updatable = false)
    private String reason;

    @Column(name = "performed_by", updatable = false)
    private UUID performedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected StockMovement() {
    }

    public StockMovement(
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
            UUID performedBy
    ) {
        this.tenantId = tenantId;
        this.businessId = businessId;
        this.branchId = branchId;
        this.inventoryId = inventoryId;
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.quantityBefore = quantityBefore;
        this.quantityAfter = quantityAfter;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.reason = reason;
        this.performedBy = performedBy;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
