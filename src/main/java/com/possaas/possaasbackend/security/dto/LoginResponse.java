
package com.possaas.possaasbackend.security.dto;

import java.util.UUID;

public record LoginResponse(

        String accessToken,

        String tokenType,

        long expiresIn,

        UUID userId,

        UUID businessId,

        String firstName,

        String lastName,

        String email

) {
}
