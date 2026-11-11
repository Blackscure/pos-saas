package com.possaas.possaasbackend.subscription.dto;

import com.possaas.possaasbackend.subscription.Subscription;
import java.time.LocalDateTime;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id,
        UUID planId,
        String planName,
        String planCode,
        String status,
        LocalDateTime startDate,
        LocalDateTime endDate,
        LocalDateTime trialEndDate,
        LocalDateTime cancelledAt
) {
    public static SubscriptionResponse fromEntity(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getPlan().getId(),
                subscription.getPlan().getName(),
                subscription.getPlan().getCode(),
                subscription.getStatus().name(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getTrialEndDate(),
                subscription.getCancelledAt()
        );
    }
}
