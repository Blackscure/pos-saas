package com.possaas.possaasbackend.role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Page<Role> findAllByBusiness_Id(UUID businessId, Pageable pageable);

    Optional<Role> findByIdAndBusiness_Id(UUID roleId, UUID businessId);

    Optional<Role> findByBusiness_IdAndName(UUID businessId, String name);

    boolean existsByBusiness_IdAndName(UUID businessId, String name);

    @Query("""
    SELECT DISTINCT r
    FROM Role r
    LEFT JOIN FETCH r.permissions
    WHERE r.id = :roleId
    AND r.business.id = :businessId
    """)
    Optional<Role> findByIdAndBusinessIdWithPermissions(
            @Param("roleId") UUID roleId,
            @Param("businessId") UUID businessId
    );
}