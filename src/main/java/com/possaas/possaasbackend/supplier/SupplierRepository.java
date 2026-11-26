package com.possaas.possaasbackend.supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            SELECT s FROM Supplier s
            WHERE s.business.id = :businessId
            AND (
                :search IS NULL
                OR TRIM(:search) = ''
                OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(s.contactPerson, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(s.phone, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(s.email, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(s.taxPin, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            """)
    Page<Supplier> searchByBusiness(
            @Param("businessId") UUID businessId,
            @Param("search") String search,
            Pageable pageable
    );
}
