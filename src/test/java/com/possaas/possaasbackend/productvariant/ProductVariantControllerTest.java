
package com.possaas.possaasbackend.productvariant;

import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.productvariant.dto.CreateProductVariantRequest;
import com.possaas.possaasbackend.productvariant.dto.UpdateProductVariantRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductVariantControllerTest {

    @Mock
    private ProductVariantService productVariantService;

    @InjectMocks
    private ProductVariantController controller;

    private UUID businessId;
    private UUID productId;
    private UUID variantId;

    @BeforeEach
    void setUp() {
        businessId = UUID.randomUUID();
        productId = UUID.randomUUID();
        variantId = UUID.randomUUID();
    }

    // --------------------------------------------------
    // CREATE VARIANT
    // --------------------------------------------------

    @Test
    void createVariant_shouldReturnSuccessResponse() {

        CreateProductVariantRequest request =
                new CreateProductVariantRequest(
                        "SKU-001",
                        "BAR-001",
                        "Large Size",
                        new BigDecimal("100.00"),
                        new BigDecimal("150.00")
                );

        ProductVariant expected = mock(ProductVariant.class);

        when(productVariantService.createVariant(
                businessId,
                productId,
                request
        )).thenReturn(expected);

        ApiResponse<ProductVariant> response =
                controller.createVariant(
                        businessId,
                        productId,
                        request
                );

        assertNotNull(response);
        assertEquals(
                "Product variant created successfully",
                response.getMessage()
        );
        assertSame(expected, response.getData());

        verify(productVariantService).createVariant(
                businessId,
                productId,
                request
        );
    }

    // --------------------------------------------------
    // GET ALL VARIANTS
    // --------------------------------------------------

    @Test
    void getProductVariants_shouldReturnVariantsSuccessfully() {

        ProductVariant variant1 = mock(ProductVariant.class);
        ProductVariant variant2 = mock(ProductVariant.class);

        List<ProductVariant> expected = List.of(variant1, variant2);

        when(productVariantService.getProductVariants(
                businessId,
                productId
        )).thenReturn(expected);

        ApiResponse<List<ProductVariant>> response =
                controller.getProductVariants(
                        businessId,
                        productId
                );

        assertNotNull(response);
        assertEquals(
                "Product variants retrieved successfully",
                response.getMessage()
        );
        assertEquals(expected, response.getData());
        assertEquals(2, response.getData().size());

        verify(productVariantService).getProductVariants(
                businessId,
                productId
        );
    }

    @Test
    void getProductVariants_shouldReturnEmptyList() {

        when(productVariantService.getProductVariants(
                businessId,
                productId
        )).thenReturn(List.of());

        ApiResponse<List<ProductVariant>> response =
                controller.getProductVariants(
                        businessId,
                        productId
                );

        assertNotNull(response);
        assertNotNull(response.getData());
        assertTrue(response.getData().isEmpty());

        verify(productVariantService).getProductVariants(
                businessId,
                productId
        );
    }

    // --------------------------------------------------
    // GET VARIANT BY ID
    // --------------------------------------------------

    @Test
    void getVariantById_shouldReturnVariantSuccessfully() {

        ProductVariant expected = mock(ProductVariant.class);

        when(productVariantService.getVariantById(
                businessId,
                productId,
                variantId
        )).thenReturn(expected);

        ApiResponse<ProductVariant> response =
                controller.getVariantById(
                        businessId,
                        productId,
                        variantId
                );

        assertNotNull(response);
        assertEquals(
                "Product variant retrieved successfully",
                response.getMessage()
        );
        assertSame(expected, response.getData());

        verify(productVariantService).getVariantById(
                businessId,
                productId,
                variantId
        );
    }

    // --------------------------------------------------
    // UPDATE VARIANT
    // --------------------------------------------------

    @Test
    void updateVariant_shouldReturnSuccessResponse() {

        UpdateProductVariantRequest request =
                new UpdateProductVariantRequest(
                        "SKU-UPDATED",
                        "BAR-UPDATED",
                        "Updated Variant",
                        new BigDecimal("120.00"),
                        new BigDecimal("180.00")
                );

        ProductVariant expected = mock(ProductVariant.class);

        when(productVariantService.updateVariant(
                businessId,
                productId,
                variantId,
                request
        )).thenReturn(expected);

        ApiResponse<ProductVariant> response =
                controller.updateVariant(
                        businessId,
                        productId,
                        variantId,
                        request
                );

        assertNotNull(response);
        assertEquals(
                "Product variant updated successfully",
                response.getMessage()
        );
        assertSame(expected, response.getData());

        verify(productVariantService).updateVariant(
                businessId,
                productId,
                variantId,
                request
        );
    }

    // --------------------------------------------------
    // UPDATE VARIANT STATUS
    // --------------------------------------------------

    @Test
    void updateVariantStatus_shouldDeactivateVariantSuccessfully() {

        ProductVariant expected = mock(ProductVariant.class);

        when(productVariantService.updateVariantStatus(
                businessId,
                productId,
                variantId,
                false
        )).thenReturn(expected);

        ApiResponse<ProductVariant> response =
                controller.updateVariantStatus(
                        businessId,
                        productId,
                        variantId,
                        false
                );

        assertNotNull(response);
        assertEquals(
                "Product variant deactivated successfully",
                response.getMessage()
        );
        assertSame(expected, response.getData());

        verify(productVariantService).updateVariantStatus(
                businessId,
                productId,
                variantId,
                false
        );
    }

    @Test
    void updateVariantStatus_shouldActivateVariantSuccessfully() {

        ProductVariant expected = mock(ProductVariant.class);

        when(productVariantService.updateVariantStatus(
                businessId,
                productId,
                variantId,
                true
        )).thenReturn(expected);

        ApiResponse<ProductVariant> response =
                controller.updateVariantStatus(
                        businessId,
                        productId,
                        variantId,
                        true
                );

        assertNotNull(response);
        assertEquals(
                "Product variant activated successfully",
                response.getMessage()
        );
        assertSame(expected, response.getData());

        verify(productVariantService).updateVariantStatus(
                businessId,
                productId,
                variantId,
                true
        );
    }
}
