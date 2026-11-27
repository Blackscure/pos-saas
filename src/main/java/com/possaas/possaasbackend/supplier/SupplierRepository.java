package com.possaas.possaasbackend.supplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Locale;
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

    default Page<Supplier> searchByBusiness(
            UUID businessId,
            String search,
            Pageable pageable
    ) {
        if (search == null || search.isBlank()) {
            return findAllByBusiness_Id(businessId, pageable);
        }

        String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
        return searchByBusinessWithPattern(businessId, pattern, pageable);
    }

    @Query("""
            SELECT s FROM Supplier s
            WHERE s.business.id = :businessId
            AND (
                LOWER(s.name) LIKE :pattern
                OR LOWER(COALESCE(s.contactPerson, '')) LIKE :pattern
                OR LOWER(COALESCE(s.phone, '')) LIKE :pattern
                OR LOWER(COALESCE(s.email, '')) LIKE :pattern
                OR LOWER(COALESCE(s.taxPin, '')) LIKE :pattern
            )
            """)
    Page<Supplier> searchByBusinessWithPattern(
            @Param("businessId") UUID businessId,
            @Param("pattern") String pattern,
            Pageable pageable
    );
}
