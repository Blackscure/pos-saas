package com.possaas.possaasbackend.productvariant;

import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.productvariant.dto.AssignProductAttributeRequest;
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
class ProductAttributeControllerTest {

    @Mock
    private ProductAttributeService productAttributeService;

    @InjectMocks
    private ProductAttributeController controller;

    private UUID businessId;
    private UUID productId;
    private UUID productAttributeId;
    private UUID attributeId;

    @BeforeEach
    void setUp() {
        businessId = UUID.randomUUID();
        productId = UUID.randomUUID();
        productAttributeId = UUID.randomUUID();
        attributeId = UUID.randomUUID();
    }

    // --------------------------------------------------
    // ASSIGN ATTRIBUTE
    // --------------------------------------------------

    @Test
    void assignAttribute_shouldReturnSuccessResponse() {

        AssignProductAttributeRequest request =
                new AssignProductAttributeRequest(attributeId);

        ProductAttribute expected = mock(ProductAttribute.class);

        when(productAttributeService.assignAttribute(
                businessId,
                productId,
                request
        )).thenReturn(expected);

        ApiResponse<ProductAttribute> response =
                controller.assignAttribute(
                        businessId,
                        productId,
                        request
                );

        assertNotNull(response);
        assertEquals(
                "Attribute assigned to product successfully",
                response.getMessage()
        );
        assertSame(expected, response.getData());

        verify(productAttributeService).assignAttribute(
                businessId,
                productId,
                request
        );
    }

    // --------------------------------------------------
    // GET PRODUCT ATTRIBUTES
    // --------------------------------------------------

    @Test
    void getProductAttributes_shouldReturnAttributesSuccessfully() {

        ProductAttribute attribute1 = mock(ProductAttribute.class);
        ProductAttribute attribute2 = mock(ProductAttribute.class);

        List<ProductAttribute> expected =
                List.of(attribute1, attribute2);

        when(productAttributeService.getProductAttributes(
                businessId,
                productId
        )).thenReturn(expected);

        ApiResponse<List<ProductAttribute>> response =
                controller.getProductAttributes(
                        businessId,
                        productId
                );

        assertNotNull(response);
        assertEquals(
                "Product attributes retrieved successfully",
                response.getMessage()
        );
        assertEquals(expected, response.getData());
        assertEquals(2, response.getData().size());

        verify(productAttributeService).getProductAttributes(
                businessId,
                productId
        );
    }

    @Test
    void getProductAttributes_shouldReturnEmptyList() {

        when(productAttributeService.getProductAttributes(
                businessId,
                productId
        )).thenReturn(List.of());

        ApiResponse<List<ProductAttribute>> response =
                controller.getProductAttributes(
                        businessId,
                        productId
                );

        assertNotNull(response);
        assertNotNull(response.getData());
        assertTrue(response.getData().isEmpty());

        verify(productAttributeService).getProductAttributes(
                businessId,
                productId
        );
    }

    // --------------------------------------------------
    // GET PRODUCT ATTRIBUTE BY ID
    // --------------------------------------------------

    @Test
    void getProductAttributeById_shouldReturnAttributeSuccessfully() {

        ProductAttribute expected = mock(ProductAttribute.class);

        when(productAttributeService.getProductAttributeById(
                businessId,
                productId,
                productAttributeId
        )).thenReturn(expected);

        ApiResponse<ProductAttribute> response =
                controller.getProductAttributeById(
                        businessId,
                        productId,
                        productAttributeId
                );

        assertNotNull(response);
        assertEquals(
                "Product attribute retrieved successfully",
                response.getMessage()
        );
        assertSame(expected, response.getData());

        verify(productAttributeService).getProductAttributeById(
                businessId,
                productId,
                productAttributeId
        );
    }

    // --------------------------------------------------
    // REMOVE ATTRIBUTE
    // --------------------------------------------------

    @Test
    void removeAttribute_shouldReturnSuccessResponse() {

        doNothing().when(productAttributeService).removeAttribute(
                businessId,
                productId,
                productAttributeId
        );

        ApiResponse<Void> response =
                controller.removeAttribute(
                        businessId,
                        productId,
                        productAttributeId
                );

        assertNotNull(response);
        assertEquals(
                "Attribute removed from product successfully",
                response.getMessage()
        );

        verify(productAttributeService).removeAttribute(
                businessId,
                productId,
                productAttributeId
        );
    }
}
