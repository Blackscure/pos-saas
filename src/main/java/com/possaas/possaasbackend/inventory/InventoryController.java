package com.possaas.possaasbackend.inventory;

import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.inventory.dto.CreateInventoryRequest;
import com.possaas.possaasbackend.inventory.dto.InventoryResponse;
import com.possaas.possaasbackend.inventory.dto.UpdateInventoryRequest;
import com.possaas.possaasbackend.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/businesses/{businessId}")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // =========================================================
    // CREATE INVENTORY
    // =========================================================

    @PostMapping("/branches/{branchId}/inventory")
    public ApiResponse<InventoryResponse> createInventory(
            @PathVariable UUID businessId,
            @PathVariable UUID branchId,
            @Valid @RequestBody CreateInventoryRequest request,
            Authentication authentication
    ) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        InventoryResponse response = inventoryService.createInventory(
                user.tenantId(),
                businessId,
                branchId,
                request
        );

        return ApiResponse.success(
                "Inventory created successfully",
                response
        );
    }

    // =========================================================
    // GET BRANCH INVENTORY
    // =========================================================

    @GetMapping("/branches/{branchId}/inventory")
    public ApiResponse<List<InventoryResponse>> getBranchInventory(
            @PathVariable UUID businessId,
            @PathVariable UUID branchId,
            Authentication authentication
    ) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        List<InventoryResponse> response = inventoryService.getBranchInventory(
                user.tenantId(),
                businessId,
                branchId
        );

        return ApiResponse.success(
                "Branch inventory retrieved successfully",
                response
        );
    }

    // =========================================================
    // GET PRODUCT INVENTORY IN A BRANCH
    // =========================================================

    @GetMapping("/branches/{branchId}/inventory/products/{productId}")
    public ApiResponse<List<InventoryResponse>> getProductInventory(
            @PathVariable UUID businessId,
            @PathVariable UUID branchId,
            @PathVariable UUID productId,
            Authentication authentication
    ) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        List<InventoryResponse> response = inventoryService.getProductInventory(
                user.tenantId(),
                businessId,
                branchId,
                productId
        );

        return ApiResponse.success(
                "Product inventory retrieved successfully",
                response
        );
    }

    // =========================================================
    // GET INVENTORY BY ID
    // =========================================================

    @GetMapping("/inventory/{inventoryId}")
    public ApiResponse<InventoryResponse> getInventoryById(
            @PathVariable UUID businessId,
            @PathVariable UUID inventoryId,
            Authentication authentication
    ) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        InventoryResponse response = inventoryService.getInventoryById(
                user.tenantId(),
                businessId,
                inventoryId
        );

        return ApiResponse.success(
                "Inventory retrieved successfully",
                response
        );
    }

    // =========================================================
    // GET BUSINESS INVENTORY
    // =========================================================

    @GetMapping("/inventory")
    public ApiResponse<List<InventoryResponse>> getBusinessInventory(
            @PathVariable UUID businessId,
            Authentication authentication
    ) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        List<InventoryResponse> response = inventoryService.getBusinessInventory(
                user.tenantId(),
                businessId
        );

        return ApiResponse.success(
                "Business inventory retrieved successfully",
                response
        );
    }

    // =========================================================
    // UPDATE REORDER LEVEL
    // =========================================================

    @PatchMapping("/inventory/{inventoryId}/reorder-level")
    public ApiResponse<InventoryResponse> updateReorderLevel(
            @PathVariable UUID businessId,
            @PathVariable UUID inventoryId,
            @Valid @RequestBody UpdateInventoryRequest request,
            Authentication authentication
    ) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        InventoryResponse response = inventoryService.updateReorderLevel(
                user.tenantId(),
                businessId,
                inventoryId,
                request
        );

        return ApiResponse.success(
                "Inventory reorder level updated successfully",
                response
        );
    }
}
