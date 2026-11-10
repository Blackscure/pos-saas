package com.possaas.possaasbackend.registration.dto;

import java.util.UUID;

public record RegistrationResponse(
        UUID tenantId,
        UUID businessId,
        UUID branchId,
        UUID userId,
        String ownerEmail,
        String message
) {
}