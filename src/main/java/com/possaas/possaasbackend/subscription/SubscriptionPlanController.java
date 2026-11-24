package com.possaas.possaasbackend.subscription;

import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.subscription.dto.SubscriptionPlanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/subscription-plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    /**
     * Get all active subscription plans.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getActivePlans() {

        List<SubscriptionPlanResponse> plans =
                subscriptionPlanService.getActivePlans()
                        .stream()
                        .map(SubscriptionPlanResponse::fromEntity)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Subscription plans retrieved successfully",
                        plans
                )
        );
    }

    /**
     * Get an active subscription plan by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> getPlanById(
            @PathVariable UUID id
    ) {

        SubscriptionPlanResponse plan =
                SubscriptionPlanResponse.fromEntity(
                        subscriptionPlanService.getActivePlanById(id)
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Subscription plan retrieved successfully",
                        plan
                )
        );
    }

    /**
     * Get an active subscription plan by code.
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> getPlanByCode(
            @PathVariable String code
    ) {

        SubscriptionPlanResponse plan =
                SubscriptionPlanResponse.fromEntity(
                        subscriptionPlanService.getActivePlanByCode(code)
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Subscription plan retrieved successfully",
                        plan
                )
        );
    }
}
