package com.possaas.possaasbackend.subscription;

import com.possaas.possaasbackend.common.pagination.PageResponse;
import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.security.AuthenticatedUser;
import com.possaas.possaasbackend.subscription.dto.SubscriptionResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * OWNER and MANAGER can view the current subscription.
     */
    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getCurrentSubscription(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        SubscriptionResponse response = SubscriptionResponse.fromEntity(
                subscriptionService.getCurrentSubscription(
                        authenticatedUser.tenantId()
                )
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Current subscription retrieved successfully",
                        response
                )
        );
    }

    /**
     * OWNER and MANAGER can view subscription history.
     */
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<ApiResponse<PageResponse<SubscriptionResponse>>> getSubscriptionHistory(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<SubscriptionResponse> subscriptions =
                subscriptionService.getSubscriptionHistory(
                        authenticatedUser.tenantId(),
                        pageable
                ).map(SubscriptionResponse::fromEntity);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Subscription history retrieved successfully",
                        PageResponse.of(subscriptions)
                )
        );
    }

    /**
     * Only OWNER can cancel a subscription.
     */
    @PatchMapping("/{subscriptionId}/cancel")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancelSubscription(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable UUID subscriptionId
    ) {
        SubscriptionResponse response = SubscriptionResponse.fromEntity(
                subscriptionService.cancelSubscription(
                        authenticatedUser.tenantId(),
                        subscriptionId
                )
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Subscription cancelled successfully",
                        response
                )
        );
    }
}
