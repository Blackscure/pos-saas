package com.possaas.possaasbackend.stockmovement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    // All stock movements for a business
    Page<StockMovement> findByTenantIdAndBusinessIdOrderByCreatedAtDesc(
            UUID tenantId,
            UUID businessId,
            Pageable pageable
    );

    // Stock movements for a specific branch
    Page<StockMovement> findByTenantIdAndBusinessIdAndBranchIdOrderByCreatedAtDesc(
            UUID tenantId,
            UUID businessId,
            UUID branchId,
            Pageable pageable
    );

    // Stock movement history for a specific inventory record
    Page<StockMovement> findByTenantIdAndBusinessIdAndInventoryIdOrderByCreatedAtDesc(
            UUID tenantId,
            UUID businessId,
            UUID inventoryId,
            Pageable pageable
    );

    // Stock movement history for a specific product
    Page<StockMovement> findByTenantIdAndBusinessIdAndProductIdOrderByCreatedAtDesc(
            UUID tenantId,
            UUID businessId,
            UUID productId,
            Pageable pageable
    );

    // Stock movement history for a specific product variant
    Page<StockMovement> findByTenantIdAndBusinessIdAndProductVariantIdOrderByCreatedAtDesc(
            UUID tenantId,
            UUID businessId,
            UUID productVariantId,
            Pageable pageable
    );

    // Stock movement history filtered by movement type
    Page<StockMovement> findByTenantIdAndBusinessIdAndMovementTypeOrderByCreatedAtDesc(
            UUID tenantId,
            UUID businessId,
            StockMovementType movementType,
            Pageable pageable
    );

    // Stock movement history for a branch filtered by movement type
    Page<StockMovement> findByTenantIdAndBusinessIdAndBranchIdAndMovementTypeOrderByCreatedAtDesc(
            UUID tenantId,
            UUID businessId,
            UUID branchId,
            StockMovementType movementType,
            Pageable pageable
    );
    Optional<StockMovement> findByIdAndTenantIdAndBusinessIdAndBranchId(
            UUID id,
            UUID tenantId,
            UUID businessId,
            UUID branchId
    );
}
