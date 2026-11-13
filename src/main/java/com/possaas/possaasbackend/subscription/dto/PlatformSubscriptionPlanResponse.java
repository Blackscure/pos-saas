package com.possaas.possaasbackend.subscription.dto;

import com.possaas.possaasbackend.subscription.SubscriptionPlan;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PlatformSubscriptionPlanResponse(

        UUID id,
        String name,
        String code,
        String description,
        BigDecimal monthlyPrice,
        BigDecimal yearlyPrice,
        String currency,
        Integer maxBranches,
        Integer maxUsers,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {

    public static PlatformSubscriptionPlanResponse fromEntity(
            SubscriptionPlan plan
    ) {
        return new PlatformSubscriptionPlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getCode(),
                plan.getDescription(),
                plan.getMonthlyPrice(),
                plan.getYearlyPrice(),
                plan.getCurrency(),
                plan.getMaxBranches(),
                plan.getMaxUsers(),
                plan.isActive(),
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }
}
