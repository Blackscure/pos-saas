package com.possaas.possaasbackend.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
}