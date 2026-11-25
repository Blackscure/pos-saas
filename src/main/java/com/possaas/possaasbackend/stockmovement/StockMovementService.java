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
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final InventoryRepository inventoryRepository;
    private final BusinessRepository businessRepository;
    private final BranchRepository branchRepository;

    public StockMovementService(
            StockMovementRepository stockMovementRepository,
            InventoryRepository inventoryRepository,
            BusinessRepository businessRepository,
            BranchRepository branchRepository
    ) {
        this.stockMovementRepository = stockMovementRepository;
        this.inventoryRepository = inventoryRepository;
        this.businessRepository = businessRepository;
        this.branchRepository = branchRepository;
    }

    // =========================================================
    // STOCK IN
    // =========================================================

    public StockMovementResponse stockIn(
            UUID tenantId,
            UUID businessId,
            UUID branchId,
            UUID performedBy,
            StockMovementRequest request
    ) {

        validateRequestQuantity(request.quantity());

        validateBusinessAndBranch(
                tenantId,
                businessId,
                branchId
        );

        Inventory inventory = getLockedInventory(
                businessId,
                branchId,
                request.inventoryId()
        );

        BigDecimal quantityBefore = inventory.getQuantityOnHand();

        BigDecimal quantityAfter = quantityBefore.add(
                request.quantity()
        );

        inventory.setQuantityOnHand(quantityAfter);

        inventoryRepository.save(inventory);

        StockMovement movement = createMovement(
                tenantId,
                inventory,
                StockMovementType.STOCK_IN,
                request.quantity(),
                quantityBefore,
                quantityAfter,
                null,
                null,
                request.reason(),
                performedBy
        );

        return StockMovementResponse.from(
                stockMovementRepository.save(movement)
        );
    }

    // =========================================================
    // STOCK OUT
    // =========================================================

    public StockMovementResponse stockOut(
            UUID tenantId,
            UUID businessId,
            UUID branchId,
            UUID performedBy,
            StockMovementRequest request
    ) {

        validateRequestQuantity(request.quantity());

        validateBusinessAndBranch(
                tenantId,
                businessId,
                branchId
        );

        Inventory inventory = getLockedInventory(
                businessId,
                branchId,
                request.inventoryId()
        );

        BigDecimal quantityBefore = inventory.getQuantityOnHand();

        BigDecimal quantityAfter = quantityBefore.subtract(
                request.quantity()
        );

        if (quantityAfter.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient stock. Available quantity: " + quantityBefore
            );
        }

        if (quantityAfter.compareTo(inventory.getQuantityReserved()) < 0) {
            throw new IllegalArgumentException(
                    "Stock out cannot reduce inventory below reserved quantity. "
                            + "Reserved quantity: " + inventory.getQuantityReserved()
            );
        }

        inventory.setQuantityOnHand(quantityAfter);

        inventoryRepository.save(inventory);

        StockMovement movement = createMovement(
                tenantId,
                inventory,
                StockMovementType.STOCK_OUT,
                request.quantity(),
                quantityBefore,
                quantityAfter,
                null,
                null,
                request.reason(),
                performedBy
        );

        return StockMovementResponse.from(
                stockMovementRepository.save(movement)
        );
    }

    // =========================================================
    // STOCK ADJUSTMENT
    // =========================================================

    public StockMovementResponse adjustStock(
            UUID tenantId,
            UUID businessId,
            UUID branchId,
            UUID performedBy,
            StockAdjustmentRequest request
    ) {

        validateAdjustmentQuantity(request.quantity());

        validateBusinessAndBranch(
                tenantId,
                businessId,
                branchId
        );

        Inventory inventory = getLockedInventory(
                businessId,
                branchId,
                request.inventoryId()
        );

        BigDecimal quantityBefore = inventory.getQuantityOnHand();

        BigDecimal quantityAfter = request.quantity();

        if (quantityAfter.compareTo(inventory.getQuantityReserved()) < 0) {
            throw new IllegalArgumentException(
                    "Actual stock cannot be less than reserved quantity. "
                            + "Reserved quantity: " + inventory.getQuantityReserved()
            );
        }

        BigDecimal difference = quantityAfter.subtract(quantityBefore);

        if (difference.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException(
                    "Stock adjustment is unnecessary. Stock quantity has not changed."
            );
        }

        inventory.setQuantityOnHand(quantityAfter);

        inventoryRepository.save(inventory);

        StockMovement movement = createMovement(
                tenantId,
                inventory,
                StockMovementType.ADJUSTMENT,
                difference.abs(),
                quantityBefore,
                quantityAfter,
                null,
                null,
                request.reason(),
                performedBy
        );

        return StockMovementResponse.from(
                stockMovementRepository.save(movement)
        );
    }

    // =========================================================
    // LOCK INVENTORY
    // =========================================================

    private Inventory getLockedInventory(
            UUID businessId,
            UUID branchId,
            UUID inventoryId
    ) {

        return inventoryRepository
                .findByIdAndBusinessIdAndBranchId(
                        inventoryId,
                        businessId,
                        branchId
                )
                .orElseThrow(() -> new EntityNotFoundException(
                        "Inventory not found for this business and branch"
                ));
    }

    // =========================================================
    // BUSINESS AND BRANCH VALIDATION
    // =========================================================

    private void validateBusinessAndBranch(
            UUID tenantId,
            UUID businessId,
            UUID branchId
    ) {

        Business business = businessRepository
                .findByIdAndTenantId(businessId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Business not found"
                ));

        if (!business.isActive()) {
            throw new IllegalArgumentException(
                    "Business is inactive"
            );
        }

        Branch branch = branchRepository
                .findByIdAndBusiness_Id(branchId, businessId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Branch not found for this business"
                ));

        if (!branch.isActive()) {
            throw new IllegalArgumentException(
                    "Branch is inactive"
            );
        }
    }

    // =========================================================
    // REQUEST VALIDATION
    // =========================================================

    private void validateRequestQuantity(BigDecimal quantity) {

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }
    }

    private void validateAdjustmentQuantity(BigDecimal quantity) {

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Actual stock quantity cannot be negative"
            );
        }
    }

    // =========================================================
    // CREATE STOCK MOVEMENT
    // =========================================================

    private StockMovement createMovement(
            UUID tenantId,
            Inventory inventory,
            StockMovementType movementType,
            BigDecimal quantity,
            BigDecimal quantityBefore,
            BigDecimal quantityAfter,
            String referenceType,
            UUID referenceId,
            String reason,
            UUID performedBy
    ) {

        return new StockMovement(
                tenantId,
                inventory.getBusinessId(),
                inventory.getBranchId(),
                inventory.getId(),
                inventory.getProductId(),
                inventory.getProductVariantId(),
                movementType,
                quantity,
                quantityBefore,
                quantityAfter,
                referenceType,
                referenceId,
                reason,
                performedBy
        );
    }
}