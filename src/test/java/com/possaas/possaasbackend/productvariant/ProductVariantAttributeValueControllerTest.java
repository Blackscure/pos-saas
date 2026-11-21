package com.possaas.possaasbackend.productvariant;

import com.possaas.possaasbackend.common.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductVariantAttributeValueControllerTest {

    @Mock
    private ProductVariantAttributeValueService service;

    @InjectMocks
    private ProductVariantAttributeValueController controller;

    private UUID businessId;
    private UUID productId;
    private UUID variantId;
    private UUID productAttributeId;
    private UUID attributeValueId;

    @BeforeEach
    void setUp() {
        businessId = UUID.randomUUID();
        productId = UUID.randomUUID();
        variantId = UUID.randomUUID();
        productAttributeId = UUID.randomUUID();
        attributeValueId = UUID.randomUUID();
    }

    // --------------------------------------------------
    // SET ATTRIBUTE VALUE
    // --------------------------------------------------

    @Test
    void setAttributeValue_shouldReturnSuccessResponse() {

        ProductVariantAttributeValue expected =
                mock(ProductVariantAttributeValue.class);

        when(service.setAttributeValue(
                businessId,
                productId,
                variantId,
                productAttributeId,
                attributeValueId
        )).thenReturn(expected);

        ApiResponse<ProductVariantAttributeValue> response =
                controller.setAttributeValue(
                        businessId,
                        productId,
                        variantId,
                        productAttributeId,
                        attributeValueId
                );

        assertNotNull(response);
        assertEquals(
                "Variant attribute value assigned successfully",
                response.getMessage()
        );
        assertEquals(expected, response.getData());

        verify(service).setAttributeValue(
                businessId,
                productId,
                variantId,
                productAttributeId,
                attributeValueId
        );
    }

    // --------------------------------------------------
    // GET ATTRIBUTE VALUES
    // --------------------------------------------------

    @Test
    void getVariantAttributeValues_shouldReturnSuccessResponse() {

        ProductVariantAttributeValue value1 =
                mock(ProductVariantAttributeValue.class);

        ProductVariantAttributeValue value2 =
                mock(ProductVariantAttributeValue.class);

        List<ProductVariantAttributeValue> expected =
                List.of(value1, value2);

        when(service.getVariantAttributeValues(
                businessId,
                productId,
                variantId
        )).thenReturn(expected);

        ApiResponse<List<ProductVariantAttributeValue>> response =
                controller.getVariantAttributeValues(
                        businessId,
                        productId,
                        variantId
                );

        assertNotNull(response);

        assertEquals(
                "Variant attribute values retrieved successfully",
                response.getMessage()
        );

        assertEquals(expected, response.getData());
        assertEquals(2, response.getData().size());

        verify(service).getVariantAttributeValues(
                businessId,
                productId,
                variantId
        );
    }

    @Test
    void getVariantAttributeValues_shouldReturnEmptyList() {

        when(service.getVariantAttributeValues(
                businessId,
                productId,
                variantId
        )).thenReturn(List.of());

        ApiResponse<List<ProductVariantAttributeValue>> response =
                controller.getVariantAttributeValues(
                        businessId,
                        productId,
                        variantId
                );

        assertNotNull(response);
        assertNotNull(response.getData());
        assertTrue(response.getData().isEmpty());

        verify(service).getVariantAttributeValues(
                businessId,
                productId,
                variantId
        );
    }

    // --------------------------------------------------
    // REMOVE ATTRIBUTE VALUE
    // --------------------------------------------------

    @Test
    void removeAttributeValue_shouldReturnSuccessResponse() {

        doNothing().when(service).removeAttributeValue(
                businessId,
                productId,
                variantId,
                productAttributeId
        );

        ApiResponse<Void> response =
                controller.removeAttributeValue(
                        businessId,
                        productId,
                        variantId,
                        productAttributeId
                );

        assertNotNull(response);

        assertEquals(
                "Variant attribute value removed successfully",
                response.getMessage()
        );

        verify(service).removeAttributeValue(
                businessId,
                productId,
                variantId,
                productAttributeId
        );
    }
}
