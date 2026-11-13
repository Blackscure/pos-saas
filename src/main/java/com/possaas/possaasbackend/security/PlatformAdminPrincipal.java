package com.possaas.possaasbackend.security;

import java.util.UUID;

public record PlatformAdminPrincipal(
        UUID adminId,
        String email,
        String role
) {
}