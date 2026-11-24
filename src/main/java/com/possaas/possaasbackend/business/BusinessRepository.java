package com.possaas.possaasbackend.business;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {

    // Platform Admin: retrieve all businesses across tenants
    @Override
    @EntityGraph(attributePaths = {"tenant"})
    Page<Business> findAll(Pageable pageable);

    // Platform Admin: retrieve a business by ID across tenants
    @Override
    @EntityGraph(attributePaths = {"tenant"})
    Optional<Business> findById(UUID id);

    // Tenant-scoped queries
    @EntityGraph(attributePaths = {"tenant"})
    List<Business> findAllByTenantId(UUID tenantId);

    @EntityGraph(attributePaths = {"tenant"})
    Page<Business> findAllByTenantId(UUID tenantId, Pageable pageable);

    @EntityGraph(attributePaths = {"tenant"})
    Optional<Business> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndName(UUID tenantId, String name);

    @EntityGraph(attributePaths = {"tenant"})
    List<Business> findAllByTenantIdAndBusinessType(
            UUID tenantId,
            BusinessType businessType
    );

    @EntityGraph(attributePaths = {"tenant"})
    Page<Business> findAllByTenantIdAndBusinessType(
            UUID tenantId,
            BusinessType businessType,
            Pageable pageable
    );

    boolean existsByTenantIdAndRegistrationNumber(
            UUID tenantId,
            String registrationNumber
    );
}
