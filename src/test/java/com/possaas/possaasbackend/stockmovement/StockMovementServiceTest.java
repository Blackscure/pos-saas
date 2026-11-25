package com.possaas.possaasbackend.stockmovement;

import com.possaas.possaasbackend.branch.Branch;
import com.possaas.possaasbackend.branch.BranchRepository;
import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessRepository;
import com.possaas.possaasbackend.inventory.Inventory;
import com.possaas.possaasbackend.inventory.InventoryRepository;
import com.possaas.possaasbackend.stockmovement.dto.StockAdjustmentRequest;
import com.possaas.possaasbackend.stockmovement.dto.StockMovementRequest;
import com.possaas.possaasbackend.stockmovement.dto.StockMovementResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private Business business;

    @Mock
    private Branch branch;

    @Mock
    private Inventory inventory;

    @InjectMocks
    private StockMovementService stockMovementService;

    private UUID tenantId;
    private UUID businessId;
    private UUID branchId;
    private UUID inventoryId;
    private UUID performedBy;

    @BeforeEach
    void setUp() {

        tenantId = UUID.randomUUID();
        businessId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        inventoryId = UUID.randomUUID();
        performedBy = UUID.randomUUID();

        lenient().when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.of(business));

        lenient().when(business.isActive())
                .thenReturn(true);

        lenient().when(branchRepository.findByIdAndBusiness_Id(branchId, businessId))
                .thenReturn(Optional.of(branch));

        lenient().when(branch.isActive())
                .thenReturn(true);

        lenient().when(inventoryRepository.findByIdAndBusinessIdAndBranchId(
                        inventoryId, businessId, branchId))
                .thenReturn(Optional.of(inventory));

        lenient().when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // =========================================================
    // STOCK IN TESTS
    // =========================================================

    @Test
    void stockIn_shouldIncreaseQuantity() {

        when(inventory.getQuantityOnHand())
                .thenReturn(new BigDecimal("20.0000"));

        lenient().when(inventory.getQuantityReserved())
                .thenReturn(new BigDecimal("5.0000"));

        when(inventory.getId())
                .thenReturn(inventoryId);

        StockMovementRequest request =
                new StockMovementRequest(
                        inventoryId,
                        new BigDecimal("10.0000"),
                        "Restocking"
                );

        StockMovementResponse response =
                stockMovementService.stockIn(
                        tenantId,
                        businessId,
                        branchId,
                        performedBy,
                        request
                );

        assertNotNull(response);

        assertEquals(
                new BigDecimal("30.0000"),
                response.quantityAfter()
        );

        assertEquals(
                new BigDecimal("20.0000"),
                response.quantityBefore()
        );

        assertEquals(
                StockMovementType.STOCK_IN,
                response.movementType()
        );

        verify(inventory).setQuantityOnHand(new BigDecimal("30.0000"));

        verify(inventoryRepository).save(inventory);

        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    void stockIn_shouldRejectZeroQuantity() {

        StockMovementRequest request =
                new StockMovementRequest(
                        inventoryId,
                        BigDecimal.ZERO,
                        "Restocking"
                );

        assertThrows(
                RuntimeException.class,
                () -> stockMovementService.stockIn(
                        tenantId,
                        businessId,
                        branchId,
                        performedBy,
                        request
                )
        );

        verifyNoInteractions(inventoryRepository);
        verifyNoInteractions(stockMovementRepository);
    }

    @Test
    void stockIn_shouldRejectNegativeQuantity() {

        StockMovementRequest request =
                new StockMovementRequest(
                        inventoryId,
                        new BigDecimal("-5.0000"),
                        "Invalid stock"
                );

        assertThrows(
                RuntimeException.class,
                () -> stockMovementService.stockIn(
                        tenantId,
                        businessId,
                        branchId,
                        performedBy,
                        request
                )
        );

        verifyNoInteractions(inventoryRepository);
        verifyNoInteractions(stockMovementRepository);
    }

    // =========================================================
    // STOCK OUT TESTS
    // =========================================================

    @Test
    void stockOut_shouldDecreaseQuantity() {

        when(inventory.getQuantityOnHand())
                .thenReturn(new BigDecimal("20.0000"));

        when(inventory.getQuantityReserved())
                .thenReturn(new BigDecimal("3.0000"));

        when(inventory.getId())
                .thenReturn(inventoryId);

        StockMovementRequest request =
                new StockMovementRequest(
                        inventoryId,
                        new BigDecimal("5.0000"),
                        "Damaged stock"
                );

        StockMovementResponse response =
                stockMovementService.stockOut(
                        tenantId,
                        businessId,
                        branchId,
                        performedBy,
                        request
                );

        assertNotNull(response);

        assertEquals(
                new BigDecimal("15.0000"),
                response.quantityAfter()
        );

        assertEquals(
                new BigDecimal("20.0000"),
                response.quantityBefore()
        );

        assertEquals(
                StockMovementType.STOCK_OUT,
                response.movementType()
        );

        verify(inventory).setQuantityOnHand(new BigDecimal("15.0000"));

        verify(inventoryRepository).save(inventory);

        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    void stockOut_shouldRejectInsufficientStock() {

        when(inventory.getQuantityOnHand())
                .thenReturn(new BigDecimal("5.0000"));

        lenient().when(inventory.getQuantityReserved())
                .thenReturn(BigDecimal.ZERO);

        StockMovementRequest request =
                new StockMovementRequest(
                        inventoryId,
                        new BigDecimal("10.0000"),
                        "Stock removal"
                );

        assertThrows(
                RuntimeException.class,
                () -> stockMovementService.stockOut(
                        tenantId,
                        businessId,
                        branchId,
                        performedBy,
                        request
                )
        );

        verify(inventoryRepository, never()).save(any());

        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void stockOut_shouldNotReduceStockBelowReservedQuantity() {

        when(inventory.getQuantityOnHand())
                .thenReturn(new BigDecimal("10.0000"));

        when(inventory.getQuantityReserved())
                .thenReturn(new BigDecimal("7.0000"));

        StockMovementRequest request =
                new StockMovementRequest(
                        inventoryId,
                        new BigDecimal("5.0000"),
                        "Stock removal"
                );

        assertThrows(
                RuntimeException.class,
                () -> stockMovementService.stockOut(
                        tenantId,
                        businessId,
                        branchId,
                        performedBy,
                        request
                )
        );

        verify(inventoryRepository, never()).save(any());

        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void stockOut_shouldRejectNegativeQuantity() {

        StockMovementRequest request =
                new StockMovementRequest(
                        inventoryId,
                        new BigDecimal("-2.0000"),
                        "Invalid stock out"
                );

        assertThrows(
                RuntimeException.class,
                () -> stockMovementService.stockOut(
                        tenantId,
                        businessId,
                        branchId,
                        performedBy,
                        request
                )
        );

        verifyNoInteractions(inventoryRepository);
        verifyNoInteractions(stockMovementRepository);
    }

    // =========================================================
    // STOCK ADJUSTMENT TESTS
    // =========================================================

    @Test
    void adjustStock_shouldIncreaseQuantity() {

        when(inventory.getQuantityOnHand())
                .thenReturn(new BigDecimal("10.0000"));

        when(inventory.getQuantityReserved())
                .thenReturn(new BigDecimal("2.0000"));

        when(inventory.getId())
                .thenReturn(inventoryId);

        StockAdjustmentRequest request =
                new StockAdjustmentRequest(
                        inventoryId,
                        new BigDecimal("15.0000"),
                        "Physical stock count"
                );

        StockMovementResponse response =
                stockMovementService.adjustStock(
                        tenantId,
                        businessId,
                        branchId,
                        performedBy,
                        request
                );

        assertNotNull(response);

        assertEquals(
                new BigDecimal("15.0000"),
                response.quantityAfter()
        );

        assertEquals(
                new BigDecimal("10.0000"),
                response.quantityBefore()
        );

        assertEquals(
                StockMovementType.ADJUSTMENT,
                response.movementType()
        );

        verify(inventory).setQuantityOnHand(new BigDecimal("15.0000"));

        verify(inventoryRepository).save(inventory);

        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    void adjustStock_shouldDecreaseQuantity() {

        when(inventory.getQuantityOnHand())
                .thenReturn(new BigDecimal("20.0000"));

        when(inventory.getQuantityReserved())
                .thenReturn(new BigDecimal("5.0000"));

        when(inventory.getId())
                .thenReturn(inventoryId);

        StockAdjustmentRequest request =
                new StockAdjustmentRequest(
                        inventoryId,
                        new BigDecimal("12.0000"),
                        "Physical stock count"
                );

        StockMovementResponse response =
                stockMovementService.adjustStock(
                        tenantId,
                        businessId,
                        branchId,
                        performedBy,
                        request
                );

        assertNotNull(response);

        assertEquals(
                new BigDecimal("12.0000"),
                response.quantityAfter()
        );

        assertEquals(
                new BigDecimal("20.0000"),
                response.quantityBefore()
        );

        assertEquals(
                StockMovementType.ADJUSTMENT,
                response.movementType()
        );

        verify(inventory).setQuantityOnHand(new BigDecimal("12.0000"));

        verify(inventoryRepository).save(inventory);

        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    void adjustStock_shouldRejectQuantityBelowReserved() {

        when(inventory.getQuantityOnHand())
                .thenReturn(new BigDecimal("10.0000"));

        when(inventory.getQuantityReserved())
                .thenReturn(new BigDecimal("8.0000"));

        StockAdjustmentRequest request =
                new StockAdjustmentRequest(
                        inventoryId,
                        new BigDecimal("5.0000"),
                        "Physical stock count"
                );

        assertThrows(
                RuntimeException.class,
                () -> stockMovementService.adjustStock(
                        tenantId,
                        businessId,
                        branchId,
                        performedBy,
                        request
                )
        );

        verify(inventoryRepository, never()).save(any());

        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void adjustStock_shouldRejectUnchangedQuantity() {

        when(inventory.getQuantityOnHand())
                .thenReturn(new BigDecimal("10.0000"));

        when(inventory.getQuantityReserved())
                .thenReturn(new BigDecimal("2.0000"));

        StockAdjustmentRequest request =
                new StockAdjustmentRequest(
                        inventoryId,
                        new BigDecimal("10.0000"),
                        "Physical stock count"
                );

        assertThrows(
                RuntimeException.class,
                () -> stockMovementService.adjustStock(
                        tenantId,
                        businessId,
                        branchId,
                        performedBy,
                        request
                )
        );

        verify(inventoryRepository, never()).save(any());

        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void adjustStock_shouldRejectNegativeQuantity() {

        StockAdjustmentRequest request =
                new StockAdjustmentRequest(
                        inventoryId,
                        new BigDecimal("-5.0000"),
                        "Invalid adjustment"
                );

        assertThrows(
                RuntimeException.class,
                () -> stockMovementService.adjustStock(
                        tenantId,
                        businessId,
                        branchId,
                        performedBy,
                        request
                )
        );

        verifyNoInteractions(inventoryRepository);
        verifyNoInteractions(stockMovementRepository);
    }

    // =========================================================
    // INVENTORY VALIDATION TESTS
    // =========================================================

    @Test
    void stockIn_shouldThrowWhenInventoryDoesNotExist() {

        when(inventoryRepository.findByIdAndBusinessIdAndBranchId(
                inventoryId,
                businessId,
                branchId
        )).thenReturn(Optional.empty());

        StockMovementRequest request =
                new StockMovementRequest(
                        inventoryId,
                        new BigDecimal("5.0000"),
                        "Restocking"
                );

        assertThrows(
                RuntimeException.class,
                () -> stockMovementService.stockIn(
                        tenantId,
                        businessId,
                        branchId,
                        performedBy,
                        request
                )
        );

        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void stockIn_shouldThrowWhenBusinessIsInactive() {

        when(business.isActive())
                .thenReturn(false);

        StockMovementRequest request =
                new StockMovementRequest(
                        inventoryId,
                        new BigDecimal("5.0000"),
                        "Restocking"
                );

        assertThrows(
                RuntimeException.class,
                () -> stockMovementService.stockIn(
                        tenantId,
                        businessId,
                        branchId,
                        performedBy,
                        request
                )
        );

        verifyNoInteractions(inventoryRepository);
        verifyNoInteractions(stockMovementRepository);
    }

    @Test
    void stockIn_shouldThrowWhenBranchIsInactive() {

        when(branch.isActive())
                .thenReturn(false);

        StockMovementRequest request =
                new StockMovementRequest(
                        inventoryId,
                        new BigDecimal("5.0000"),
                        "Restocking"
                );

        assertThrows(
                RuntimeException.class,
                () -> stockMovementService.stockIn(
                        tenantId,
                        businessId,
                        branchId,
                        performedBy,
                        request
                )
        );

        verifyNoInteractions(inventoryRepository);
        verifyNoInteractions(stockMovementRepository);
    }
}
