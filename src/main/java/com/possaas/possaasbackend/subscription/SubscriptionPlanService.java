package com.possaas.possaasbackend.subscription;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    /**
     * Returns all active subscription plans.
     */
    public List<SubscriptionPlan> getActivePlans() {
        return subscriptionPlanRepository.findAllByActiveTrue();
    }

    /**
     * Returns an active subscription plan by its ID.
     */
    public SubscriptionPlan getActivePlanById(UUID planId) {

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Subscription plan not found"
                        )
                );

        if (!plan.isActive()) {
            throw new IllegalArgumentException(
                    "Subscription plan is inactive"
            );
        }

        return plan;
    }

    /**
     * Returns an active subscription plan by its code.
     */
    public SubscriptionPlan getActivePlanByCode(String code) {

        SubscriptionPlan plan = subscriptionPlanRepository.findByCode(code)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Subscription plan not found"
                        )
                );

        if (!plan.isActive()) {
            throw new IllegalArgumentException(
                    "Subscription plan is inactive"
            );
        }

        return plan;
    }
}
