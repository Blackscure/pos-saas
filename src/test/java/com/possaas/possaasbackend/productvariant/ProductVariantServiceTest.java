package com.possaas.possaasbackend.productvariant;

import com.possaas.possaasbackend.product.Product;
import com.possaas.possaasbackend.product.ProductRepository;
import com.possaas.possaasbackend.productvariant.dto.CreateProductVariantRequest;
import com.possaas.possaasbackend.productvariant.dto.UpdateProductVariantRequest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductVariantServiceTest {

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductVariantService productVariantService;

    private UUID businessId;
    private UUID productId;
    private UUID variantId;

    private Product product;
    private ProductVariant variant;

    @BeforeEach
    void setUp() {
        businessId = UUID.randomUUID();
        productId = UUID.randomUUID();
        variantId = UUID.randomUUID();

        product = mock(Product.class);

        variant = new ProductVariant();
        variant.setId(variantId);
        variant.setBusinessId(businessId);
        variant.setProductId(productId);
        variant.setSku("SKU-001");
        variant.setBarcode("123456789");
        variant.setName("Small Red");
        variant.setPurchasePrice(new BigDecimal("100.00"));
        variant.setSellingPrice(new BigDecimal("150.00"));
        variant.setActive(true);
    }

    // =========================================================
    // CREATE VARIANT
    // =========================================================

    @Test
    void shouldCreateVariantSuccessfully() {
        CreateProductVariantRequest request =
                new CreateProductVariantRequest(
                        " SKU-002 ",
                        " 987654321 ",
                        "Medium Blue",
                        new BigDecimal("200.00"),
                        new BigDecimal("300.00")
                );

        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.of(product));

        when(productVariantRepository.existsByBusinessIdAndSkuIgnoreCase(
                businessId, "SKU-002"))
                .thenReturn(false);

        when(productVariantRepository.existsByBusinessIdAndBarcodeIgnoreCase(
                businessId, "987654321"))
                .thenReturn(false);

        when(productVariantRepository.save(any(ProductVariant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductVariant result =
                productVariantService.createVariant(
                        businessId, productId, request);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(businessId, result.getBusinessId());
        assertEquals(productId, result.getProductId());
        assertEquals("SKU-002", result.getSku());
        assertEquals("987654321", result.getBarcode());
        assertEquals("Medium Blue", result.getName());
        assertEquals(new BigDecimal("200.00"), result.getPurchasePrice());
        assertEquals(new BigDecimal("300.00"), result.getSellingPrice());
        assertTrue(result.getActive());

        verify(productVariantRepository).save(any(ProductVariant.class));
    }

    @Test
    void shouldRejectCreatingVariantWhenProductDoesNotExist() {
        CreateProductVariantRequest request =
                new CreateProductVariantRequest(
                        "SKU-002",
                        "987654321",
                        "Medium Blue",
                        new BigDecimal("200.00"),
                        new BigDecimal("300.00")
                );

        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> productVariantService.createVariant(
                        businessId, productId, request));

        verify(productVariantRepository, never())
                .save(any(ProductVariant.class));
    }

    @Test
    void shouldRejectDuplicateSku() {
        CreateProductVariantRequest request =
                new CreateProductVariantRequest(
                        "SKU-001",
                        "987654321",
                        "Medium Blue",
                        new BigDecimal("200.00"),
                        new BigDecimal("300.00")
                );

        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.of(product));

        when(productVariantRepository.existsByBusinessIdAndSkuIgnoreCase(
                businessId, "SKU-001"))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> productVariantService.createVariant(
                        businessId, productId, request));

        verify(productVariantRepository, never())
                .save(any(ProductVariant.class));
    }

    @Test
    void shouldRejectDuplicateBarcode() {
        CreateProductVariantRequest request =
                new CreateProductVariantRequest(
                        "SKU-002",
                        "123456789",
                        "Medium Blue",
                        new BigDecimal("200.00"),
                        new BigDecimal("300.00")
                );

        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.of(product));

        when(productVariantRepository.existsByBusinessIdAndSkuIgnoreCase(
                businessId, "SKU-002"))
                .thenReturn(false);

        when(productVariantRepository.existsByBusinessIdAndBarcodeIgnoreCase(
                businessId, "123456789"))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> productVariantService.createVariant(
                        businessId, productId, request));

        verify(productVariantRepository, never())
                .save(any(ProductVariant.class));
    }

    @Test
    void shouldAllowCreatingVariantWithoutBarcode() {
        CreateProductVariantRequest request =
                new CreateProductVariantRequest(
                        "SKU-003",
                        null,
                        "Large Green",
                        new BigDecimal("250.00"),
                        new BigDecimal("400.00")
                );

        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.of(product));

        when(productVariantRepository.existsByBusinessIdAndSkuIgnoreCase(
                businessId, "SKU-003"))
                .thenReturn(false);

        when(productVariantRepository.save(any(ProductVariant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductVariant result =
                productVariantService.createVariant(
                        businessId, productId, request);

        assertNull(result.getBarcode());
        assertEquals("SKU-003", result.getSku());

        verify(productVariantRepository, never())
                .existsByBusinessIdAndBarcodeIgnoreCase(any(), any());
    }

    // =========================================================
    // GET PRODUCT VARIANTS
    // =========================================================

    @Test
    void shouldReturnAllProductVariants() {
        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.of(product));

        when(productVariantRepository.findAllByBusinessIdAndProductId(
                businessId, productId))
                .thenReturn(List.of(variant));

        List<ProductVariant> result =
                productVariantService.getProductVariants(
                        businessId, productId);

        assertEquals(1, result.size());
        assertEquals(variantId, result.get(0).getId());
        assertEquals("SKU-001", result.get(0).getSku());
    }

    @Test
    void shouldReturnEmptyListWhenProductHasNoVariants() {
        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.of(product));

        when(productVariantRepository.findAllByBusinessIdAndProductId(
                businessId, productId))
                .thenReturn(List.of());

        List<ProductVariant> result =
                productVariantService.getProductVariants(
                        businessId, productId);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenGettingVariantsForNonexistentProduct() {
        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> productVariantService.getProductVariants(
                        businessId, productId));

        verify(productVariantRepository, never())
                .findAllByBusinessIdAndProductId(any(), any());
    }

    // =========================================================
    // GET VARIANT BY ID
    // =========================================================

    @Test
    void shouldReturnVariantById() {
        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.of(product));

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId))
                .thenReturn(Optional.of(variant));

        ProductVariant result =
                productVariantService.getVariantById(
                        businessId, productId, variantId);

        assertNotNull(result);
        assertEquals(variantId, result.getId());
        assertEquals("SKU-001", result.getSku());
    }

    @Test
    void shouldThrowExceptionWhenVariantDoesNotExist() {
        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.of(product));

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> productVariantService.getVariantById(
                        businessId, productId, variantId));
    }

    @Test
    void shouldThrowExceptionWhenGettingVariantForNonexistentProduct() {
        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> productVariantService.getVariantById(
                        businessId, productId, variantId));

        verify(productVariantRepository, never())
                .findByIdAndBusinessIdAndProductId(any(), any(), any());
    }

    // =========================================================
    // UPDATE VARIANT
    // =========================================================

    @Test
    void shouldUpdateVariantSuccessfully() {
        UpdateProductVariantRequest request =
                new UpdateProductVariantRequest(
                        "SKU-UPDATED",
                        "987654321",
                        "Updated Variant",
                        new BigDecimal("120.00"),
                        new BigDecimal("180.00")
                );

        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.of(product));

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId))
                .thenReturn(Optional.of(variant));

        when(productVariantRepository.existsByBusinessIdAndSkuIgnoreCase(
                businessId, "SKU-UPDATED"))
                .thenReturn(false);

        when(productVariantRepository.existsByBusinessIdAndBarcodeIgnoreCase(
                businessId, "987654321"))
                .thenReturn(false);

        when(productVariantRepository.save(variant))
                .thenReturn(variant);

        ProductVariant result =
                productVariantService.updateVariant(
                        businessId, productId, variantId, request);

        assertEquals("SKU-UPDATED", result.getSku());
        assertEquals("987654321", result.getBarcode());
        assertEquals("Updated Variant", result.getName());
        assertEquals(new BigDecimal("120.00"), result.getPurchasePrice());
        assertEquals(new BigDecimal("180.00"), result.getSellingPrice());

        verify(productVariantRepository).save(variant);
    }

    @Test
    void shouldUpdateVariantNameAndPricesWithoutChangingSkuOrBarcode() {
        UpdateProductVariantRequest request =
                new UpdateProductVariantRequest(
                        null,
                        null,
                        "  New Name  ",
                        new BigDecimal("110.00"),
                        new BigDecimal("170.00")
                );

        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.of(product));

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId))
                .thenReturn(Optional.of(variant));

        when(productVariantRepository.save(variant))
                .thenReturn(variant);

        ProductVariant result =
                productVariantService.updateVariant(
                        businessId, productId, variantId, request);

        assertEquals("SKU-001", result.getSku());
        assertEquals("123456789", result.getBarcode());
        assertEquals("New Name", result.getName());
        assertEquals(new BigDecimal("110.00"), result.getPurchasePrice());
        assertEquals(new BigDecimal("170.00"), result.getSellingPrice());
    }

    @Test
    void shouldRejectUpdatingVariantWithDuplicateSku() {
        UpdateProductVariantRequest request =
                new UpdateProductVariantRequest(
                        "SKU-OTHER",
                        null,
                        null,
                        null,
                        null
                );

        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.of(product));

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId))
                .thenReturn(Optional.of(variant));

        when(productVariantRepository.existsByBusinessIdAndSkuIgnoreCase(
                businessId, "SKU-OTHER"))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> productVariantService.updateVariant(
                        businessId, productId, variantId, request));

        verify(productVariantRepository, never())
                .save(variant);
    }

    @Test
    void shouldRejectUpdatingVariantWithDuplicateBarcode() {
        UpdateProductVariantRequest request =
                new UpdateProductVariantRequest(
                        null,
                        "999999999",
                        null,
                        null,
                        null
                );

        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.of(product));

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId))
                .thenReturn(Optional.of(variant));

        when(productVariantRepository.existsByBusinessIdAndBarcodeIgnoreCase(
                businessId, "999999999"))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> productVariantService.updateVariant(
                        businessId, productId, variantId, request));

        verify(productVariantRepository, never())
                .save(variant);
    }

    @Test
    void shouldRejectNegativePurchasePrice() {
        UpdateProductVariantRequest request =
                new UpdateProductVariantRequest(
                        null,
                        null,
                        null,
                        new BigDecimal("-1.00"),
                        null
                );

        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.of(product));

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId))
                .thenReturn(Optional.of(variant));

        assertThrows(IllegalArgumentException.class,
                () -> productVariantService.updateVariant(
                        businessId, productId, variantId, request));

        verify(productVariantRepository, never())
                .save(variant);
    }

    @Test
    void shouldRejectNegativeSellingPrice() {
        UpdateProductVariantRequest request =
                new UpdateProductVariantRequest(
                        null,
                        null,
                        null,
                        null,
                        new BigDecimal("-5.00")
                );

        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.of(product));

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId))
                .thenReturn(Optional.of(variant));

        assertThrows(IllegalArgumentException.class,
                () -> productVariantService.updateVariant(
                        businessId, productId, variantId, request));

        verify(productVariantRepository, never())
                .save(variant);
    }

    @Test
    void shouldNotCheckSkuWhenSkuIsUnchanged() {
        UpdateProductVariantRequest request =
                new UpdateProductVariantRequest(
                        "SKU-001",
                        null,
                        null,
                        null,
                        null
                );

        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.of(product));

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId))
                .thenReturn(Optional.of(variant));

        when(productVariantRepository.save(variant))
                .thenReturn(variant);

        productVariantService.updateVariant(
                businessId, productId, variantId, request);

        verify(productVariantRepository, never())
                .existsByBusinessIdAndSkuIgnoreCase(any(), any());
    }

    @Test
    void shouldNotCheckBarcodeWhenBarcodeIsUnchanged() {
        UpdateProductVariantRequest request =
                new UpdateProductVariantRequest(
                        null,
                        "123456789",
                        null,
                        null,
                        null
                );

        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.of(product));

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId))
                .thenReturn(Optional.of(variant));

        when(productVariantRepository.save(variant))
                .thenReturn(variant);

        productVariantService.updateVariant(
                businessId, productId, variantId, request);

        verify(productVariantRepository, never())
                .existsByBusinessIdAndBarcodeIgnoreCase(any(), any());
    }

    // =========================================================
    // UPDATE VARIANT STATUS
    // =========================================================

    @Test
    void shouldDeactivateVariantSuccessfully() {
        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.of(product));

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId))
                .thenReturn(Optional.of(variant));

        when(productVariantRepository.save(variant))
                .thenReturn(variant);

        ProductVariant result =
                productVariantService.updateVariantStatus(
                        businessId, productId, variantId, false);

        assertFalse(result.getActive());
        verify(productVariantRepository).save(variant);
    }

    @Test
    void shouldActivateVariantSuccessfully() {
        variant.setActive(false);

        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.of(product));

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId))
                .thenReturn(Optional.of(variant));

        when(productVariantRepository.save(variant))
                .thenReturn(variant);

        ProductVariant result =
                productVariantService.updateVariantStatus(
                        businessId, productId, variantId, true);

        assertTrue(result.getActive());
        verify(productVariantRepository).save(variant);
    }
}
