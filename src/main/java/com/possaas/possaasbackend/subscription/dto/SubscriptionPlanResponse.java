package com.possaas.possaasbackend.subscription.dto;

import com.possaas.possaasbackend.subscription.SubscriptionPlan;

import java.math.BigDecimal;
import java.util.UUID;

public record SubscriptionPlanResponse(
        UUID id,
        String name,
        String code,
        String description,
        BigDecimal monthlyPrice,
        BigDecimal yearlyPrice,
        String currency,
        Integer maxBranches,
        Integer maxUsers
) {

    public static SubscriptionPlanResponse fromEntity(
            SubscriptionPlan plan
    ) {
        return new SubscriptionPlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getCode(),
                plan.getDescription(),
                plan.getMonthlyPrice(),
                plan.getYearlyPrice(),
                plan.getCurrency(),
                plan.getMaxBranches(),
                plan.getMaxUsers()
        );
    }
}
