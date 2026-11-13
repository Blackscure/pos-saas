package com.possaas.possaasbackend.subscription;

import com.possaas.possaasbackend.subscription.dto.CreatePlanRequest;
import com.possaas.possaasbackend.subscription.dto.UpdatePlanRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PlatformSubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public SubscriptionPlan createPlan(CreatePlanRequest request) {

        String code = request.code().trim().toUpperCase(Locale.ROOT);

        if (subscriptionPlanRepository.existsByCode(code)) {
            throw new IllegalArgumentException(
                    "A subscription plan with this code already exists"
            );
        }

        SubscriptionPlan plan = new SubscriptionPlan();

        plan.setName(request.name().trim());
        plan.setCode(code);
        plan.setDescription(request.description());
        plan.setMonthlyPrice(request.monthlyPrice());
        plan.setYearlyPrice(request.yearlyPrice());
        plan.setCurrency(request.currency().trim().toUpperCase(Locale.ROOT));
        plan.setMaxBranches(request.maxBranches());
        plan.setMaxUsers(request.maxUsers());
        plan.setActive(true);

        return subscriptionPlanRepository.save(plan);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlan> getAllPlans() {
        return subscriptionPlanRepository.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

    @Transactional(readOnly = true)
    public SubscriptionPlan getPlanById(UUID id) {
        return subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Subscription plan not found"
                ));
    }

    public SubscriptionPlan updatePlan(UUID id, UpdatePlanRequest request) {

        SubscriptionPlan plan = getPlanById(id);

        plan.setName(request.name().trim());
        plan.setDescription(request.description());
        plan.setMonthlyPrice(request.monthlyPrice());
        plan.setYearlyPrice(request.yearlyPrice());
        plan.setCurrency(request.currency().trim().toUpperCase(Locale.ROOT));
        plan.setMaxBranches(request.maxBranches());
        plan.setMaxUsers(request.maxUsers());

        return subscriptionPlanRepository.save(plan);
    }

    public SubscriptionPlan updatePlanStatus(UUID id, boolean active) {

        SubscriptionPlan plan = getPlanById(id);

        plan.setActive(active);

        return subscriptionPlanRepository.save(plan);
    }
}
