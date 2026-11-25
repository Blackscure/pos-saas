package com.possaas.possaasbackend.inventory;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    // =========================================================
    // GET INVENTORY BY BUSINESS AND BRANCH
    // =========================================================

    List<Inventory> findByBusinessIdAndBranchId(
            UUID businessId,
            UUID branchId
    );

    // =========================================================
    // GET INVENTORY BY PRODUCT
    // =========================================================

    List<Inventory> findByBusinessIdAndBranchIdAndProductId(
            UUID businessId,
            UUID branchId,
            UUID productId
    );

    // =========================================================
    // FIND INVENTORY BY PRODUCT OR VARIANT
    // =========================================================

    Optional<Inventory> findByBusinessIdAndBranchIdAndProductIdAndProductVariantId(
            UUID businessId,
            UUID branchId,
            UUID productId,
            UUID productVariantId
    );

    // =========================================================
    // CHECK EXISTING INVENTORY
    // =========================================================

    boolean existsByBusinessIdAndBranchIdAndProductIdAndProductVariantId(
            UUID businessId,
            UUID branchId,
            UUID productId,
            UUID productVariantId
    );

    // =========================================================
    // FIND INVENTORY BY ID AND BUSINESS
    // =========================================================

    Optional<Inventory> findByIdAndBusinessId(
            UUID inventoryId,
            UUID businessId
    );

    // =========================================================
    // FIND INVENTORY BY ID, BUSINESS AND BRANCH WITH WRITE LOCK
    // =========================================================

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Inventory> findByIdAndBusinessIdAndBranchId(
            UUID inventoryId,
            UUID businessId,
            UUID branchId
    );

    // =========================================================
    // GET ALL INVENTORY FOR BUSINESS
    // =========================================================

    List<Inventory> findByBusinessId(UUID businessId);
}