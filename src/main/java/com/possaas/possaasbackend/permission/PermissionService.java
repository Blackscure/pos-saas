
package com.possaas.possaasbackend.permission;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAllByOrderByCategoryAscNameAsc();
    }

    public List<Permission> getPermissionsByCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException(
                    "Permission category is required"
            );
        }

        return permissionRepository.findAllByCategoryOrderByNameAsc(
                category.trim().toUpperCase()
        );
    }

    public Permission getPermissionByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Permission code is required"
            );
        }

        return permissionRepository.findByCode(
                code.trim().toUpperCase()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Permission not found with code: " + code
                )
        );
    }

    public boolean permissionExists(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }

        return permissionRepository.existsByCode(
                code.trim().toUpperCase()
        );
    }
}
