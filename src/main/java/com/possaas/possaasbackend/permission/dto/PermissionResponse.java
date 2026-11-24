
package com.possaas.possaasbackend.permission.dto;

import com.possaas.possaasbackend.permission.Permission;

import java.util.UUID;

public record PermissionResponse(
        UUID id,
        String code,
        String name,
        String description,
        String category
) {

    public static PermissionResponse fromEntity(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getCode(),
                permission.getName(),
                permission.getDescription(),
                permission.getCategory()
        );
    }
}
