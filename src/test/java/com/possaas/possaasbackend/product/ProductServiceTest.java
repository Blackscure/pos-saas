
package com.possaas.possaasbackend.product;

import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessRepository;
import com.possaas.possaasbackend.category.Category;
import com.possaas.possaasbackend.category.CategoryRepository;
import com.possaas.possaasbackend.product.dto.CreateProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private Business business;

    @InjectMocks
    private ProductService productService;

    private UUID tenantId;
    private UUID businessId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        businessId = UUID.randomUUID();
        productId = UUID.randomUUID();

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.of(business));
    }

    private void stubActiveBusiness() {
        when(business.isActive()).thenReturn(true);
    }

    // =========================================================================
    // CREATE PRODUCT
    // =========================================================================

    @Test
    void shouldCreateProductSuccessfully() {

        stubActiveBusiness();

        UUID categoryId = UUID.randomUUID();

        Category category = new Category();
        category.setActive(true);

        CreateProductRequest request = new CreateProductRequest(
                " MILK-001 ",
                " Fresh Milk 500ml ",
                "Fresh pasteurized milk",
                "616110000001",
                null,
                new BigDecimal("45.00"),
                new BigDecimal("60.00"),
                new BigDecimal("16.0000"),
                false,
                categoryId
        );

        when(productRepository.existsByBusiness_IdAndSkuIgnoreCase(
                businessId, "MILK-001"
        )).thenReturn(false);

        when(productRepository.existsByBusiness_IdAndBarcodeIgnoreCase(
                businessId, "616110000001"
        )).thenReturn(false);

        when(categoryRepository.findByIdAndBusiness_Id(categoryId, businessId))
                .thenReturn(Optional.of(category));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.createProduct(
                tenantId, businessId, request
        );

        assertNotNull(result);
        assertEquals("MILK-001", result.getSku());
        assertEquals("Fresh Milk 500ml", result.getName());
        assertEquals(new BigDecimal("45.00"), result.getPurchasePrice());
        assertEquals(new BigDecimal("60.00"), result.getSellingPrice());
        assertEquals(new BigDecimal("16.0000"), result.getTaxRate());
        assertSame(category, result.getCategory());
        assertTrue(result.isActive());

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldRejectProductCreationWhenCategoryIdIsNull() {

        stubActiveBusiness();

        CreateProductRequest request = new CreateProductRequest(
                "MILK-001",
                "Fresh Milk",
                null,
                null,
                null,
                BigDecimal.ZERO,
                new BigDecimal("60.00"),
                BigDecimal.ZERO,
                false,
                null
        );

        when(productRepository.existsByBusiness_IdAndSkuIgnoreCase(
                businessId, "MILK-001"
        )).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(tenantId, businessId, request)
        );

        assertEquals("Category ID is required", exception.getMessage());

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldRejectDuplicateSku() {

        stubActiveBusiness();

        CreateProductRequest request = new CreateProductRequest(
                "MILK-001",
                "Fresh Milk",
                null,
                null,
                null,
                BigDecimal.ZERO,
                new BigDecimal("60.00"),
                BigDecimal.ZERO,
                false,
                UUID.randomUUID()
        );

        when(productRepository.existsByBusiness_IdAndSkuIgnoreCase(
                businessId, "MILK-001"
        )).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(tenantId, businessId, request)
        );

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldRejectCategoryFromAnotherBusiness() {

        stubActiveBusiness();

        UUID categoryId = UUID.randomUUID();

        CreateProductRequest request = new CreateProductRequest(
                "MILK-002",
                "Fresh Milk",
                null,
                null,
                null,
                BigDecimal.ZERO,
                new BigDecimal("60.00"),
                BigDecimal.ZERO,
                false,
                categoryId
        );

        when(productRepository.existsByBusiness_IdAndSkuIgnoreCase(
                businessId, "MILK-002"
        )).thenReturn(false);

        when(categoryRepository.findByIdAndBusiness_Id(categoryId, businessId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(tenantId, businessId, request)
        );

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldRejectProductCreationWhenCategoryIsInactive() {

        stubActiveBusiness();

        UUID categoryId = UUID.randomUUID();

        Category inactiveCategory = new Category();
        inactiveCategory.setActive(false);

        CreateProductRequest request = new CreateProductRequest(
                "MILK-002",
                "Fresh Milk",
                null,
                null,
                null,
                BigDecimal.ZERO,
                new BigDecimal("60.00"),
                BigDecimal.ZERO,
                false,
                categoryId
        );

        when(productRepository.existsByBusiness_IdAndSkuIgnoreCase(
                businessId, "MILK-002"
        )).thenReturn(false);

        when(categoryRepository.findByIdAndBusiness_Id(categoryId, businessId))
                .thenReturn(Optional.of(inactiveCategory));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(tenantId, businessId, request)
        );

        assertEquals(
                "Cannot assign a product to an inactive category",
                exception.getMessage()
        );

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldRejectProductCreationForInactiveBusiness() {

        when(business.isActive()).thenReturn(false);

        CreateProductRequest request = new CreateProductRequest(
                "MILK-003",
                "Fresh Milk",
                null,
                null,
                null,
                BigDecimal.ZERO,
                new BigDecimal("60.00"),
                BigDecimal.ZERO,
                false,
                UUID.randomUUID()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(tenantId, businessId, request)
        );

        verify(productRepository, never()).save(any(Product.class));
    }

    // =========================================================================
    // PRODUCT STATUS
    // =========================================================================

    @Test
    void shouldDeactivateProductSuccessfully() {

        stubActiveBusiness();

        Product product = new Product();
        product.setActive(true);

        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.of(product));

        when(productRepository.save(product)).thenReturn(product);

        Product result = productService.updateProductStatus(
                tenantId, businessId, productId, false
        );

        assertNotNull(result);
        assertFalse(result.isActive());

        verify(productRepository).save(product);
    }

    // =========================================================================
    // GET PRODUCT BY ID
    // =========================================================================

    @Test
    void shouldRejectProductNotFound() {

        when(productRepository.findByIdAndBusiness_Id(productId, businessId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.getProductById(
                        tenantId, businessId, productId
                )
        );

        verify(productRepository, never()).save(any(Product.class));
    }

    // =========================================================================
    // GET PRODUCTS (SEARCH, FILTER, PAGINATION)
    // =========================================================================

    @Test
    void shouldGetProductsWithAllFilters() {

        Pageable pageable = PageRequest.of(0, 10);
        UUID categoryId = UUID.randomUUID();

        Product product = new Product();

        Page<Product> expectedPage =
                new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(expectedPage);

        Page<Product> result = productService.getProducts(
                tenantId,
                businessId,
                "  milk  ",
                " Fresh Milk ",
                " MILK-001 ",
                " 616110000001 ",
                categoryId,
                true,
                pageable
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertSame(product, result.getContent().get(0));

        verify(productRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }

    @Test
    void shouldGetProductsWithoutFilters() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> expectedPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(productRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(expectedPage);

        Page<Product> result = productService.getProducts(
                tenantId,
                businessId,
                pageable
        );

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        verify(productRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }

    @Test
    void shouldTreatBlankFilterTermsAsNull() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> expectedPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(productRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(expectedPage);

        Page<Product> result = productService.getProducts(
                tenantId,
                businessId,
                "   ",
                "",
                "   ",
                "",
                null,
                null,
                pageable
        );

        assertNotNull(result);

        verify(productRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }

    @Test
    void shouldFilterProductsByCategory() {

        Pageable pageable = PageRequest.of(0, 10);
        UUID categoryId = UUID.randomUUID();

        Page<Product> expectedPage =
                new PageImpl<>(List.of(new Product()), pageable, 1);

        when(productRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(expectedPage);

        Page<Product> result = productService.getProducts(
                tenantId,
                businessId,
                null,
                null,
                null,
                null,
                categoryId,
                null,
                pageable
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(productRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }

    @Test
    void shouldFilterProductsByActiveStatus() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Product> expectedPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(productRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(expectedPage);

        Page<Product> result = productService.getProducts(
                tenantId,
                businessId,
                null,
                null,
                null,
                null,
                null,
                false,
                pageable
        );

        assertNotNull(result);

        verify(productRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }

    @Test
    void shouldThrowExceptionWhenBusinessNotFoundOnGetProducts() {

        Pageable pageable = PageRequest.of(0, 10);

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> productService.getProducts(
                        tenantId,
                        businessId,
                        pageable
                )
        );

        verify(productRepository, never()).findAll(
                any(Specification.class),
                any(Pageable.class)
        );
    }
}
