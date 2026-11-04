package com.possaas.possaasbackend.role.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record RolePermissionRequest(

        @NotNull(message = "Permission IDs are required")
        @NotEmpty(message = "At least one permission must be selected")
        Set<UUID> permissionIds

) {
}
