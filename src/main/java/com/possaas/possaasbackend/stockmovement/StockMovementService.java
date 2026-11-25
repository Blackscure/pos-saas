package com.possaas.possaasbackend.stockmovement;

import com.possaas.possaasbackend.branch.Branch;
import com.possaas.possaasbackend.branch.BranchRepository;
import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessRepository;
import com.possaas.possaasbackend.common.pagination.PageResponse;
import com.possaas.possaasbackend.inventory.Inventory;
import com.possaas.possaasbackend.inventory.InventoryRepository;
import com.possaas.possaasbackend.product.Product;
import com.possaas.possaasbackend.product.ProductRepository;
import com.possaas.possaasbackend.productvariant.ProductVariant;
import com.possaas.possaasbackend.productvariant.ProductVariantRepository;
import com.possaas.possaasbackend.stockmovement.dto.StockAdjustmentRequest;
import com.possaas.possaasbackend.stockmovement.dto.StockMovementRequest;
import com.possaas.possaasbackend.stockmovement.dto.StockMovementResponse;
import com.possaas.possaasbackend.tenant.Tenant;
import com.possaas.possaasbackend.tenant.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final InventoryRepository inventoryRepository;
    private final BusinessRepository businessRepository;
    private final BranchRepository branchRepository;
    private final TenantRepository tenantRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public StockMovementService(
            StockMovementRepository stockMovementRepository,
            InventoryRepository inventoryRepository,
            BusinessRepository businessRepository,
            BranchRepository branchRepository,
            TenantRepository tenantRepository,
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository
    ) {
        this.stockMovementRepository = stockMovementRepository;
        this.inventoryRepository = inventoryRepository;
        this.businessRepository = businessRepository;
        this.branchRepository = branchRepository;
        this.tenantRepository = tenantRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
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

        ValidatedBusinessAndBranch context = validateBusinessAndBranch(
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

        return toResponse(
                stockMovementRepository.save(movement),
                context.business(),
                context.branch(),
                inventory
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

        ValidatedBusinessAndBranch context = validateBusinessAndBranch(
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

        return toResponse(
                stockMovementRepository.save(movement),
                context.business(),
                context.branch(),
                inventory
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

        ValidatedBusinessAndBranch context = validateBusinessAndBranch(
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

        return toResponse(
                stockMovementRepository.save(movement),
                context.business(),
                context.branch(),
                inventory
        );
    }

    // =========================================================
    // BRANCH STOCK MOVEMENT HISTORY
    // =========================================================

    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> getBranchMovements(
            UUID tenantId,
            UUID businessId,
            UUID branchId,
            Pageable pageable
    ) {

        validateBusinessAndBranch(
                tenantId,
                businessId,
                branchId
        );

        Page<StockMovement> movements =
                stockMovementRepository
                        .findByTenantIdAndBusinessIdAndBranchIdOrderByCreatedAtDesc(
                                tenantId,
                                businessId,
                                branchId,
                                pageable
                        );

        return PageResponse.of(
                movements,
                toResponses(movements.getContent())
        );
    }

    // =========================================================
    // INVENTORY STOCK MOVEMENT HISTORY
    // =========================================================

    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> getInventoryMovements(
            UUID tenantId,
            UUID businessId,
            UUID inventoryId,
            Pageable pageable
    ) {

        validateBusiness(
                tenantId,
                businessId
        );

        // Verify that the inventory record belongs to this business.
        inventoryRepository
                .findByIdAndBusinessId(
                        inventoryId,
                        businessId
                )
                .orElseThrow(() -> new EntityNotFoundException(
                        "Inventory not found for this business"
                ));

        Page<StockMovement> movements =
                stockMovementRepository
                        .findByTenantIdAndBusinessIdAndInventoryIdOrderByCreatedAtDesc(
                                tenantId,
                                businessId,
                                inventoryId,
                                pageable
                        );

        return PageResponse.of(
                movements,
                toResponses(movements.getContent())
        );
    }

    // =========================================================
    // GET SINGLE STOCK MOVEMENT
    // =========================================================

    @Transactional(readOnly = true)
    public StockMovementResponse getMovementById(
            UUID tenantId,
            UUID businessId,
            UUID branchId,
            UUID movementId
    ) {

        ValidatedBusinessAndBranch context = validateBusinessAndBranch(
                tenantId,
                businessId,
                branchId
        );

        StockMovement movement = stockMovementRepository
                .findById(movementId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Stock movement not found"
                ));

        // Scope the movement to the authenticated tenant,
        // business, and branch.
        if (!movement.getTenantId().equals(tenantId)
                || !movement.getBusinessId().equals(businessId)
                || !movement.getBranchId().equals(branchId)) {

            throw new EntityNotFoundException(
                    "Stock movement not found"
            );
        }

        Inventory inventory = movement.getInventoryId() != null
                ? inventoryRepository.findById(movement.getInventoryId()).orElse(null)
                : null;

        return toResponse(
                movement,
                context.business(),
                context.branch(),
                inventory
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

    private ValidatedBusinessAndBranch validateBusinessAndBranch(
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

        return new ValidatedBusinessAndBranch(business, branch);
    }

    // =========================================================
    // RESPONSE MAPPING
    // =========================================================

    private StockMovementResponse toResponse(
            StockMovement movement,
            Business business,
            Branch branch,
            Inventory inventory
    ) {

        Tenant tenant = business != null && business.getTenant() != null
                ? business.getTenant()
                : (movement.getTenantId() != null
                        ? tenantRepository.findById(movement.getTenantId()).orElse(null)
                        : null);

        Product product = movement.getProductId() != null
                ? productRepository.findById(movement.getProductId()).orElse(null)
                : null;

        ProductVariant productVariant = movement.getProductVariantId() != null
                ? productVariantRepository.findById(movement.getProductVariantId()).orElse(null)
                : null;

        return StockMovementResponse.from(
                movement,
                tenant,
                business,
                branch,
                inventory,
                product,
                productVariant
        );
    }

    private List<StockMovementResponse> toResponses(List<StockMovement> movements) {

        if (movements.isEmpty()) {
            return List.of();
        }

        Set<UUID> tenantIds = collectIds(movements, StockMovement::getTenantId);
        Set<UUID> businessIds = collectIds(movements, StockMovement::getBusinessId);
        Set<UUID> branchIds = collectIds(movements, StockMovement::getBranchId);
        Set<UUID> inventoryIds = collectIds(movements, StockMovement::getInventoryId);
        Set<UUID> productIds = collectIds(movements, StockMovement::getProductId);
        Set<UUID> variantIds = collectIds(movements, StockMovement::getProductVariantId);

        Map<UUID, Tenant> tenantsById = tenantIds.isEmpty()
                ? Collections.emptyMap()
                : tenantRepository.findAllById(tenantIds).stream()
                        .collect(Collectors.toMap(Tenant::getId, Function.identity()));

        Map<UUID, Business> businessesById = businessIds.isEmpty()
                ? Collections.emptyMap()
                : businessRepository.findAllById(businessIds).stream()
                        .collect(Collectors.toMap(Business::getId, Function.identity()));

        Map<UUID, Branch> branchesById = branchIds.isEmpty()
                ? Collections.emptyMap()
                : branchRepository.findAllById(branchIds).stream()
                        .collect(Collectors.toMap(Branch::getId, Function.identity()));

        Map<UUID, Inventory> inventoriesById = inventoryIds.isEmpty()
                ? Collections.emptyMap()
                : inventoryRepository.findAllById(inventoryIds).stream()
                        .collect(Collectors.toMap(Inventory::getId, Function.identity()));

        Map<UUID, Product> productsById = productIds.isEmpty()
                ? Collections.emptyMap()
                : productRepository.findAllById(productIds).stream()
                        .collect(Collectors.toMap(Product::getId, Function.identity()));

        Map<UUID, ProductVariant> variantsById = variantIds.isEmpty()
                ? Collections.emptyMap()
                : productVariantRepository.findAllById(variantIds).stream()
                        .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));

        return movements.stream()
                .map(movement -> StockMovementResponse.from(
                        movement,
                        tenantsById.get(movement.getTenantId()),
                        businessesById.get(movement.getBusinessId()),
                        branchesById.get(movement.getBranchId()),
                        inventoriesById.get(movement.getInventoryId()),
                        productsById.get(movement.getProductId()),
                        variantsById.get(movement.getProductVariantId())
                ))
                .toList();
    }

    private Set<UUID> collectIds(
            List<StockMovement> movements,
            Function<StockMovement, UUID> idExtractor
    ) {
        return movements.stream()
                .map(idExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private record ValidatedBusinessAndBranch(
            Business business,
            Branch branch
    ) {
    }

    // =========================================================
    // BUSINESS VALIDATION
    // =========================================================

    private void validateBusiness(
            UUID tenantId,
            UUID businessId
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
    // =========================================================
// BRANCH STOCK MOVEMENT HISTORY
// =========================================================

    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> getBranchMovementHistory(
            UUID tenantId,
            UUID businessId,
            UUID branchId,
            int page,
            int size
    ) {

        validateBusinessAndBranch(
                tenantId,
                businessId,
                branchId
        );

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<StockMovement> movements =
                stockMovementRepository
                        .findByTenantIdAndBusinessIdAndBranchIdOrderByCreatedAtDesc(
                                tenantId,
                                businessId,
                                branchId,
                                pageable
                        );

        return PageResponse.of(
                movements,
                toResponses(movements.getContent())
        );
    }
    // =========================================================
// INVENTORY MOVEMENT HISTORY
// =========================================================

    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> getInventoryMovementHistory(
            UUID tenantId,
            UUID businessId,
            UUID inventoryId,
            int page,
            int size
    ) {

        validateBusiness(
                tenantId,
                businessId
        );

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        inventoryRepository
                .findByIdAndBusinessId(
                        inventoryId,
                        businessId
                )
                .orElseThrow(() -> new EntityNotFoundException(
                        "Inventory not found for this business"
                ));

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<StockMovement> movements =
                stockMovementRepository
                        .findByTenantIdAndBusinessIdAndInventoryIdOrderByCreatedAtDesc(
                                tenantId,
                                businessId,
                                inventoryId,
                                pageable
                        );

        return PageResponse.of(
                movements,
                toResponses(movements.getContent())
        );
    }
}
