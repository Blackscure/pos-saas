package com.possaas.possaasbackend.inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    // Get all inventory records for a business branch
    List<Inventory> findByBusinessIdAndBranchId(
            UUID businessId,
            UUID branchId
    );

    // Get inventory for a specific product within a branch
    List<Inventory> findByBusinessIdAndBranchIdAndProductId(
            UUID businessId,
            UUID branchId,
            UUID productId
    );

    // Find inventory for a specific product or variant
    Optional<Inventory> findByBusinessIdAndBranchIdAndProductIdAndProductVariantId(
            UUID businessId,
            UUID branchId,
            UUID productId,
            UUID productVariantId
    );

    // Check whether inventory already exists for a product or variant
    boolean existsByBusinessIdAndBranchIdAndProductIdAndProductVariantId(
            UUID businessId,
            UUID branchId,
            UUID productId,
            UUID productVariantId
    );

    // Find inventory record by its ID within a business
    Optional<Inventory> findByIdAndBusinessId(
            UUID inventoryId,
            UUID businessId
    );

    // Get all inventory records for a business
    List<Inventory> findByBusinessId(UUID businessId);
}
