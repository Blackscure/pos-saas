package com.possaas.possaasbackend.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // Get all users belonging to a specific business
    Page<User> findAllByBusiness_Id(UUID businessId, Pageable pageable);

    // Find a user by ID within a specific business
    Optional<User> findByIdAndBusiness_Id(UUID userId, UUID businessId);

    // Check whether an email already exists within a business
    boolean existsByBusiness_IdAndEmail(UUID businessId, String email);

    // Find a user by email within a business
    Optional<User> findByBusiness_IdAndEmail(UUID businessId, String email);

    // Load user together with business, tenant, roles and permissions
    @Query("""
            SELECT DISTINCT u
            FROM User u
            LEFT JOIN FETCH u.business b
            LEFT JOIN FETCH b.tenant
            LEFT JOIN FETCH u.roles r
            LEFT JOIN FETCH r.permissions
            WHERE u.id = :userId
              AND b.id = :businessId
            """)
    Optional<User> findByIdWithRolesAndPermissions(
            @Param("userId") UUID userId,
            @Param("businessId") UUID businessId
    );
}