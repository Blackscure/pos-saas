package com.possaas.possaasbackend.subscription;

import com.possaas.possaasbackend.tenant.Tenant;
import com.possaas.possaasbackend.tenant.TenantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SubscriptionService {

    private static final List<SubscriptionStatus> CURRENT_STATUSES =
            List.of(
                    SubscriptionStatus.TRIALING,
                    SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.PAST_DUE,
                    SubscriptionStatus.SUSPENDED
            );

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final TenantRepository tenantRepository;

    public SubscriptionService(
            SubscriptionRepository subscriptionRepository,
            SubscriptionPlanRepository subscriptionPlanRepository,
            TenantRepository tenantRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.tenantRepository = tenantRepository;
    }

    // Create a subscription for a tenant.
    public Subscription createSubscription(
            UUID tenantId,
            UUID planId,
            SubscriptionStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime trialEndDate
    ) {
        Tenant tenant = getActiveTenant(tenantId);

        SubscriptionPlan plan = subscriptionPlanRepository
                .findById(planId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Subscription plan not found")
                );

        if (!plan.isActive()) {
            throw new IllegalArgumentException(
                    "Cannot subscribe to an inactive plan"
            );
        }

        if (status != SubscriptionStatus.TRIALING &&
                status != SubscriptionStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "New subscriptions must be TRIALING or ACTIVE"
            );
        }

        if (status == SubscriptionStatus.TRIALING && trialEndDate == null) {
            throw new IllegalArgumentException(
                    "Trial end date is required for trial subscriptions"
            );
        }

        LocalDateTime effectiveStartDate =
                startDate != null ? startDate : LocalDateTime.now();

        if (endDate != null && endDate.isBefore(effectiveStartDate)) {
            throw new IllegalArgumentException(
                    "Subscription end date cannot be before start date"
            );
        }

        if (trialEndDate != null &&
                trialEndDate.isBefore(effectiveStartDate)) {
            throw new IllegalArgumentException(
                    "Trial end date cannot be before start date"
            );
        }

        boolean hasCurrentSubscription =
                subscriptionRepository.existsByTenant_IdAndStatusIn(
                        tenantId,
                        CURRENT_STATUSES
                );

        if (hasCurrentSubscription) {
            throw new IllegalArgumentException(
                    "Tenant already has a current subscription"
            );
        }

        Subscription subscription = new Subscription();
        subscription.setTenant(tenant);
        subscription.setPlan(plan);
        subscription.setStatus(status);
        subscription.setStartDate(effectiveStartDate);
        subscription.setEndDate(endDate);
        subscription.setTrialEndDate(trialEndDate);

        return subscriptionRepository.save(subscription);
    }

    // Get the current subscription of a tenant.
    @Transactional(readOnly = true)
    public Subscription getCurrentSubscription(UUID tenantId) {

        getTenant(tenantId);

        return subscriptionRepository
                .findFirstByTenant_IdAndStatusInOrderByCreatedAtDesc(
                        tenantId,
                        CURRENT_STATUSES
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No current subscription found for this tenant"
                        )
                );
    }

    // Get subscription history for a tenant.
    @Transactional(readOnly = true)
    public Page<Subscription> getSubscriptionHistory(
            UUID tenantId,
            Pageable pageable
    ) {
        getTenant(tenantId);

        return subscriptionRepository.findAllByTenant_Id(
                tenantId,
                pageable
        );
    }

    // Cancel a tenant's subscription.
    public Subscription cancelSubscription(
            UUID tenantId,
            UUID subscriptionId
    ) {
        Subscription subscription = subscriptionRepository
                .findByIdAndTenant_Id(subscriptionId, tenantId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Subscription not found for this tenant"
                        )
                );

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED ||
                subscription.getStatus() == SubscriptionStatus.EXPIRED) {
            throw new IllegalArgumentException(
                    "Subscription is already cancelled or expired"
            );
        }

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());

        return subscriptionRepository.save(subscription);
    }

    private Tenant getTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Tenant not found")
                );
    }

    private Tenant getActiveTenant(UUID tenantId) {
        Tenant tenant = getTenant(tenantId);

        if (!tenant.isActive()) {
            throw new IllegalArgumentException(
                    "Cannot create a subscription for an inactive tenant"
            );
        }

        return tenant;
    }
}
