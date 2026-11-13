package com.possaas.possaasbackend.platformadmin;

import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.subscription.PlatformSubscriptionPlanService;
import com.possaas.possaasbackend.subscription.SubscriptionPlan;
import com.possaas.possaasbackend.subscription.dto.CreatePlanRequest;
import com.possaas.possaasbackend.subscription.dto.PlatformSubscriptionPlanResponse;
import com.possaas.possaasbackend.subscription.dto.UpdatePlanRequest;
import com.possaas.possaasbackend.subscription.dto.UpdatePlanStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/platform/subscription-plans")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class PlatformSubscriptionPlanController {

    private final PlatformSubscriptionPlanService planService;

    @PostMapping
    public ResponseEntity<ApiResponse<PlatformSubscriptionPlanResponse>> createPlan(
            @Valid @RequestBody CreatePlanRequest request
    ) {
        SubscriptionPlan plan = planService.createPlan(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        "Subscription plan created successfully",
                        PlatformSubscriptionPlanResponse.fromEntity(plan)
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlatformSubscriptionPlanResponse>>> getAllPlans() {

        List<PlatformSubscriptionPlanResponse> plans =
                planService.getAllPlans()
                        .stream()
                        .map(PlatformSubscriptionPlanResponse::fromEntity)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Subscription plans retrieved successfully",
                        plans
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlatformSubscriptionPlanResponse>> getPlanById(
            @PathVariable UUID id
    ) {
        SubscriptionPlan plan = planService.getPlanById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Subscription plan retrieved successfully",
                        PlatformSubscriptionPlanResponse.fromEntity(plan)
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PlatformSubscriptionPlanResponse>> updatePlan(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePlanRequest request
    ) {
        SubscriptionPlan plan = planService.updatePlan(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Subscription plan updated successfully",
                        PlatformSubscriptionPlanResponse.fromEntity(plan)
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PlatformSubscriptionPlanResponse>> updatePlanStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePlanStatusRequest request
    ) {
        SubscriptionPlan plan = planService.updatePlanStatus(
                id,
                request.active()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Subscription plan status updated successfully",
                        PlatformSubscriptionPlanResponse.fromEntity(plan)
                )
        );
    }
}
