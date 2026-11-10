package com.possaas.possaasbackend.security;

import java.util.UUID;

public record AuthenticatedUser(
        UUID userId,
        String email,
        UUID businessId
) {
}
