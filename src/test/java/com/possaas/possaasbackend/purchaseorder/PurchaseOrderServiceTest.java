package com.possaas.possaasbackend.purchaseorder;

import com.possaas.possaasbackend.branch.Branch;
import com.possaas.possaasbackend.branch.BranchRepository;
import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessRepository;
import com.possaas.possaasbackend.product.Product;
import com.possaas.possaasbackend.product.ProductRepository;
import com.possaas.possaasbackend.productvariant.ProductVariant;
import com.possaas.possaasbackend.productvariant.ProductVariantRepository;
import com.possaas.possaasbackend.purchaseorder.dto.CreatePurchaseOrderRequest;
import com.possaas.possaasbackend.purchaseorder.dto.PurchaseOrderItemRequest;
import com.possaas.possaasbackend.purchaseorder.dto.PurchaseOrderItemResponse;
import com.possaas.possaasbackend.purchaseorder.dto.PurchaseOrderResponse;
import com.possaas.possaasbackend.supplier.Supplier;
import com.possaas.possaasbackend.supplier.SupplierRepository;
import com.possaas.possaasbackend.user.User;
import com.possaas.possaasbackend.user.UserRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    private UUID tenantId;
    private UUID businessId;
    private UUID branchId;
    private UUID supplierId;
    private UUID userId;
    private UUID productId;
    private UUID secondProductId;
    private UUID variantId;

    private Business business;
    private Branch branch;
    private Supplier supplier;
    private User user;
    private Product product;
    private Product secondProduct;
    private ProductVariant variant;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        businessId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        supplierId = UUID.randomUUID();
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        secondProductId = UUID.randomUUID();
        variantId = UUID.randomUUID();

        business = new Business();
        business.setId(businessId);
        business.setActive(true);

        branch = new Branch();
        ReflectionTestUtils.setField(branch, "id", branchId);
        branch.setBusiness(business);

        supplier = new Supplier();
        ReflectionTestUtils.setField(supplier, "id", supplierId);
        supplier.setBusiness(business);
        supplier.setName("Acme Distributors");
        supplier.setActive(true);

        user = new User();
        ReflectionTestUtils.setField(user, "id", userId);
        user.setBusiness(business);

        product = new Product();
        ReflectionTestUtils.setField(product, "id", productId);
        product.setBusiness(business);
        product.setName("Espresso Beans");
        product.setActive(true);

        secondProduct = new Product();
        ReflectionTestUtils.setField(secondProduct, "id", secondProductId);
        secondProduct.setBusiness(business);
        secondProduct.setName("Oat Milk");
        secondProduct.setActive(true);

        variant = new ProductVariant();
        variant.setId(variantId);
        variant.setBusinessId(businessId);
        variant.setProductId(productId);
        variant.setActive(true);
    }

    private void mockBaseEntitiesExist() {
        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.of(business));
        when(branchRepository.findByIdAndBusiness_Id(branchId, businessId))
                .thenReturn(Optional.of(branch));
        when(supplierRepository.findByIdAndBusiness_Id(supplierId, businessId))
                .thenReturn(Optional.of(supplier));
        when(userRepository.findByIdAndBusiness_Id(userId, businessId))
                .thenReturn(Optional.of(user));
    }

    private void mockRepositoriesSave() {
        when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
                .thenAnswer(invocation -> {
                    PurchaseOrder po = invocation.getArgument(0);
                    ReflectionTestUtils.setField(po, "id", UUID.randomUUID());
                    ReflectionTestUtils.setField(po, "createdAt", LocalDateTime.now());
                    ReflectionTestUtils.setField(po, "updatedAt", LocalDateTime.now());
                    return po;
                });

        when(purchaseOrderItemRepository.saveAll(anyList()))
                .thenAnswer(invocation -> {
                    List<PurchaseOrderItem> items = invocation.getArgument(0);
                    for (PurchaseOrderItem item : items) {
                        ReflectionTestUtils.setField(item, "id", UUID.randomUUID());
                    }
                    return items;
                });
    }

    private CreatePurchaseOrderRequest buildRequest(List<PurchaseOrderItemRequest> items) {
        return new CreatePurchaseOrderRequest(
                branchId,
                supplierId,
                LocalDate.now().plusDays(7),
                "Restock order",
                items
        );
    }

    @Nested
    @DisplayName("Create Purchase Order Tests")
    class CreatePurchaseOrderTests {

        @Test
        @DisplayName("Should create purchase order successfully with single item and tax")
        void shouldCreatePurchaseOrderSuccessfully() {
            mockBaseEntitiesExist();
            when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                    .thenReturn(Optional.of(product));
            mockRepositoriesSave();

            LocalDate expectedDelivery = LocalDate.now().plusDays(5);
            CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest(
                    branchId,
                    supplierId,
                    expectedDelivery,
                    "Urgent restock",
                    List.of(new PurchaseOrderItemRequest(
                            productId,
                            null,
                            5,
                            new BigDecimal("12.34"),
                            new BigDecimal("16.00")
                    ))
            );

            PurchaseOrderResponse response = purchaseOrderService.createPurchaseOrder(
                    businessId, tenantId, userId, request
            );

            assertNotNull(response);
            assertNotNull(response.id());
            assertEquals(businessId, response.businessId());
            assertEquals(branchId, response.branchId());
            assertEquals(supplierId, response.supplierId());
            assertTrue(response.poNumber().matches("^PO-\\d{4}-[A-F0-9]{8}$"));
            assertEquals(PurchaseOrderStatus.DRAFT, response.status());
            assertEquals(LocalDate.now(), response.orderDate());
            assertEquals(expectedDelivery, response.expectedDeliveryDate());
            assertEquals("Urgent restock", response.notes());
            assertEquals(userId, response.createdBy());
            assertNull(response.approvedBy());
            assertNull(response.approvedAt());
            assertNotNull(response.createdAt());
            assertNotNull(response.updatedAt());

            // 5 * 12.34 = 61.70; 61.70 * 16 / 100 = 9.872 -> 9.87; total = 71.57
            assertEquals(new BigDecimal("61.70"), response.subtotal());
            assertEquals(new BigDecimal("9.87"), response.taxAmount());
            assertEquals(new BigDecimal("71.57"), response.totalAmount());

            assertEquals(1, response.items().size());
            PurchaseOrderItemResponse item = response.items().get(0);
            assertNotNull(item.id());
            assertEquals(productId, item.productId());
            assertNull(item.productVariantId());
            assertEquals(5, item.quantityOrdered());
            assertEquals(0, item.quantityReceived());
            assertEquals(new BigDecimal("12.34"), item.unitCost());
            assertEquals(new BigDecimal("16.00"), item.taxRate());
            assertEquals(new BigDecimal("61.70"), item.subtotal());
            assertEquals(new BigDecimal("9.87"), item.taxAmount());
            assertEquals(new BigDecimal("71.57"), item.totalAmount());

            verify(purchaseOrderRepository).save(any(PurchaseOrder.class));
            verify(purchaseOrderItemRepository).saveAll(anyList());
            verifyNoInteractions(productVariantRepository);
        }

        @Test
        @DisplayName("Should create purchase order with variant and default null tax rate to zero")
        void shouldCreatePurchaseOrderWithVariantAndMultipleItems() {
            mockBaseEntitiesExist();
            when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                    .thenReturn(Optional.of(product));
            when(productRepository.findByIdAndBusiness_Id(secondProductId, businessId))
                    .thenReturn(Optional.of(secondProduct));
            when(productVariantRepository.findByIdAndBusinessIdAndProductId(variantId, businessId, productId))
                    .thenReturn(Optional.of(variant));
            mockRepositoriesSave();

            CreatePurchaseOrderRequest request = buildRequest(List.of(
                    new PurchaseOrderItemRequest(
                            productId,
                            variantId,
                            3,
                            new BigDecimal("10.00"),
                            new BigDecimal("10.00")
                    ),
                    new PurchaseOrderItemRequest(
                            secondProductId,
                            null,
                            2,
                            new BigDecimal("5.50"),
                            null
                    )
            ));

            PurchaseOrderResponse response = purchaseOrderService.createPurchaseOrder(
                    businessId, tenantId, userId, request
            );

            // Item 1: 30.00 + 3.00 tax = 33.00
            // Item 2: 11.00 + 0.00 tax = 11.00
            // Order: subtotal = 41.00, tax = 3.00, total = 44.00
            assertEquals(new BigDecimal("41.00"), response.subtotal());
            assertEquals(new BigDecimal("3.00"), response.taxAmount());
            assertEquals(new BigDecimal("44.00"), response.totalAmount());
            assertEquals(2, response.items().size());

            PurchaseOrderItemResponse firstItem = response.items().get(0);
            assertEquals(variantId, firstItem.productVariantId());
            assertEquals(new BigDecimal("33.00"), firstItem.totalAmount());

            PurchaseOrderItemResponse secondItem = response.items().get(1);
            assertNull(secondItem.productVariantId());
            assertEquals(BigDecimal.ZERO, secondItem.taxRate());
            assertEquals(new BigDecimal("11.00"), secondItem.subtotal());
            assertEquals(new BigDecimal("0.00"), secondItem.taxAmount());
            assertEquals(new BigDecimal("11.00"), secondItem.totalAmount());

            verify(productVariantRepository).findByIdAndBusinessIdAndProductId(variantId, businessId, productId);
        }

        @Test
        @DisplayName("Should allow zero unit cost")
        void shouldAllowZeroUnitCost() {
            mockBaseEntitiesExist();
            when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                    .thenReturn(Optional.of(product));
            mockRepositoriesSave();

            CreatePurchaseOrderRequest request = buildRequest(List.of(
                    new PurchaseOrderItemRequest(productId, null, 4, BigDecimal.ZERO, BigDecimal.ZERO)
            ));

            PurchaseOrderResponse response = purchaseOrderService.createPurchaseOrder(
                    businessId, tenantId, userId, request
            );

            assertEquals(new BigDecimal("0.00"), response.subtotal());
            assertEquals(new BigDecimal("0.00"), response.taxAmount());
            assertEquals(new BigDecimal("0.00"), response.totalAmount());
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when business is not found")
        void shouldThrowWhenBusinessNotFound() {
            when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                    .thenReturn(Optional.empty());

            CreatePurchaseOrderRequest request = buildRequest(List.of(
                    new PurchaseOrderItemRequest(productId, null, 1, BigDecimal.TEN, BigDecimal.ZERO)
            ));

            EntityNotFoundException ex = assertThrows(
                    EntityNotFoundException.class,
                    () -> purchaseOrderService.createPurchaseOrder(businessId, tenantId, userId, request)
            );

            assertEquals("Business not found", ex.getMessage());
            verifyNoInteractions(branchRepository, supplierRepository, userRepository,
                    productRepository, purchaseOrderRepository, purchaseOrderItemRepository);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when branch is not found")
        void shouldThrowWhenBranchNotFound() {
            when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                    .thenReturn(Optional.of(business));
            when(branchRepository.findByIdAndBusiness_Id(branchId, businessId))
                    .thenReturn(Optional.empty());

            CreatePurchaseOrderRequest request = buildRequest(List.of(
                    new PurchaseOrderItemRequest(productId, null, 1, BigDecimal.TEN, BigDecimal.ZERO)
            ));

            EntityNotFoundException ex = assertThrows(
                    EntityNotFoundException.class,
                    () -> purchaseOrderService.createPurchaseOrder(businessId, tenantId, userId, request)
            );

            assertEquals("Branch not found for this business", ex.getMessage());
            verifyNoInteractions(supplierRepository, userRepository,
                    productRepository, purchaseOrderRepository, purchaseOrderItemRepository);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when supplier is not found")
        void shouldThrowWhenSupplierNotFound() {
            when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                    .thenReturn(Optional.of(business));
            when(branchRepository.findByIdAndBusiness_Id(branchId, businessId))
                    .thenReturn(Optional.of(branch));
            when(supplierRepository.findByIdAndBusiness_Id(supplierId, businessId))
                    .thenReturn(Optional.empty());

            CreatePurchaseOrderRequest request = buildRequest(List.of(
                    new PurchaseOrderItemRequest(productId, null, 1, BigDecimal.TEN, BigDecimal.ZERO)
            ));

            EntityNotFoundException ex = assertThrows(
                    EntityNotFoundException.class,
                    () -> purchaseOrderService.createPurchaseOrder(businessId, tenantId, userId, request)
            );

            assertEquals("Supplier not found for this business", ex.getMessage());
            verifyNoInteractions(userRepository, productRepository,
                    purchaseOrderRepository, purchaseOrderItemRepository);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when supplier is inactive")
        void shouldThrowWhenSupplierInactive() {
            supplier.setActive(false);
            when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                    .thenReturn(Optional.of(business));
            when(branchRepository.findByIdAndBusiness_Id(branchId, businessId))
                    .thenReturn(Optional.of(branch));
            when(supplierRepository.findByIdAndBusiness_Id(supplierId, businessId))
                    .thenReturn(Optional.of(supplier));

            CreatePurchaseOrderRequest request = buildRequest(List.of(
                    new PurchaseOrderItemRequest(productId, null, 1, BigDecimal.TEN, BigDecimal.ZERO)
            ));

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> purchaseOrderService.createPurchaseOrder(businessId, tenantId, userId, request)
            );

            assertEquals("Supplier is inactive", ex.getMessage());
            verifyNoInteractions(userRepository, productRepository,
                    purchaseOrderRepository, purchaseOrderItemRepository);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when user is not found")
        void shouldThrowWhenUserNotFound() {
            when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                    .thenReturn(Optional.of(business));
            when(branchRepository.findByIdAndBusiness_Id(branchId, businessId))
                    .thenReturn(Optional.of(branch));
            when(supplierRepository.findByIdAndBusiness_Id(supplierId, businessId))
                    .thenReturn(Optional.of(supplier));
            when(userRepository.findByIdAndBusiness_Id(userId, businessId))
                    .thenReturn(Optional.empty());

            CreatePurchaseOrderRequest request = buildRequest(List.of(
                    new PurchaseOrderItemRequest(productId, null, 1, BigDecimal.TEN, BigDecimal.ZERO)
            ));

            EntityNotFoundException ex = assertThrows(
                    EntityNotFoundException.class,
                    () -> purchaseOrderService.createPurchaseOrder(businessId, tenantId, userId, request)
            );

            assertEquals("User not found for this business", ex.getMessage());
            verifyNoInteractions(productRepository, purchaseOrderRepository, purchaseOrderItemRepository);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when product is not found")
        void shouldThrowWhenProductNotFound() {
            mockBaseEntitiesExist();
            when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                    .thenReturn(Optional.empty());

            CreatePurchaseOrderRequest request = buildRequest(List.of(
                    new PurchaseOrderItemRequest(productId, null, 1, BigDecimal.TEN, BigDecimal.ZERO)
            ));

            EntityNotFoundException ex = assertThrows(
                    EntityNotFoundException.class,
                    () -> purchaseOrderService.createPurchaseOrder(businessId, tenantId, userId, request)
            );

            assertEquals("Product not found for this business: " + productId, ex.getMessage());
            verifyNoInteractions(purchaseOrderRepository, purchaseOrderItemRepository);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when product is inactive")
        void shouldThrowWhenProductInactive() {
            product.setActive(false);
            mockBaseEntitiesExist();
            when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                    .thenReturn(Optional.of(product));

            CreatePurchaseOrderRequest request = buildRequest(List.of(
                    new PurchaseOrderItemRequest(productId, null, 1, BigDecimal.TEN, BigDecimal.ZERO)
            ));

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> purchaseOrderService.createPurchaseOrder(businessId, tenantId, userId, request)
            );

            assertEquals("Product is inactive: Espresso Beans", ex.getMessage());
            verifyNoInteractions(purchaseOrderRepository, purchaseOrderItemRepository);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when product variant is not found")
        void shouldThrowWhenVariantNotFound() {
            mockBaseEntitiesExist();
            when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                    .thenReturn(Optional.of(product));
            when(productVariantRepository.findByIdAndBusinessIdAndProductId(variantId, businessId, productId))
                    .thenReturn(Optional.empty());

            CreatePurchaseOrderRequest request = buildRequest(List.of(
                    new PurchaseOrderItemRequest(productId, variantId, 1, BigDecimal.TEN, BigDecimal.ZERO)
            ));

            EntityNotFoundException ex = assertThrows(
                    EntityNotFoundException.class,
                    () -> purchaseOrderService.createPurchaseOrder(businessId, tenantId, userId, request)
            );

            assertEquals("Variant not found for this product and business", ex.getMessage());
            verifyNoInteractions(purchaseOrderRepository, purchaseOrderItemRepository);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when product variant is inactive")
        void shouldThrowWhenVariantInactive() {
            variant.setActive(false);
            mockBaseEntitiesExist();
            when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                    .thenReturn(Optional.of(product));
            when(productVariantRepository.findByIdAndBusinessIdAndProductId(variantId, businessId, productId))
                    .thenReturn(Optional.of(variant));

            CreatePurchaseOrderRequest request = buildRequest(List.of(
                    new PurchaseOrderItemRequest(productId, variantId, 1, BigDecimal.TEN, BigDecimal.ZERO)
            ));

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> purchaseOrderService.createPurchaseOrder(businessId, tenantId, userId, request)
            );

            assertEquals("Product variant is inactive", ex.getMessage());
            verifyNoInteractions(purchaseOrderRepository, purchaseOrderItemRepository);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when quantity ordered is null or not positive")
        void shouldThrowWhenQuantityInvalid() {
            mockBaseEntitiesExist();
            when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                    .thenReturn(Optional.of(product));

            CreatePurchaseOrderRequest zeroQtyRequest = buildRequest(List.of(
                    new PurchaseOrderItemRequest(productId, null, 0, BigDecimal.TEN, BigDecimal.ZERO)
            ));

            IllegalArgumentException zeroEx = assertThrows(
                    IllegalArgumentException.class,
                    () -> purchaseOrderService.createPurchaseOrder(businessId, tenantId, userId, zeroQtyRequest)
            );
            assertEquals("Quantity ordered must be greater than zero", zeroEx.getMessage());

            CreatePurchaseOrderRequest nullQtyRequest = buildRequest(List.of(
                    new PurchaseOrderItemRequest(productId, null, null, BigDecimal.TEN, BigDecimal.ZERO)
            ));

            IllegalArgumentException nullEx = assertThrows(
                    IllegalArgumentException.class,
                    () -> purchaseOrderService.createPurchaseOrder(businessId, tenantId, userId, nullQtyRequest)
            );
            assertEquals("Quantity ordered must be greater than zero", nullEx.getMessage());

            verifyNoInteractions(purchaseOrderRepository, purchaseOrderItemRepository);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when unit cost is null or negative")
        void shouldThrowWhenUnitCostInvalid() {
            mockBaseEntitiesExist();
            when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                    .thenReturn(Optional.of(product));

            CreatePurchaseOrderRequest negativeCostRequest = buildRequest(List.of(
                    new PurchaseOrderItemRequest(productId, null, 2, new BigDecimal("-1.00"), BigDecimal.ZERO)
            ));

            IllegalArgumentException negativeEx = assertThrows(
                    IllegalArgumentException.class,
                    () -> purchaseOrderService.createPurchaseOrder(businessId, tenantId, userId, negativeCostRequest)
            );
            assertEquals("Unit cost cannot be negative", negativeEx.getMessage());

            CreatePurchaseOrderRequest nullCostRequest = buildRequest(List.of(
                    new PurchaseOrderItemRequest(productId, null, 2, null, BigDecimal.ZERO)
            ));

            IllegalArgumentException nullEx = assertThrows(
                    IllegalArgumentException.class,
                    () -> purchaseOrderService.createPurchaseOrder(businessId, tenantId, userId, nullCostRequest)
            );
            assertEquals("Unit cost cannot be negative", nullEx.getMessage());

            verifyNoInteractions(purchaseOrderRepository, purchaseOrderItemRepository);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when tax rate is negative")
        void shouldThrowWhenTaxRateNegative() {
            mockBaseEntitiesExist();
            when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                    .thenReturn(Optional.of(product));

            CreatePurchaseOrderRequest request = buildRequest(List.of(
                    new PurchaseOrderItemRequest(productId, null, 2, BigDecimal.TEN, new BigDecimal("-5.00"))
            ));

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> purchaseOrderService.createPurchaseOrder(businessId, tenantId, userId, request)
            );

            assertEquals("Tax rate cannot be negative", ex.getMessage());
            verifyNoInteractions(purchaseOrderRepository, purchaseOrderItemRepository);
        }
    }
}
