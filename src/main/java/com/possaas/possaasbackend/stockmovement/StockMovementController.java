package com.possaas.possaasbackend.stockmovement;

import com.possaas.possaasbackend.common.pagination.PageResponse;
import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.security.AuthenticatedUser;
import com.possaas.possaasbackend.stockmovement.dto.StockAdjustmentRequest;
import com.possaas.possaasbackend.stockmovement.dto.StockMovementRequest;
import com.possaas.possaasbackend.stockmovement.dto.StockMovementResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/businesses/{businessId}/branches/{branchId}/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    // =========================================================
    // STOCK IN
    // =========================================================

    @PostMapping("/in")
    public ApiResponse<StockMovementResponse> stockIn(
            @PathVariable UUID businessId,
            @PathVariable UUID branchId,
            @Valid @RequestBody StockMovementRequest request,
            Authentication authentication
    ) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        StockMovementResponse response = stockMovementService.stockIn(
                user.tenantId(),
                businessId,
                branchId,
                user.userId(),
                request
        );

        return ApiResponse.success("Stock received successfully", response);
    }

    // =========================================================
    // STOCK OUT
    // =========================================================

    @PostMapping("/out")
    public ApiResponse<StockMovementResponse> stockOut(
            @PathVariable UUID businessId,
            @PathVariable UUID branchId,
            @Valid @RequestBody StockMovementRequest request,
            Authentication authentication
    ) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        StockMovementResponse response = stockMovementService.stockOut(
                user.tenantId(),
                businessId,
                branchId,
                user.userId(),
                request
        );

        return ApiResponse.success("Stock removed successfully", response);
    }

    // =========================================================
    // STOCK ADJUSTMENT
    // =========================================================

    @PostMapping("/adjustments")
    public ApiResponse<StockMovementResponse> adjustStock(
            @PathVariable UUID businessId,
            @PathVariable UUID branchId,
            @Valid @RequestBody StockAdjustmentRequest request,
            Authentication authentication
    ) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        StockMovementResponse response = stockMovementService.adjustStock(
                user.tenantId(),
                businessId,
                branchId,
                user.userId(),
                request
        );

        return ApiResponse.success("Stock adjusted successfully", response);
    }

    // =========================================================
    // BRANCH MOVEMENT HISTORY
    // =========================================================

    @GetMapping
    public ApiResponse<PageResponse<StockMovementResponse>> getBranchMovementHistory(
            @PathVariable UUID businessId,
            @PathVariable UUID branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        PageResponse<StockMovementResponse> response =
                stockMovementService.getBranchMovementHistory(
                        user.tenantId(),
                        businessId,
                        branchId,
                        page,
                        size
                );

        return ApiResponse.success("Stock movement history retrieved successfully", response);
    }

    // =========================================================
    // SINGLE STOCK MOVEMENT
    // =========================================================

    @GetMapping("/{movementId}")
    public ApiResponse<StockMovementResponse> getMovement(
            @PathVariable UUID businessId,
            @PathVariable UUID branchId,
            @PathVariable UUID movementId,
            Authentication authentication
    ) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        StockMovementResponse response = stockMovementService.getMovement(
                user.tenantId(),
                businessId,
                branchId,
                movementId
        );

        return ApiResponse.success("Stock movement retrieved successfully", response);
    }
}
