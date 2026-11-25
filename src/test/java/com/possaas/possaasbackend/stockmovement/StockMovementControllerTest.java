package com.possaas.possaasbackend.stockmovement;

import com.possaas.possaasbackend.security.AuthenticatedUser;
import com.possaas.possaasbackend.stockmovement.dto.StockAdjustmentRequest;
import com.possaas.possaasbackend.stockmovement.dto.StockMovementRequest;
import com.possaas.possaasbackend.stockmovement.dto.StockMovementResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMovementControllerTest {

    @Mock
    private StockMovementService stockMovementService;

    @Mock
    private Authentication authentication;

    @Mock
    private StockMovementResponse stockMovementResponse;

    @InjectMocks
    private StockMovementController stockMovementController;

    private UUID tenantId;
    private UUID userId;
    private UUID businessId;
    private UUID branchId;
    private UUID inventoryId;

    private AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setUp() {

        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        businessId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        inventoryId = UUID.randomUUID();

        authenticatedUser = new AuthenticatedUser(
                userId,
                "cashier@example.com",
                businessId,
                tenantId,
                Set.of("CASHIER"),
                Set.of("INVENTORY_MANAGE")
        );

        when(authentication.getPrincipal())
                .thenReturn(authenticatedUser);
    }

    // =========================================================
    // STOCK IN
    // =========================================================

    @Test
    void stockIn_shouldReturnSuccessResponse() {

        StockMovementRequest request = new StockMovementRequest(
                inventoryId,
                new BigDecimal("10.0000"),
                "Received new stock"
        );

        when(stockMovementService.stockIn(
                tenantId,
                businessId,
                branchId,
                userId,
                request
        )).thenReturn(stockMovementResponse);

        var response = stockMovementController.stockIn(
                businessId,
                branchId,
                request,
                authentication
        );

        assertNotNull(response);

        verify(stockMovementService).stockIn(
                tenantId,
                businessId,
                branchId,
                userId,
                request
        );
    }

    // =========================================================
    // STOCK OUT
    // =========================================================

    @Test
    void stockOut_shouldReturnSuccessResponse() {

        StockMovementRequest request = new StockMovementRequest(
                inventoryId,
                new BigDecimal("5.0000"),
                "Damaged stock"
        );

        when(stockMovementService.stockOut(
                tenantId,
                businessId,
                branchId,
                userId,
                request
        )).thenReturn(stockMovementResponse);

        var response = stockMovementController.stockOut(
                businessId,
                branchId,
                request,
                authentication
        );

        assertNotNull(response);

        verify(stockMovementService).stockOut(
                tenantId,
                businessId,
                branchId,
                userId,
                request
        );
    }

    // =========================================================
    // STOCK ADJUSTMENT
    // =========================================================

    @Test
    void adjustStock_shouldReturnSuccessResponse() {

        StockAdjustmentRequest request = new StockAdjustmentRequest(
                inventoryId,
                new BigDecimal("25.0000"),
                "Physical stock count"
        );

        when(stockMovementService.adjustStock(
                tenantId,
                businessId,
                branchId,
                userId,
                request
        )).thenReturn(stockMovementResponse);

        var response = stockMovementController.adjustStock(
                businessId,
                branchId,
                request,
                authentication
        );

        assertNotNull(response);

        verify(stockMovementService).adjustStock(
                tenantId,
                businessId,
                branchId,
                userId,
                request
        );
    }
}
