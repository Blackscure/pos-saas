package com.possaas.possaasbackend.platformadmin.dto;

import java.util.UUID;

public record PlatformAdminLoginResponse(

        String accessToken,

        String tokenType,

        long expiresIn,

        UUID adminId,

        String firstName,

        String lastName,

        String email,

        String role

) {
}