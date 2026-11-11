package com.possaas.possaasbackend.registration.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegistrationResponse(
        UUID tenantId,
        UUID businessId,
        UUID branchId,
        UUID userId,
        String ownerEmail,
        UUID subscriptionId,
        UUID subscriptionPlanId,
        String subscriptionPlanName,
        String subscriptionStatus,
        LocalDateTime trialEndDate,
        String message
) {
}
