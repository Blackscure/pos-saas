package com.possaas.possaasbackend.productvariant;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductVariantAttributeValueServiceTest {

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private ProductAttributeRepository productAttributeRepository;

    @Mock
    private BusinessAttributeValueRepository businessAttributeValueRepository;

    @Mock
    private ProductVariantAttributeValueRepository productVariantAttributeValueRepository;

    @InjectMocks
    private ProductVariantAttributeValueService service;

    private UUID businessId;
    private UUID productId;
    private UUID variantId;
    private UUID productAttributeId;
    private UUID attributeId;
    private UUID attributeValueId;

    private ProductVariant variant;
    private ProductAttribute productAttribute;
    private BusinessAttributeValue attributeValue;

    @BeforeEach
    void setUp() {
        businessId = UUID.randomUUID();
        productId = UUID.randomUUID();
        variantId = UUID.randomUUID();
        productAttributeId = UUID.randomUUID();
        attributeId = UUID.randomUUID();
        attributeValueId = UUID.randomUUID();

        variant = mock(ProductVariant.class);
        productAttribute = mock(ProductAttribute.class);
        attributeValue = mock(BusinessAttributeValue.class);
    }

    // --------------------------------------------------
    // SET ATTRIBUTE VALUE
    // --------------------------------------------------

    @Test
    void setAttributeValue_shouldSaveNewAttributeValueSuccessfully() {

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId
        )).thenReturn(Optional.of(variant));

        when(productAttributeRepository.findByIdAndBusinessIdAndProductId(
                productAttributeId, businessId, productId
        )).thenReturn(Optional.of(productAttribute));

        when(productAttribute.getAttributeId()).thenReturn(attributeId);

        when(businessAttributeValueRepository.findByIdAndBusinessIdAndAttributeId(
                attributeValueId, businessId, attributeId
        )).thenReturn(Optional.of(attributeValue));

        when(attributeValue.getId()).thenReturn(attributeValueId);

        when(productVariantAttributeValueRepository
                .findAllByBusinessIdAndProductIdAndProductVariantId(
                        businessId, productId, variantId
                )).thenReturn(List.of());

        when(productVariantAttributeValueRepository.save(
                any(ProductVariantAttributeValue.class)
        )).thenAnswer(invocation -> invocation.getArgument(0));

        ProductVariantAttributeValue result = service.setAttributeValue(
                businessId,
                productId,
                variantId,
                productAttributeId,
                attributeValueId
        );

        assertNotNull(result);
        assertNotNull(result.getId());

        assertEquals(businessId, result.getBusinessId());
        assertEquals(productId, result.getProductId());
        assertEquals(variantId, result.getProductVariantId());
        assertEquals(productAttributeId, result.getProductAttributeId());
        assertEquals(attributeId, result.getAttributeId());
        assertEquals(attributeValueId, result.getAttributeValueId());

        verify(productVariantAttributeValueRepository).save(any(
                ProductVariantAttributeValue.class
        ));
    }

    @Test
    void setAttributeValue_shouldThrowWhenVariantDoesNotExist() {

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId
        )).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                service.setAttributeValue(
                        businessId,
                        productId,
                        variantId,
                        productAttributeId,
                        attributeValueId
                ));

        verify(productAttributeRepository, never())
                .findByIdAndBusinessIdAndProductId(any(), any(), any());

        verify(productVariantAttributeValueRepository, never())
                .save(any());
    }

    @Test
    void setAttributeValue_shouldThrowWhenProductAttributeDoesNotExist() {

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId
        )).thenReturn(Optional.of(variant));

        when(productAttributeRepository.findByIdAndBusinessIdAndProductId(
                productAttributeId, businessId, productId
        )).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                service.setAttributeValue(
                        businessId,
                        productId,
                        variantId,
                        productAttributeId,
                        attributeValueId
                ));

        verify(businessAttributeValueRepository, never())
                .findByIdAndBusinessIdAndAttributeId(any(), any(), any());

        verify(productVariantAttributeValueRepository, never())
                .save(any());
    }

    @Test
    void setAttributeValue_shouldThrowWhenAttributeValueDoesNotExist() {

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId
        )).thenReturn(Optional.of(variant));

        when(productAttributeRepository.findByIdAndBusinessIdAndProductId(
                productAttributeId, businessId, productId
        )).thenReturn(Optional.of(productAttribute));

        when(productAttribute.getAttributeId()).thenReturn(attributeId);

        when(businessAttributeValueRepository.findByIdAndBusinessIdAndAttributeId(
                attributeValueId, businessId, attributeId
        )).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                service.setAttributeValue(
                        businessId,
                        productId,
                        variantId,
                        productAttributeId,
                        attributeValueId
                ));

        verify(productVariantAttributeValueRepository, never())
                .save(any());
    }

    @Test
    void setAttributeValue_shouldUpdateExistingAttributeValueInsteadOfCreatingNew() {

        ProductVariantAttributeValue existingValue =
                mock(ProductVariantAttributeValue.class);

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId
        )).thenReturn(Optional.of(variant));

        when(productAttributeRepository.findByIdAndBusinessIdAndProductId(
                productAttributeId, businessId, productId
        )).thenReturn(Optional.of(productAttribute));

        when(productAttribute.getAttributeId()).thenReturn(attributeId);

        when(businessAttributeValueRepository.findByIdAndBusinessIdAndAttributeId(
                attributeValueId, businessId, attributeId
        )).thenReturn(Optional.of(attributeValue));

        when(attributeValue.getId()).thenReturn(attributeValueId);

        when(productVariantAttributeValueRepository
                .findAllByBusinessIdAndProductIdAndProductVariantId(
                        businessId, productId, variantId
                )).thenReturn(List.of(existingValue));

        when(existingValue.getProductAttributeId())
                .thenReturn(productAttributeId);

        when(productVariantAttributeValueRepository.save(existingValue))
                .thenReturn(existingValue);

        ProductVariantAttributeValue result = service.setAttributeValue(
                businessId,
                productId,
                variantId,
                productAttributeId,
                attributeValueId
        );

        assertSame(existingValue, result);

        verify(existingValue).setBusinessId(businessId);
        verify(existingValue).setProductId(productId);
        verify(existingValue).setProductVariantId(variantId);
        verify(existingValue).setProductAttributeId(productAttributeId);
        verify(existingValue).setAttributeId(attributeId);
        verify(existingValue).setAttributeValueId(attributeValueId);

        verify(productVariantAttributeValueRepository)
                .save(existingValue);
    }

    // --------------------------------------------------
    // GET ATTRIBUTE VALUES
    // --------------------------------------------------

    @Test
    void getVariantAttributeValues_shouldReturnAttributeValuesSuccessfully() {

        ProductVariantAttributeValue value1 =
                mock(ProductVariantAttributeValue.class);

        ProductVariantAttributeValue value2 =
                mock(ProductVariantAttributeValue.class);

        List<ProductVariantAttributeValue> expected =
                List.of(value1, value2);

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId
        )).thenReturn(Optional.of(variant));

        when(productVariantAttributeValueRepository
                .findAllByBusinessIdAndProductIdAndProductVariantId(
                        businessId, productId, variantId
                )).thenReturn(expected);

        List<ProductVariantAttributeValue> result =
                service.getVariantAttributeValues(
                        businessId,
                        productId,
                        variantId
                );

        assertEquals(2, result.size());
        assertSame(expected, result);

        verify(productVariantAttributeValueRepository)
                .findAllByBusinessIdAndProductIdAndProductVariantId(
                        businessId, productId, variantId
                );
    }

    @Test
    void getVariantAttributeValues_shouldReturnEmptyListWhenNoValuesExist() {

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId
        )).thenReturn(Optional.of(variant));

        when(productVariantAttributeValueRepository
                .findAllByBusinessIdAndProductIdAndProductVariantId(
                        businessId, productId, variantId
                )).thenReturn(List.of());

        List<ProductVariantAttributeValue> result =
                service.getVariantAttributeValues(
                        businessId,
                        productId,
                        variantId
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getVariantAttributeValues_shouldThrowWhenVariantDoesNotExist() {

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId
        )).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                service.getVariantAttributeValues(
                        businessId,
                        productId,
                        variantId
                ));

        verify(productVariantAttributeValueRepository, never())
                .findAllByBusinessIdAndProductIdAndProductVariantId(
                        any(), any(), any()
                );
    }

    // --------------------------------------------------
    // REMOVE ATTRIBUTE VALUE
    // --------------------------------------------------

    @Test
    void removeAttributeValue_shouldDeleteSuccessfully() {

        ProductVariantAttributeValue existingValue =
                mock(ProductVariantAttributeValue.class);

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId
        )).thenReturn(Optional.of(variant));

        when(productVariantAttributeValueRepository
                .findAllByBusinessIdAndProductIdAndProductVariantId(
                        businessId, productId, variantId
                )).thenReturn(List.of(existingValue));

        when(existingValue.getProductAttributeId())
                .thenReturn(productAttributeId);

        service.removeAttributeValue(
                businessId,
                productId,
                variantId,
                productAttributeId
        );

        verify(productVariantAttributeValueRepository)
                .delete(existingValue);
    }

    @Test
    void removeAttributeValue_shouldThrowWhenVariantDoesNotExist() {

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId
        )).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                service.removeAttributeValue(
                        businessId,
                        productId,
                        variantId,
                        productAttributeId
                ));

        verify(productVariantAttributeValueRepository, never())
                .delete(any());
    }

    @Test
    void removeAttributeValue_shouldThrowWhenAttributeValueAssignmentDoesNotExist() {

        when(productVariantRepository.findByIdAndBusinessIdAndProductId(
                variantId, businessId, productId
        )).thenReturn(Optional.of(variant));

        when(productVariantAttributeValueRepository
                .findAllByBusinessIdAndProductIdAndProductVariantId(
                        businessId, productId, variantId
                )).thenReturn(List.of());

        assertThrows(EntityNotFoundException.class, () ->
                service.removeAttributeValue(
                        businessId,
                        productId,
                        variantId,
                        productAttributeId
                ));

        verify(productVariantAttributeValueRepository, never())
                .delete(any());
    }
}
