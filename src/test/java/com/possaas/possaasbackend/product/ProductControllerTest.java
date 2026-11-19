package com.possaas.possaasbackend.product;

import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.common.exception.GlobalExceptionHandler;
import com.possaas.possaasbackend.product.dto.CreateProductRequest;
import com.possaas.possaasbackend.product.dto.UpdateProductRequest;
import com.possaas.possaasbackend.security.AuthenticatedUser;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private UUID tenantId;
    private UUID businessId;
    private UUID productId;
    private Business business;
    private AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        businessId = UUID.randomUUID();
        productId = UUID.randomUUID();

        business = new Business();
        business.setId(businessId);
        business.setActive(true);

        authenticatedUser = new AuthenticatedUser(
                UUID.randomUUID(),
                "test@example.com",
                businessId,
                tenantId,
                Set.of("MANAGER"),
                Set.of(
                        "PRODUCT_CREATE",
                        "PRODUCT_VIEW",
                        "PRODUCT_UPDATE",
                        "PRODUCT_DELETE"
                )
        );

        HandlerMethodArgumentResolver authenticatedUserResolver =
                new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType()
                                .equals(AuthenticatedUser.class);
                    }

                    @Override
                    public Object resolveArgument(
                            MethodParameter parameter,
                            ModelAndViewContainer mavContainer,
                            NativeWebRequest webRequest,
                            WebDataBinderFactory binderFactory
                    ) {
                        return authenticatedUser;
                    }
                };

        mockMvc = MockMvcBuilders
                .standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(authenticatedUserResolver)
                .build();
    }

    private Product buildProduct(
            UUID id,
            String sku,
            String name,
            boolean active
    ) {
        Product product = new Product();

        ReflectionTestUtils.setField(product, "id", id);
        ReflectionTestUtils.setField(product, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(product, "updatedAt", LocalDateTime.now());

        product.setBusiness(business);
        product.setSku(sku);
        product.setName(name);
        product.setDescription("Test product description");
        product.setBarcode("616110000001");
        product.setImageUrl(null);
        product.setPurchasePrice(new BigDecimal("45.00"));
        product.setSellingPrice(new BigDecimal("60.00"));
        product.setTaxRate(new BigDecimal("16.0000"));
        product.setTaxInclusive(false);
        product.setActive(active);

        return product;
    }

    private String validProductJson() {
        return """
                {
                    "sku": "MILK-001",
                    "name": "Fresh Milk 500ml",
                    "description": "Fresh pasteurized milk",
                    "barcode": "616110000001",
                    "imageUrl": null,
                    "purchasePrice": 45.00,
                    "sellingPrice": 60.00,
                    "taxRate": 16.0000,
                    "taxInclusive": false,
                    "categoryId": null
                }
                """;
    }

    // =========================================================================
    // CREATE PRODUCT
    // =========================================================================

    @Test
    void createProduct_Success() throws Exception {
        Product product = buildProduct(
                productId, "MILK-001", "Fresh Milk 500ml", true
        );

        when(productService.createProduct(
                eq(tenantId),
                eq(businessId),
                any(CreateProductRequest.class)
        )).thenReturn(product);

        mockMvc.perform(post("/api/businesses/" + businessId + "/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product created successfully"))
                .andExpect(jsonPath("$.data.id").value(productId.toString()))
                .andExpect(jsonPath("$.data.businessId").value(businessId.toString()))
                .andExpect(jsonPath("$.data.sku").value("MILK-001"))
                .andExpect(jsonPath("$.data.name").value("Fresh Milk 500ml"))
                .andExpect(jsonPath("$.data.purchasePrice").value(45.00))
                .andExpect(jsonPath("$.data.sellingPrice").value(60.00))
                .andExpect(jsonPath("$.data.taxRate").value(16.0000))
                .andExpect(jsonPath("$.data.active").value(true));

        verify(productService).createProduct(
                eq(tenantId), eq(businessId), any(CreateProductRequest.class)
        );
    }

    @Test
    void createProduct_ValidationError_BlankSku() throws Exception {
        String json = """
                {
                    "sku": "",
                    "name": "Fresh Milk",
                    "purchasePrice": 45.00,
                    "sellingPrice": 60.00,
                    "taxRate": 16.00,
                    "taxInclusive": false
                }
                """;

        mockMvc.perform(post("/api/businesses/" + businessId + "/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value(Matchers.containsString("SKU is required")));

        verify(productService, never()).createProduct(any(), any(), any());
    }

    @Test
    void createProduct_ValidationError_MissingSellingPrice() throws Exception {
        String json = """
                {
                    "sku": "MILK-001",
                    "name": "Fresh Milk",
                    "purchasePrice": 45.00,
                    "taxRate": 16.00,
                    "taxInclusive": false
                }
                """;

        mockMvc.perform(post("/api/businesses/" + businessId + "/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(productService, never()).createProduct(any(), any(), any());
    }

    @Test
    void createProduct_DuplicateSku_ReturnsBadRequest() throws Exception {
        when(productService.createProduct(
                eq(tenantId),
                eq(businessId),
                any(CreateProductRequest.class)
        )).thenThrow(new IllegalArgumentException(
                "A product with this SKU already exists in this business"
        ));

        mockMvc.perform(post("/api/businesses/" + businessId + "/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(
                        "A product with this SKU already exists in this business"
                ));
    }

    // =========================================================================
    // GET PRODUCTS
    // =========================================================================

    @Test
    void getProducts_DefaultPagination_Success() throws Exception {
        Product product1 = buildProduct(
                UUID.randomUUID(), "MILK-001", "Fresh Milk", true
        );

        Product product2 = buildProduct(
                UUID.randomUUID(), "BREAD-001", "Brown Bread", true
        );

        Page<Product> page = new PageImpl<>(
                List.of(product1, product2),
                PageRequest.of(0, 10),
                2
        );

        when(productService.getProducts(
                eq(tenantId),
                eq(businessId),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/businesses/" + businessId + "/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Products fetched successfully"))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].sku").value("MILK-001"))
                .andExpect(jsonPath("$.data.content[1].sku").value("BREAD-001"))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(1));

        verify(productService).getProducts(
                eq(tenantId), eq(businessId), any(Pageable.class)
        );
    }

    @Test
    void getProducts_CustomPagination_Success() throws Exception {
        Page<Product> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(2, 5),
                0
        );

        when(productService.getProducts(
                eq(tenantId),
                eq(businessId),
                any(Pageable.class)
        )).thenReturn(emptyPage);

        mockMvc.perform(get("/api/businesses/" + businessId + "/products")
                        .param("page", "2")
                        .param("size", "5")
                        .param("sortBy", "name")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pageNumber").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(5))
                .andExpect(jsonPath("$.data.content.length()").value(0));
    }

    // =========================================================================
    // GET PRODUCT BY ID
    // =========================================================================

    @Test
    void getProductById_Success() throws Exception {
        Product product = buildProduct(
                productId, "MILK-001", "Fresh Milk", true
        );

        when(productService.getProductById(tenantId, businessId, productId))
                .thenReturn(product);

        mockMvc.perform(get(
                        "/api/businesses/" + businessId + "/products/" + productId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product fetched successfully"))
                .andExpect(jsonPath("$.data.id").value(productId.toString()))
                .andExpect(jsonPath("$.data.sku").value("MILK-001"));

        verify(productService).getProductById(tenantId, businessId, productId);
    }

    @Test
    void getProductById_NotFound_ReturnsBadRequest() throws Exception {
        when(productService.getProductById(tenantId, businessId, productId))
                .thenThrow(new IllegalArgumentException("Product not found"));

        mockMvc.perform(get(
                        "/api/businesses/" + businessId + "/products/" + productId
                ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Product not found"));
    }

    // =========================================================================
    // UPDATE PRODUCT
    // =========================================================================

    @Test
    void updateProduct_Success() throws Exception {
        Product updated = buildProduct(
                productId, "MILK-001", "Fresh Milk 1L", true
        );

        when(productService.updateProduct(
                eq(tenantId),
                eq(businessId),
                eq(productId),
                any(UpdateProductRequest.class)
        )).thenReturn(updated);

        String json = """
                {
                    "sku": "MILK-001",
                    "name": "Fresh Milk 1L",
                    "description": "One litre fresh milk",
                    "barcode": "616110000001",
                    "imageUrl": null,
                    "purchasePrice": 80.00,
                    "sellingPrice": 100.00,
                    "taxRate": 16.0000,
                    "taxInclusive": false,
                    "categoryId": null
                }
                """;

        mockMvc.perform(put(
                        "/api/businesses/" + businessId + "/products/" + productId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product updated successfully"))
                .andExpect(jsonPath("$.data.id").value(productId.toString()))
                .andExpect(jsonPath("$.data.name").value("Fresh Milk 1L"));

        verify(productService).updateProduct(
                eq(tenantId),
                eq(businessId),
                eq(productId),
                any(UpdateProductRequest.class)
        );
    }

    @Test
    void updateProduct_ValidationError_BlankName() throws Exception {
        String json = """
                {
                    "sku": "MILK-001",
                    "name": "",
                    "purchasePrice": 45.00,
                    "sellingPrice": 60.00,
                    "taxRate": 16.00,
                    "taxInclusive": false
                }
                """;

        mockMvc.perform(put(
                        "/api/businesses/" + businessId + "/products/" + productId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value(Matchers.containsString("Product name is required")));

        verify(productService, never()).updateProduct(any(), any(), any(), any());
    }

    // =========================================================================
    // UPDATE PRODUCT STATUS
    // =========================================================================

    @Test
    void updateProductStatus_Deactivate_Success() throws Exception {
        Product product = buildProduct(
                productId, "MILK-001", "Fresh Milk", false
        );

        when(productService.updateProductStatus(
                tenantId, businessId, productId, false
        )).thenReturn(product);

        mockMvc.perform(patch(
                        "/api/businesses/" + businessId + "/products/" + productId + "/status"
                ).param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product status updated successfully"))
                .andExpect(jsonPath("$.data.id").value(productId.toString()))
                .andExpect(jsonPath("$.data.active").value(false));

        verify(productService).updateProductStatus(
                tenantId, businessId, productId, false
        );
    }

    @Test
    void updateProductStatus_Activate_Success() throws Exception {
        Product product = buildProduct(
                productId, "MILK-001", "Fresh Milk", true
        );

        when(productService.updateProductStatus(
                tenantId, businessId, productId, true
        )).thenReturn(product);

        mockMvc.perform(patch(
                        "/api/businesses/" + businessId + "/products/" + productId + "/status"
                ).param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    // =========================================================================
    // DELETE PRODUCT
    // =========================================================================

    @Test
    void deleteProduct_Success() throws Exception {
        Product product = buildProduct(
                productId, "MILK-001", "Fresh Milk", false
        );

        when(productService.deleteProduct(tenantId, businessId, productId))
                .thenReturn(product);

        mockMvc.perform(delete(
                        "/api/businesses/" + businessId + "/products/" + productId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product deactivated successfully"))
                .andExpect(jsonPath("$.data.id").value(productId.toString()))
                .andExpect(jsonPath("$.data.active").value(false));

        verify(productService).deleteProduct(tenantId, businessId, productId);
    }

    @Test
    void deleteProduct_NotFound_ReturnsBadRequest() throws Exception {
        when(productService.deleteProduct(tenantId, businessId, productId))
                .thenThrow(new IllegalArgumentException("Product not found"));

        mockMvc.perform(delete(
                        "/api/businesses/" + businessId + "/products/" + productId
                ))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Product not found"));
    }
}
