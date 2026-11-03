package com.possaas.possaasbackend.branch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {

    Page<Branch> findAllByBusiness_Id(UUID businessId, Pageable pageable);

    Optional<Branch> findByIdAndBusiness_Id(UUID branchId, UUID businessId);

    boolean existsByBusiness_IdAndName(UUID businessId, String name);

    boolean existsByBusiness_IdAndCode(UUID businessId, String code);
}