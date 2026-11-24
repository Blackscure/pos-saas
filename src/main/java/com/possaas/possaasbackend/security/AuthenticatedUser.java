package com.possaas.possaasbackend.security;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedUser(
        UUID userId,
        String email,
        UUID businessId,
        UUID tenantId,
        Set<String> roles,
        Set<String> permissions
) {
}
