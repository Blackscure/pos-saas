package com.possaas.possaasbackend.permission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository
        extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByCode(String code);

    List<Permission> findAllByCategoryOrderByNameAsc(String category);

    List<Permission> findAllByOrderByCategoryAscNameAsc();

    boolean existsByCode(String code);
}