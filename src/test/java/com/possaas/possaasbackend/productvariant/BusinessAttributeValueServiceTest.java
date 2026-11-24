
package com.possaas.possaasbackend.productvariant;

import com.possaas.possaasbackend.productvariant.dto.BusinessAttributeValueResponse;
import com.possaas.possaasbackend.productvariant.dto.CreateBusinessAttributeValueRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class BusinessAttributeValueServiceTest {

    @Mock
    private BusinessAttributeRepository businessAttributeRepository;

    @Mock
    private BusinessAttributeValueRepository businessAttributeValueRepository;

    @InjectMocks
    private BusinessAttributeValueService businessAttributeValueService;

    private UUID businessId;
    private UUID attributeId;
    private UUID valueId;

    private BusinessAttribute attribute;
    private BusinessAttributeValue attributeValue;

    @BeforeEach
    void setUp() {
        businessId = UUID.randomUUID();
        attributeId = UUID.randomUUID();
        valueId = UUID.randomUUID();

        attribute = new BusinessAttribute();
        attribute.setId(attributeId);
        attribute.setBusinessId(businessId);

        attributeValue = new BusinessAttributeValue();
        attributeValue.setId(valueId);
        attributeValue.setBusinessId(businessId);
        attributeValue.setAttributeId(attributeId);
        attributeValue.setValue("Red");
    }

    // CREATE

    @Test
    void shouldCreateAttributeValueSuccessfully() {
        CreateBusinessAttributeValueRequest request =
                new CreateBusinessAttributeValueRequest("Red");

        when(businessAttributeRepository.findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.of(attribute));

        when(businessAttributeValueRepository
                .existsByBusinessIdAndAttributeIdAndValueIgnoreCase(
                        businessId, attributeId, "Red"))
                .thenReturn(false);

        when(businessAttributeValueRepository.save(any(BusinessAttributeValue.class)))
                .thenReturn(attributeValue);

        BusinessAttributeValueResponse response =
                businessAttributeValueService.createAttributeValue(
                        businessId, attributeId, request);

        assertNotNull(response);
        assertEquals("Red", response.value());

        verify(businessAttributeValueRepository).save(any(BusinessAttributeValue.class));
    }

    @Test
    void shouldTrimValueWhenCreatingAttributeValue() {
        CreateBusinessAttributeValueRequest request =
                new CreateBusinessAttributeValueRequest("  Red  ");

        when(businessAttributeRepository.findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.of(attribute));

        when(businessAttributeValueRepository
                .existsByBusinessIdAndAttributeIdAndValueIgnoreCase(
                        businessId, attributeId, "Red"))
                .thenReturn(false);

        when(businessAttributeValueRepository.save(any(BusinessAttributeValue.class)))
                .thenReturn(attributeValue);

        businessAttributeValueService.createAttributeValue(
                businessId, attributeId, request);

        verify(businessAttributeValueRepository).save(argThat(value ->
                "Red".equals(value.getValue())
        ));
    }

    @Test
    void shouldRejectDuplicateAttributeValue() {
        CreateBusinessAttributeValueRequest request =
                new CreateBusinessAttributeValueRequest("Red");

        when(businessAttributeRepository.findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.of(attribute));

        when(businessAttributeValueRepository
                .existsByBusinessIdAndAttributeIdAndValueIgnoreCase(
                        businessId, attributeId, "Red"))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> businessAttributeValueService.createAttributeValue(
                        businessId, attributeId, request));

        verify(businessAttributeValueRepository, never())
                .save(any(BusinessAttributeValue.class));
    }

    @Test
    void shouldRejectCreationWhenAttributeDoesNotBelongToBusiness() {
        when(businessAttributeRepository.findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.empty());

        CreateBusinessAttributeValueRequest request =
                new CreateBusinessAttributeValueRequest("Red");

        assertThrows(IllegalArgumentException.class,
                () -> businessAttributeValueService.createAttributeValue(
                        businessId, attributeId, request));
    }

    // GET ALL

    @Test
    void shouldReturnAllAttributeValues() {
        when(businessAttributeRepository.findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.of(attribute));

        when(businessAttributeValueRepository
                .findAllByBusinessIdAndAttributeId(businessId, attributeId))
                .thenReturn(List.of(attributeValue));

        List<BusinessAttributeValueResponse> response =
                businessAttributeValueService.getAttributeValues(
                        businessId, attributeId);

        assertEquals(1, response.size());
        assertEquals("Red", response.get(0).value());
    }

    @Test
    void shouldReturnEmptyListWhenNoAttributeValuesExist() {
        when(businessAttributeRepository.findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.of(attribute));

        when(businessAttributeValueRepository
                .findAllByBusinessIdAndAttributeId(businessId, attributeId))
                .thenReturn(List.of());

        List<BusinessAttributeValueResponse> response =
                businessAttributeValueService.getAttributeValues(
                        businessId, attributeId);

        assertTrue(response.isEmpty());
    }

    @Test
    void shouldRejectGettingValuesForInvalidAttribute() {
        when(businessAttributeRepository.findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> businessAttributeValueService.getAttributeValues(
                        businessId, attributeId));
    }

    // GET BY ID

    @Test
    void shouldReturnAttributeValueById() {
        when(businessAttributeRepository.findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.of(attribute));

        when(businessAttributeValueRepository
                .findByIdAndBusinessIdAndAttributeId(valueId, businessId, attributeId))
                .thenReturn(Optional.of(attributeValue));

        BusinessAttributeValueResponse response =
                businessAttributeValueService.getAttributeValueById(
                        businessId, attributeId, valueId);

        assertEquals(valueId, response.id());
        assertEquals("Red", response.value());
    }

    @Test
    void shouldThrowExceptionWhenAttributeValueDoesNotExist() {
        when(businessAttributeRepository.findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.of(attribute));

        when(businessAttributeValueRepository
                .findByIdAndBusinessIdAndAttributeId(valueId, businessId, attributeId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> businessAttributeValueService.getAttributeValueById(
                        businessId, attributeId, valueId));
    }

    // UPDATE

    @Test
    void shouldUpdateAttributeValueSuccessfully() {
        CreateBusinessAttributeValueRequest request =
                new CreateBusinessAttributeValueRequest("Blue");

        BusinessAttributeValue updatedValue = new BusinessAttributeValue();
        updatedValue.setId(valueId);
        updatedValue.setBusinessId(businessId);
        updatedValue.setAttributeId(attributeId);
        updatedValue.setValue("Blue");

        when(businessAttributeRepository.findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.of(attribute));

        when(businessAttributeValueRepository
                .findByIdAndBusinessIdAndAttributeId(valueId, businessId, attributeId))
                .thenReturn(Optional.of(attributeValue));

        when(businessAttributeValueRepository
                .existsByBusinessIdAndAttributeIdAndValueIgnoreCase(
                        businessId, attributeId, "Blue"))
                .thenReturn(false);

        when(businessAttributeValueRepository.save(attributeValue))
                .thenReturn(updatedValue);

        BusinessAttributeValueResponse response =
                businessAttributeValueService.updateAttributeValue(
                        businessId, attributeId, valueId, request);

        assertEquals("Blue", response.value());
    }

    @Test
    void shouldTrimValueWhenUpdatingAttributeValue() {
        CreateBusinessAttributeValueRequest request =
                new CreateBusinessAttributeValueRequest("  Blue  ");

        when(businessAttributeRepository.findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.of(attribute));

        when(businessAttributeValueRepository
                .findByIdAndBusinessIdAndAttributeId(valueId, businessId, attributeId))
                .thenReturn(Optional.of(attributeValue));

        when(businessAttributeValueRepository
                .existsByBusinessIdAndAttributeIdAndValueIgnoreCase(
                        businessId, attributeId, "Blue"))
                .thenReturn(false);

        when(businessAttributeValueRepository.save(attributeValue))
                .thenReturn(attributeValue);

        businessAttributeValueService.updateAttributeValue(
                businessId, attributeId, valueId, request);

        assertEquals("Blue", attributeValue.getValue());
    }

    @Test
    void shouldRejectDuplicateValueWhenUpdating() {
        CreateBusinessAttributeValueRequest request =
                new CreateBusinessAttributeValueRequest("Blue");

        BusinessAttributeValue anotherValue = new BusinessAttributeValue();
        anotherValue.setId(UUID.randomUUID());
        anotherValue.setBusinessId(businessId);
        anotherValue.setAttributeId(attributeId);
        anotherValue.setValue("Red");

        when(businessAttributeRepository.findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.of(attribute));

        when(businessAttributeValueRepository
                .findByIdAndBusinessIdAndAttributeId(valueId, businessId, attributeId))
                .thenReturn(Optional.of(anotherValue));

        when(businessAttributeValueRepository
                .existsByBusinessIdAndAttributeIdAndValueIgnoreCase(
                        businessId, attributeId, "Blue"))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> businessAttributeValueService.updateAttributeValue(
                        businessId, attributeId, valueId, request));

        verify(businessAttributeValueRepository, never())
                .save(any(BusinessAttributeValue.class));
    }

    @Test
    void shouldAllowUpdatingValueWithoutChangingIt() {
        CreateBusinessAttributeValueRequest request =
                new CreateBusinessAttributeValueRequest("Red");

        when(businessAttributeRepository.findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.of(attribute));

        when(businessAttributeValueRepository
                .findByIdAndBusinessIdAndAttributeId(valueId, businessId, attributeId))
                .thenReturn(Optional.of(attributeValue));

        when(businessAttributeValueRepository
                .existsByBusinessIdAndAttributeIdAndValueIgnoreCase(
                        businessId, attributeId, "Red"))
                .thenReturn(true);

        when(businessAttributeValueRepository.save(attributeValue))
                .thenReturn(attributeValue);

        BusinessAttributeValueResponse response =
                businessAttributeValueService.updateAttributeValue(
                        businessId, attributeId, valueId, request);

        assertEquals("Red", response.value());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonexistentValue() {
        CreateBusinessAttributeValueRequest request =
                new CreateBusinessAttributeValueRequest("Blue");

        when(businessAttributeRepository.findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.of(attribute));

        when(businessAttributeValueRepository
                .findByIdAndBusinessIdAndAttributeId(valueId, businessId, attributeId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> businessAttributeValueService.updateAttributeValue(
                        businessId, attributeId, valueId, request));
    }

    // DELETE

    @Test
    void shouldDeleteAttributeValueSuccessfully() {
        when(businessAttributeRepository.findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.of(attribute));

        when(businessAttributeValueRepository
                .findByIdAndBusinessIdAndAttributeId(valueId, businessId, attributeId))
                .thenReturn(Optional.of(attributeValue));

        businessAttributeValueService.deleteAttributeValue(
                businessId, attributeId, valueId);

        verify(businessAttributeValueRepository).delete(attributeValue);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonexistentValue() {
        when(businessAttributeRepository.findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.of(attribute));

        when(businessAttributeValueRepository
                .findByIdAndBusinessIdAndAttributeId(valueId, businessId, attributeId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> businessAttributeValueService.deleteAttributeValue(
                        businessId, attributeId, valueId));

        verify(businessAttributeValueRepository, never())
                .delete(any(BusinessAttributeValue.class));
    }
}
