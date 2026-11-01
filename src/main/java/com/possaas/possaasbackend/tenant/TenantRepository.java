package com.possaas.possaasbackend.tenant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findByCode(String code);

    Optional<Tenant> findByEmail(String email);

    boolean existsByCode(String code);

    boolean existsByEmail(String email);
}