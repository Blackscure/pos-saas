package com.possaas.possaasbackend.purchaseorder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {

    Page<PurchaseOrder> findByBusinessId(UUID businessId, Pageable pageable);

    Page<PurchaseOrder> findByBusinessIdAndStatus(
            UUID businessId,
            PurchaseOrderStatus status,
            Pageable pageable
    );

    Optional<PurchaseOrder> findByIdAndBusinessId(UUID id, UUID businessId);

    Optional<PurchaseOrder> findByBusinessIdAndPoNumber(UUID businessId, String poNumber);

    boolean existsByBusinessIdAndPoNumber(UUID businessId, String poNumber);
}
