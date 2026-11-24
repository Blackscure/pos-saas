package com.possaas.possaasbackend.security.dto;

import com.possaas.possaasbackend.business.dto.BusinessResponse;

import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserSummary user,
        BusinessResponse business
) {

    public record UserSummary(
            UUID id,
            String firstName,
            String lastName,
            String email,
            String phone,
            boolean active
    ) {
    }
}
