package com.possaas.possaasbackend.role.dto;

import com.possaas.possaasbackend.role.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record RoleResponse(

        UUID id,
        UUID businessId,
        String name,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {

    public static RoleResponse fromEntity(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getBusiness().getId(),
                role.getName(),
                role.getDescription(),
                role.isActive(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }
}