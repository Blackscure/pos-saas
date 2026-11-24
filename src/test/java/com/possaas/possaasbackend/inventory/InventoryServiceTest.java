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
import com.possaas.possaasbackend.productvariant.ProductVariant;
import com.possaas.possaasbackend.productvariant.ProductVariantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Inventory Service Tests")
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private UUID tenantId;
    private UUID businessId;
    private UUID branchId;
    private UUID productId;
    private UUID variantId;
    private UUID inventoryId;

    private Business business;
    private Branch branch;
    private Product product;
    private ProductVariant variant;
    private Inventory inventory;

    @BeforeEach
    void setUp() {

        tenantId = UUID.randomUUID();
        businessId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        productId = UUID.randomUUID();
        variantId = UUID.randomUUID();
        inventoryId = UUID.randomUUID();

        business = new Business();
        business.setId(businessId);
        business.setName("Test Business");
        business.setActive(true);

        branch = new Branch();
        branch.setName("Main Branch");
        branch.setCode("MAIN");
        branch.setActive(true);
        branch.setBusiness(business);
        ReflectionTestUtils.setField(branch, "id", branchId);

        product = new Product();
        product.setName("Test Product");
        product.setSku("SKU-001");
        product.setBarcode("123456789");
        product.setSellingPrice(new BigDecimal("100.00"));
        product.setActive(true);
        product.setBusiness(business);
        ReflectionTestUtils.setField(product, "id", productId);

        variant = new ProductVariant();
        variant.setId(variantId);
        variant.setBusinessId(businessId);
        variant.setProductId(productId);
        variant.setName("Red - Large");
        variant.setSku("SKU-001-RED-L");
        variant.setBarcode("123456789001");
        variant.setSellingPrice(new BigDecimal("120.00"));
        variant.setActive(true);

        inventory = new Inventory();
        ReflectionTestUtils.setField(inventory, "id", inventoryId);
        inventory.setBusinessId(businessId);
        inventory.setBranchId(branchId);
        inventory.setProductId(productId);
        inventory.setQuantityOnHand(BigDecimal.ZERO);
        inventory.setQuantityReserved(BigDecimal.ZERO);
        inventory.setReorderLevel(new BigDecimal("10.00"));

        ReflectionTestUtils.setField(
                inventory,
                "createdAt",
                java.time.LocalDateTime.now()
        );

        ReflectionTestUtils.setField(
                inventory,
                "updatedAt",
                java.time.LocalDateTime.now()
        );
    }

    // =========================================================
    // CREATE INVENTORY
    // =========================================================

    @Nested
    @DisplayName("Create Inventory")
    class CreateInventoryTests {

        @Test
        @DisplayName("Should create inventory for product without variant")
        void shouldCreateInventoryForProductWithoutVariant() {

            mockBusiness();
            mockBranch();
            mockProduct();

            CreateInventoryRequest request = new CreateInventoryRequest(
                    productId,
                    null,
                    new BigDecimal("10.00")
            );

            when(inventoryRepository
                    .findByBusinessIdAndBranchIdAndProductIdAndProductVariantId(
                            businessId,
                            branchId,
                            productId,
                            null
                    ))
                    .thenReturn(Optional.empty());

            when(inventoryRepository.save(any(Inventory.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            InventoryResponse response = inventoryService.createInventory(
                    tenantId,
                    businessId,
                    branchId,
                    request
            );

            assertNotNull(response);

            verify(inventoryRepository).save(any(Inventory.class));
        }

        @Test
        @DisplayName("Should create inventory for product variant")
        void shouldCreateInventoryForProductVariant() {

            mockBusiness();
            mockBranch();
            mockProduct();

            CreateInventoryRequest request = new CreateInventoryRequest(
                    productId,
                    variantId,
                    new BigDecimal("5.00")
            );

            when(productVariantRepository
                    .existsByIdAndBusinessIdAndProductIdAndActiveTrue(
                            variantId,
                            businessId,
                            productId
                    ))
                    .thenReturn(true);

            when(productVariantRepository
                    .findByIdAndBusinessIdAndProductId(
                            variantId,
                            businessId,
                            productId
                    ))
                    .thenReturn(Optional.of(variant));

            when(inventoryRepository
                    .findByBusinessIdAndBranchIdAndProductIdAndProductVariantId(
                            businessId,
                            branchId,
                            productId,
                            variantId
                    ))
                    .thenReturn(Optional.empty());

            when(inventoryRepository.save(any(Inventory.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            InventoryResponse response = inventoryService.createInventory(
                    tenantId,
                    businessId,
                    branchId,
                    request
            );

            assertNotNull(response);

            verify(productVariantRepository)
                    .existsByIdAndBusinessIdAndProductIdAndActiveTrue(
                            variantId,
                            businessId,
                            productId
                    );

            verify(inventoryRepository).save(any(Inventory.class));
        }

        @Test
        @DisplayName("Should reject duplicate inventory")
        void shouldRejectDuplicateInventory() {

            mockBusiness();
            mockBranch();
            mockProduct();

            CreateInventoryRequest request = new CreateInventoryRequest(
                    productId,
                    null,
                    new BigDecimal("10.00")
            );

            when(inventoryRepository
                    .findByBusinessIdAndBranchIdAndProductIdAndProductVariantId(
                            businessId,
                            branchId,
                            productId,
                            null
                    ))
                    .thenReturn(Optional.of(inventory));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> inventoryService.createInventory(
                            tenantId,
                            businessId,
                            branchId,
                            request
                    )
            );

            assertEquals(
                    "Inventory already exists for this product in this branch",
                    exception.getMessage()
            );

            verify(inventoryRepository, never()).save(any(Inventory.class));
        }

        @Test
        @DisplayName("Should reject inactive business")
        void shouldRejectInactiveBusiness() {

            business.setActive(false);
            mockBusiness();

            CreateInventoryRequest request = new CreateInventoryRequest(
                    productId,
                    null,
                    new BigDecimal("10.00")
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> inventoryService.createInventory(
                            tenantId,
                            businessId,
                            branchId,
                            request
                    )
            );

            assertEquals("Business is inactive", exception.getMessage());

            verify(branchRepository, never())
                    .findByIdAndBusiness_Id(any(), any());
        }

        @Test
        @DisplayName("Should reject inactive branch")
        void shouldRejectInactiveBranch() {

            mockBusiness();
            branch.setActive(false);
            mockBranch();

            CreateInventoryRequest request = new CreateInventoryRequest(
                    productId,
                    null,
                    new BigDecimal("10.00")
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> inventoryService.createInventory(
                            tenantId,
                            businessId,
                            branchId,
                            request
                    )
            );

            assertEquals("Branch is inactive", exception.getMessage());

            verify(productRepository, never())
                    .findByIdAndBusiness_Id(any(), any());
        }

        @Test
        @DisplayName("Should reject inactive product")
        void shouldRejectInactiveProduct() {

            mockBusiness();
            mockBranch();

            product.setActive(false);
            mockProduct();

            CreateInventoryRequest request = new CreateInventoryRequest(
                    productId,
                    null,
                    new BigDecimal("10.00")
            );

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> inventoryService.createInventory(
                            tenantId,
                            businessId,
                            branchId,
                            request
                    )
            );

            assertEquals("Product is inactive", exception.getMessage());

            verify(inventoryRepository, never()).save(any(Inventory.class));
        }

        @Test
        @DisplayName("Should reject invalid product variant")
        void shouldRejectInvalidProductVariant() {

            mockBusiness();
            mockBranch();
            mockProduct();

            CreateInventoryRequest request = new CreateInventoryRequest(
                    productId,
                    variantId,
                    new BigDecimal("10.00")
            );

            when(productVariantRepository
                    .existsByIdAndBusinessIdAndProductIdAndActiveTrue(
                            variantId,
                            businessId,
                            productId
                    ))
                    .thenReturn(false);

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> inventoryService.createInventory(
                            tenantId,
                            businessId,
                            branchId,
                            request
                    )
            );

            assertEquals(
                    "Active product variant not found for this product",
                    exception.getMessage()
            );

            verify(inventoryRepository, never()).save(any(Inventory.class));
        }

        @Test
        @DisplayName("Should reject nonexistent business")
        void shouldRejectNonexistentBusiness() {

            when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                    .thenReturn(Optional.empty());

            CreateInventoryRequest request = new CreateInventoryRequest(
                    productId,
                    null,
                    new BigDecimal("10.00")
            );

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> inventoryService.createInventory(
                            tenantId,
                            businessId,
                            branchId,
                            request
                    )
            );

            assertEquals("Business not found", exception.getMessage());
        }
    }

    // =========================================================
    // GET INVENTORY
    // =========================================================

    @Nested
    @DisplayName("Get Inventory")
    class GetInventoryTests {

        @Test
        @DisplayName("Should get inventory for branch")
        void shouldGetBranchInventory() {

            mockBusiness();
            mockBranch();

            when(inventoryRepository.findByBusinessIdAndBranchId(
                    businessId,
                    branchId
            )).thenReturn(List.of());

            List<InventoryResponse> response =
                    inventoryService.getBranchInventory(
                            tenantId,
                            businessId,
                            branchId
                    );

            assertNotNull(response);
            assertTrue(response.isEmpty());

            verify(inventoryRepository)
                    .findByBusinessIdAndBranchId(businessId, branchId);
        }

        @Test
        @DisplayName("Should get inventory for product")
        void shouldGetProductInventory() {

            mockBusiness();
            mockBranch();
            mockProduct();

            when(inventoryRepository
                    .findByBusinessIdAndBranchIdAndProductId(
                            businessId,
                            branchId,
                            productId
                    ))
                    .thenReturn(List.of());

            List<InventoryResponse> response =
                    inventoryService.getProductInventory(
                            tenantId,
                            businessId,
                            branchId,
                            productId
                    );

            assertNotNull(response);
            assertTrue(response.isEmpty());

            verify(inventoryRepository)
                    .findByBusinessIdAndBranchIdAndProductId(
                            businessId,
                            branchId,
                            productId
                    );
        }

        @Test
        @DisplayName("Should get inventory by ID")
        void shouldGetInventoryById() {

            mockBusiness();
            mockInventoryById();
            mockResponseMapping();

            InventoryResponse response =
                    inventoryService.getInventoryById(
                            tenantId,
                            businessId,
                            inventoryId
                    );

            assertNotNull(response);

            verify(inventoryRepository)
                    .findByIdAndBusinessId(inventoryId, businessId);
        }

        @Test
        @DisplayName("Should throw exception when inventory ID does not exist")
        void shouldThrowExceptionWhenInventoryDoesNotExist() {

            mockBusiness();

            when(inventoryRepository.findByIdAndBusinessId(
                    inventoryId,
                    businessId
            )).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> inventoryService.getInventoryById(
                            tenantId,
                            businessId,
                            inventoryId
                    )
            );

            assertEquals("Inventory not found", exception.getMessage());
        }

        @Test
        @DisplayName("Should get all inventory for business")
        void shouldGetBusinessInventory() {

            mockBusiness();

            when(inventoryRepository.findByBusinessId(businessId))
                    .thenReturn(List.of());

            List<InventoryResponse> response =
                    inventoryService.getBusinessInventory(
                            tenantId,
                            businessId
                    );

            assertNotNull(response);
            assertTrue(response.isEmpty());

            verify(inventoryRepository).findByBusinessId(businessId);
        }
    }

    // =========================================================
    // UPDATE REORDER LEVEL
    // =========================================================

    @Nested
    @DisplayName("Update Reorder Level")
    class UpdateReorderLevelTests {

        @Test
        @DisplayName("Should update inventory reorder level")
        void shouldUpdateReorderLevel() {

            mockBusiness();
            mockInventoryById();
            mockResponseMapping();

            UpdateInventoryRequest request = new UpdateInventoryRequest(
                    new BigDecimal("25.00")
            );

            when(inventoryRepository.save(any(Inventory.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            InventoryResponse response =
                    inventoryService.updateReorderLevel(
                            tenantId,
                            businessId,
                            inventoryId,
                            request
                    );

            assertNotNull(response);
            assertEquals(
                    new BigDecimal("25.00"),
                    inventory.getReorderLevel()
            );

            verify(inventoryRepository).save(inventory);
        }

        @Test
        @DisplayName("Should throw exception when inventory is missing during reorder update")
        void shouldThrowExceptionWhenInventoryMissingDuringReorderUpdate() {

            mockBusiness();

            when(inventoryRepository.findByIdAndBusinessId(
                    inventoryId,
                    businessId
            )).thenReturn(Optional.empty());

            UpdateInventoryRequest request = new UpdateInventoryRequest(
                    new BigDecimal("25.00")
            );

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> inventoryService.updateReorderLevel(
                            tenantId,
                            businessId,
                            inventoryId,
                            request
                    )
            );

            assertEquals("Inventory not found", exception.getMessage());

            verify(inventoryRepository, never()).save(any(Inventory.class));
        }
    }

    // =========================================================
    // MOCK HELPERS
    // =========================================================

    private void mockBusiness() {

        when(businessRepository.findByIdAndTenantId(
                businessId,
                tenantId
        )).thenReturn(Optional.of(business));
    }

    private void mockBranch() {

        when(branchRepository.findByIdAndBusiness_Id(
                branchId,
                businessId
        )).thenReturn(Optional.of(branch));
    }

    private void mockProduct() {

        when(productRepository.findByIdAndBusiness_Id(
                productId,
                businessId
        )).thenReturn(Optional.of(product));
    }

    private void mockInventoryById() {

        when(inventoryRepository.findByIdAndBusinessId(
                inventoryId,
                businessId
        )).thenReturn(Optional.of(inventory));
    }

    private void mockResponseMapping() {

        when(businessRepository.findById(businessId))
                .thenReturn(Optional.of(business));

        when(branchRepository.findByIdAndBusiness_Id(
                branchId,
                businessId
        )).thenReturn(Optional.of(branch));

        when(productRepository.findByIdAndBusiness_Id(
                productId,
                businessId
        )).thenReturn(Optional.of(product));
    }
}
