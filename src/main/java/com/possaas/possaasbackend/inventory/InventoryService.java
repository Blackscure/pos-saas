
package com.possaas.possaasbackend.inventory;

import com.possaas.possaasbackend.branch.Branch;
import com.possaas.possaasbackend.branch.BranchRepository;
import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessRepository;
import com.possaas.possaasbackend.inventory.dto.CreateInventoryRequest;
import com.possaas.possaasbackend.inventory.dto.InventoryResponse;
import com.possaas.possaasbackend.inventory.dto.UpdateInventoryRequest;
import com.possaas.possaasbackend.product.Product;
import com.possaas.possaasbackend.product.ProductRepository;
import com.possaas.possaasbackend.productvariant.ProductVariantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final BusinessRepository businessRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            BusinessRepository businessRepository,
            BranchRepository branchRepository,
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.businessRepository = businessRepository;
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    // =========================================================
    // CREATE INVENTORY RECORD
    // =========================================================

    public InventoryResponse createInventory(
            UUID tenantId,
            UUID businessId,
            UUID branchId,
            CreateInventoryRequest request
    ) {

        Business business = getBusinessByTenant(tenantId, businessId);
        validateBusinessIsActive(business);

        Branch branch = getBranchByBusiness(branchId, businessId);
        validateBranchIsActive(branch);

        Product product = getProductByBusiness(request.productId(), businessId);
        validateProductIsActive(product);

        if (request.productVariantId() != null) {
            validateProductVariant(
                    request.productVariantId(),
                    businessId,
                    request.productId()
            );
        }

        boolean inventoryExists = inventoryRepository
                .findByBusinessIdAndBranchIdAndProductIdAndProductVariantId(
                        businessId,
                        branchId,
                        request.productId(),
                        request.productVariantId()
                )
                .isPresent();

        if (inventoryExists) {
            throw new IllegalArgumentException(
                    "Inventory already exists for this product in this branch"
            );
        }

        Inventory inventory = new Inventory();

        inventory.setBusinessId(businessId);
        inventory.setBranchId(branchId);
        inventory.setProductId(request.productId());
        inventory.setProductVariantId(request.productVariantId());
        inventory.setQuantityOnHand(java.math.BigDecimal.ZERO);
        inventory.setQuantityReserved(java.math.BigDecimal.ZERO);
        inventory.setReorderLevel(request.reorderLevel());

        Inventory savedInventory = inventoryRepository.save(inventory);

        return InventoryResponse.from(savedInventory);
    }

    // =========================================================
    // GET INVENTORY BY BRANCH
    // =========================================================

    @Transactional(readOnly = true)
    public List<InventoryResponse> getBranchInventory(
            UUID tenantId,
            UUID businessId,
            UUID branchId
    ) {

        Business business = getBusinessByTenant(tenantId, businessId);
        validateBusinessIsActive(business);

        Branch branch = getBranchByBusiness(branchId, businessId);
        validateBranchIsActive(branch);

        return inventoryRepository
                .findByBusinessIdAndBranchId(businessId, branchId)
                .stream()
                .map(InventoryResponse::from)
                .toList();
    }

    // =========================================================
    // GET INVENTORY BY PRODUCT
    // =========================================================

    @Transactional(readOnly = true)
    public List<InventoryResponse> getProductInventory(
            UUID tenantId,
            UUID businessId,
            UUID branchId,
            UUID productId
    ) {

        Business business = getBusinessByTenant(tenantId, businessId);
        validateBusinessIsActive(business);

        Branch branch = getBranchByBusiness(branchId, businessId);
        validateBranchIsActive(branch);

        Product product = getProductByBusiness(productId, businessId);
        validateProductIsActive(product);

        return inventoryRepository
                .findByBusinessIdAndBranchIdAndProductId(
                        businessId,
                        branchId,
                        productId
                )
                .stream()
                .map(InventoryResponse::from)
                .toList();
    }

    // =========================================================
    // GET INVENTORY BY ID
    // =========================================================

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(
            UUID tenantId,
            UUID businessId,
            UUID inventoryId
    ) {

        Business business = getBusinessByTenant(tenantId, businessId);
        validateBusinessIsActive(business);

        Inventory inventory = inventoryRepository
                .findByIdAndBusinessId(inventoryId, businessId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Inventory not found"
                ));

        return InventoryResponse.from(inventory);
    }

    // =========================================================
    // GET ALL BUSINESS INVENTORY
    // =========================================================

    @Transactional(readOnly = true)
    public List<InventoryResponse> getBusinessInventory(
            UUID tenantId,
            UUID businessId
    ) {

        Business business = getBusinessByTenant(tenantId, businessId);
        validateBusinessIsActive(business);

        return inventoryRepository
                .findByBusinessId(businessId)
                .stream()
                .map(InventoryResponse::from)
                .toList();
    }

    // =========================================================
    // UPDATE REORDER LEVEL
    // =========================================================

    public InventoryResponse updateReorderLevel(
            UUID tenantId,
            UUID businessId,
            UUID inventoryId,
            UpdateInventoryRequest request
    ) {

        Business business = getBusinessByTenant(tenantId, businessId);
        validateBusinessIsActive(business);

        Inventory inventory = inventoryRepository
                .findByIdAndBusinessId(inventoryId, businessId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Inventory not found"
                ));

        inventory.setReorderLevel(request.reorderLevel());

        Inventory updatedInventory = inventoryRepository.save(inventory);

        return InventoryResponse.from(updatedInventory);
    }

    // =========================================================
    // VALIDATION HELPERS
    // =========================================================

    private Business getBusinessByTenant(UUID tenantId, UUID businessId) {

        return businessRepository
                .findByIdAndTenantId(businessId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Business not found"
                ));
    }

    private void validateBusinessIsActive(Business business) {

        if (!business.isActive()) {
            throw new IllegalArgumentException(
                    "Business is inactive"
            );
        }
    }

    private Branch getBranchByBusiness(UUID branchId, UUID businessId) {

        return branchRepository
                .findByIdAndBusiness_Id(branchId, businessId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Branch not found"
                ));
    }

    private void validateBranchIsActive(Branch branch) {

        if (!branch.isActive()) {
            throw new IllegalArgumentException(
                    "Branch is inactive"
            );
        }
    }

    private Product getProductByBusiness(UUID productId, UUID businessId) {

        return productRepository
                .findByIdAndBusiness_Id(productId, businessId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Product not found"
                ));
    }

    private void validateProductIsActive(Product product) {

        if (!product.isActive()) {
            throw new IllegalArgumentException(
                    "Product is inactive"
            );
        }
    }

    private void validateProductVariant(
            UUID variantId,
            UUID businessId,
            UUID productId
    ) {

        boolean validVariant = productVariantRepository
                .existsByIdAndBusinessIdAndProductIdAndActiveTrue(
                        variantId,
                        businessId,
                        productId
                );

        if (!validVariant) {
            throw new EntityNotFoundException(
                    "Active product variant not found for this product"
            );
        }
    }
}
