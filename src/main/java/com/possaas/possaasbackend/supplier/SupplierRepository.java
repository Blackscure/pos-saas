package com.possaas.possaasbackend.supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    Page<Supplier> findAllByBusiness_Id(
            UUID businessId,
            Pageable pageable
    );

    Optional<Supplier> findByIdAndBusiness_Id(
            UUID supplierId,
            UUID businessId
    );

    boolean existsByBusiness_IdAndNameIgnoreCase(
            UUID businessId,
            String name
    );

    boolean existsByBusiness_IdAndNameIgnoreCaseAndIdNot(
            UUID businessId,
            String name,
            UUID supplierId
    );

    boolean existsByIdAndBusiness_Id(
            UUID supplierId,
            UUID businessId
    );
}
