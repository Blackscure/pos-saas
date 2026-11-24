
package com.possaas.possaasbackend.user.dto;

import com.possaas.possaasbackend.business.dto.BusinessResponse;
import com.possaas.possaasbackend.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        BusinessResponse business,
        String firstName,
        String lastName,
        String email,
        String phone,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponse(
                user.getId(),
                BusinessResponse.fromEntity(user.getBusiness()),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
