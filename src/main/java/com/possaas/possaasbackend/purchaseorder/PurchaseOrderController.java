
package com.possaas.possaasbackend.purchaseorder;

import com.possaas.possaasbackend.common.pagination.PageResponse;
import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.purchaseorder.dto.CreatePurchaseOrderRequest;
import com.possaas.possaasbackend.purchaseorder.dto.PurchaseOrderResponse;
import com.possaas.possaasbackend.purchaseorder.dto.UpdatePurchaseOrderRequest;
import com.possaas.possaasbackend.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/businesses/{businessId}/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    // =========================================================
    // CREATE PURCHASE ORDER
    // =========================================================

    @PostMapping
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_CREATE') and #businessId == authentication.principal.businessId")
    public ApiResponse<PurchaseOrderResponse> createPurchaseOrder(
            @PathVariable UUID businessId,
            @Valid @RequestBody CreatePurchaseOrderRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        PurchaseOrderResponse purchaseOrder = purchaseOrderService.createPurchaseOrder(
                businessId,
                user.tenantId(),
                user.userId(),
                request
        );

        return ApiResponse.success("Purchase order created successfully", purchaseOrder);
    }

    // =========================================================
    // GET PURCHASE ORDERS
    // =========================================================

    @GetMapping
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_VIEW') and #businessId == authentication.principal.businessId")
    public ApiResponse<PageResponse<PurchaseOrderResponse>> getPurchaseOrders(
            @PathVariable UUID businessId,
            @RequestParam(required = false) PurchaseOrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        Pageable pageable = PageRequest.of(page, size);

        Page<PurchaseOrderResponse> purchaseOrders =
                purchaseOrderService.getPurchaseOrders(
                        businessId,
                        user.tenantId(),
                        status,
                        pageable
                );

        return ApiResponse.success(
                "Purchase orders retrieved successfully",
                PageResponse.of(purchaseOrders)
        );
    }

    // =========================================================
    // GET PURCHASE ORDER BY ID
    // =========================================================

    @GetMapping("/{purchaseOrderId}")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_VIEW') and #businessId == authentication.principal.businessId")
    public ApiResponse<PurchaseOrderResponse> getPurchaseOrderById(
            @PathVariable UUID businessId,
            @PathVariable UUID purchaseOrderId,
            Authentication authentication
    ) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        PurchaseOrderResponse purchaseOrder =
                purchaseOrderService.getPurchaseOrderById(
                        businessId,
                        user.tenantId(),
                        purchaseOrderId
                );

        return ApiResponse.success(
                "Purchase order retrieved successfully",
                purchaseOrder
        );
    }

    // =========================================================
    // UPDATE PURCHASE ORDER (DRAFT ONLY)
    // =========================================================

    @PutMapping("/{purchaseOrderId}")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_UPDATE') and #businessId == authentication.principal.businessId")
    public ApiResponse<PurchaseOrderResponse> updatePurchaseOrder(
            @PathVariable UUID businessId,
            @PathVariable UUID purchaseOrderId,
            @Valid @RequestBody UpdatePurchaseOrderRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        PurchaseOrderResponse purchaseOrder =
                purchaseOrderService.updatePurchaseOrder(
                        businessId,
                        user.tenantId(),
                        purchaseOrderId,
                        request
                );

        return ApiResponse.success(
                "Purchase order updated successfully",
                purchaseOrder
        );
    }

    // =========================================================
    // SUBMIT PURCHASE ORDER
    // =========================================================

    @PostMapping("/{purchaseOrderId}/submit")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_SUBMIT') and #businessId == authentication.principal.businessId")
    public ApiResponse<PurchaseOrderResponse> submitPurchaseOrder(
            @PathVariable UUID businessId,
            @PathVariable UUID purchaseOrderId,
            Authentication authentication
    ) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        PurchaseOrderResponse purchaseOrder =
                purchaseOrderService.submitPurchaseOrder(
                        businessId,
                        user.tenantId(),
                        purchaseOrderId
                );

        return ApiResponse.success(
                "Purchase order submitted successfully",
                purchaseOrder
        );
    }

    // =========================================================
    // APPROVE PURCHASE ORDER
    // =========================================================

    @PostMapping("/{purchaseOrderId}/approve")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_APPROVE') and #businessId == authentication.principal.businessId")
    public ApiResponse<PurchaseOrderResponse> approvePurchaseOrder(
            @PathVariable UUID businessId,
            @PathVariable UUID purchaseOrderId,
            Authentication authentication
    ) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        PurchaseOrderResponse purchaseOrder =
                purchaseOrderService.approvePurchaseOrder(
                        businessId,
                        user.tenantId(),
                        purchaseOrderId,
                        user.userId()
                );

        return ApiResponse.success(
                "Purchase order approved successfully",
                purchaseOrder
        );
    }

    // =========================================================
    // REJECT PURCHASE ORDER
    // =========================================================

    @PostMapping("/{purchaseOrderId}/reject")
    @PreAuthorize("hasAuthority('PURCHASE_ORDER_REJECT') and #businessId == authentication.principal.businessId")
    public ApiResponse<PurchaseOrderResponse> rejectPurchaseOrder(
            @PathVariable UUID businessId,
            @PathVariable UUID purchaseOrderId,
            Authentication authentication
    ) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        PurchaseOrderResponse purchaseOrder =
                purchaseOrderService.rejectPurchaseOrder(
                        businessId,
                        user.tenantId(),
                        purchaseOrderId,
                        user.userId()
                );

        return ApiResponse.success(
                "Purchase order rejected successfully",
                purchaseOrder
        );
    }
}
