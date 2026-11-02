package com.possaas.possaasbackend.business;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {

    // Find all businesses belonging to a tenant
    @EntityGraph(attributePaths = {"tenant"})
    List<Business> findAllByTenantId(UUID tenantId);

    // Find all businesses belonging to a tenant with pagination
    @EntityGraph(attributePaths = {"tenant"})
    Page<Business> findAllByTenantId(UUID tenantId, Pageable pageable);

    // Find a specific business belonging to a tenant
    @EntityGraph(attributePaths = {"tenant"})
    Optional<Business> findByIdAndTenantId(UUID id, UUID tenantId);

    // Check whether a business name already exists under a tenant
    boolean existsByTenantIdAndName(UUID tenantId, String name);

    // Find businesses by their business type
    @EntityGraph(attributePaths = {"tenant"})
    List<Business> findAllByTenantIdAndBusinessType(
            UUID tenantId,
            BusinessType businessType
    );

    // Find businesses by their business type with pagination
    @EntityGraph(attributePaths = {"tenant"})
    Page<Business> findAllByTenantIdAndBusinessType(
            UUID tenantId,
            BusinessType businessType,
            Pageable pageable
    );

    // Check whether a registration number already exists under a tenant
    boolean existsByTenantIdAndRegistrationNumber(
            UUID tenantId,
            String registrationNumber
    );
}
