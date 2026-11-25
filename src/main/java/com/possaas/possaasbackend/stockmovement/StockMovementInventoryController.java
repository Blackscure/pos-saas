package com.possaas.possaasbackend.stockmovement;

import com.possaas.possaasbackend.common.pagination.PageResponse;
import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.security.AuthenticatedUser;
import com.possaas.possaasbackend.stockmovement.dto.StockMovementResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/businesses/{businessId}/inventory/{inventoryId}/stock-movements")
public class StockMovementInventoryController {

    private final StockMovementService stockMovementService;

    public StockMovementInventoryController(
            StockMovementService stockMovementService
    ) {
        this.stockMovementService = stockMovementService;
    }

    @GetMapping
    public ApiResponse<PageResponse<StockMovementResponse>> getInventoryMovementHistory(
            @PathVariable UUID businessId,
            @PathVariable UUID inventoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        PageResponse<StockMovementResponse> response =
                stockMovementService.getInventoryMovementHistory(
                        user.tenantId(),
                        businessId,
                        inventoryId,
                        page,
                        size
                );

        return ApiResponse.success(
                "Inventory stock movement history retrieved successfully",
                response
        );
    }
}
