package com.possaas.possaasbackend.productvariant;

import com.possaas.possaasbackend.productvariant.dto.BusinessAttributeResponse;
import com.possaas.possaasbackend.productvariant.dto.CreateBusinessAttributeRequest;
import com.possaas.possaasbackend.productvariant.dto.UpdateBusinessAttributeRequest;
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
class BusinessAttributeServiceTest {

    @Mock
    private BusinessAttributeRepository businessAttributeRepository;

    @InjectMocks
    private BusinessAttributeService businessAttributeService;

    private UUID businessId;
    private UUID attributeId;

    @BeforeEach
    void setUp() {
        businessId = UUID.randomUUID();
        attributeId = UUID.randomUUID();
    }

    @Test
    void shouldCreateAttributeSuccessfully() {
        CreateBusinessAttributeRequest request =
                new CreateBusinessAttributeRequest("Size", "size");

        when(businessAttributeRepository
                .existsByBusinessIdAndCodeIgnoreCase(businessId, "SIZE"))
                .thenReturn(false);

        when(businessAttributeRepository.save(any(BusinessAttribute.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BusinessAttributeResponse response =
                businessAttributeService.createAttribute(businessId, request);

        assertNotNull(response);
        assertEquals(businessId, response.businessId());
        assertEquals("Size", response.name());
        assertEquals("SIZE", response.code());

        verify(businessAttributeRepository).save(any(BusinessAttribute.class));
    }

    @Test
    void shouldNormalizeAttributeCodeToUppercase() {
        CreateBusinessAttributeRequest request =
                new CreateBusinessAttributeRequest("Color", " color ");

        when(businessAttributeRepository
                .existsByBusinessIdAndCodeIgnoreCase(businessId, "COLOR"))
                .thenReturn(false);

        when(businessAttributeRepository.save(any(BusinessAttribute.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BusinessAttributeResponse response =
                businessAttributeService.createAttribute(businessId, request);

        assertEquals("COLOR", response.code());
    }

    @Test
    void shouldRejectDuplicateAttributeCode() {
        CreateBusinessAttributeRequest request =
                new CreateBusinessAttributeRequest("Size", "SIZE");

        when(businessAttributeRepository
                .existsByBusinessIdAndCodeIgnoreCase(businessId, "SIZE"))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> businessAttributeService.createAttribute(businessId, request));

        verify(businessAttributeRepository, never()).save(any(BusinessAttribute.class));
    }

    @Test
    void shouldReturnAllBusinessAttributes() {
        BusinessAttribute size = createAttribute(
                UUID.randomUUID(), businessId, "Size", "SIZE");

        BusinessAttribute color = createAttribute(
                UUID.randomUUID(), businessId, "Color", "COLOR");

        when(businessAttributeRepository.findAllByBusinessId(businessId))
                .thenReturn(List.of(size, color));

        List<BusinessAttributeResponse> response =
                businessAttributeService.getAllAttributes(businessId);

        assertEquals(2, response.size());
        assertEquals("Size", response.get(0).name());
        assertEquals("COLOR", response.get(1).code());
    }

    @Test
    void shouldReturnEmptyListWhenBusinessHasNoAttributes() {
        when(businessAttributeRepository.findAllByBusinessId(businessId))
                .thenReturn(List.of());

        List<BusinessAttributeResponse> response =
                businessAttributeService.getAllAttributes(businessId);

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void shouldReturnAttributeById() {
        BusinessAttribute attribute = createAttribute(
                attributeId, businessId, "Size", "SIZE");

        when(businessAttributeRepository
                .findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.of(attribute));

        BusinessAttributeResponse response =
                businessAttributeService.getAttributeById(businessId, attributeId);

        assertEquals(attributeId, response.id());
        assertEquals(businessId, response.businessId());
        assertEquals("Size", response.name());
        assertEquals("SIZE", response.code());
    }

    @Test
    void shouldThrowExceptionWhenAttributeDoesNotExist() {
        when(businessAttributeRepository
                .findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> businessAttributeService.getAttributeById(businessId, attributeId));
    }

    @Test
    void shouldNotRetrieveAttributeFromAnotherBusiness() {
        UUID anotherBusinessId = UUID.randomUUID();

        when(businessAttributeRepository
                .findByIdAndBusinessId(attributeId, anotherBusinessId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> businessAttributeService.getAttributeById(
                        anotherBusinessId, attributeId));
    }

    @Test
    void shouldUpdateAttributeSuccessfully() {
        BusinessAttribute existingAttribute = createAttribute(
                attributeId, businessId, "Size", "SIZE");

        UpdateBusinessAttributeRequest request =
                new UpdateBusinessAttributeRequest("Product Size", "product_size");

        when(businessAttributeRepository
                .findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.of(existingAttribute));

        when(businessAttributeRepository
                .existsByBusinessIdAndCodeIgnoreCase(businessId, "PRODUCT_SIZE"))
                .thenReturn(false);

        when(businessAttributeRepository.save(any(BusinessAttribute.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BusinessAttributeResponse response =
                businessAttributeService.updateAttribute(
                        businessId, attributeId, request);

        assertEquals(attributeId, response.id());
        assertEquals("Product Size", response.name());
        assertEquals("PRODUCT_SIZE", response.code());

        verify(businessAttributeRepository).save(existingAttribute);
    }

    @Test
    void shouldTrimAttributeNameWhenUpdating() {
        BusinessAttribute existingAttribute = createAttribute(
                attributeId, businessId, "Size", "SIZE");

        UpdateBusinessAttributeRequest request =
                new UpdateBusinessAttributeRequest("  Product Size  ", "PRODUCT_SIZE");

        when(businessAttributeRepository
                .findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.of(existingAttribute));

        when(businessAttributeRepository
                .existsByBusinessIdAndCodeIgnoreCase(businessId, "PRODUCT_SIZE"))
                .thenReturn(false);

        when(businessAttributeRepository.save(any(BusinessAttribute.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BusinessAttributeResponse response =
                businessAttributeService.updateAttribute(
                        businessId, attributeId, request);

        assertEquals("Product Size", response.name());
    }

    @Test
    void shouldRejectDuplicateCodeWhenUpdatingAttribute() {
        BusinessAttribute existingAttribute = createAttribute(
                attributeId, businessId, "Size", "SIZE");

        UpdateBusinessAttributeRequest request =
                new UpdateBusinessAttributeRequest("Color", "COLOR");

        when(businessAttributeRepository
                .findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.of(existingAttribute));

        when(businessAttributeRepository
                .existsByBusinessIdAndCodeIgnoreCase(businessId, "COLOR"))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> businessAttributeService.updateAttribute(
                        businessId, attributeId, request));

        verify(businessAttributeRepository, never()).save(any(BusinessAttribute.class));
    }

    @Test
    void shouldAllowUpdatingAttributeWithoutChangingItsCode() {
        BusinessAttribute existingAttribute = createAttribute(
                attributeId, businessId, "Size", "SIZE");

        UpdateBusinessAttributeRequest request =
                new UpdateBusinessAttributeRequest("Product Size", "SIZE");

        when(businessAttributeRepository
                .findByIdAndBusinessId(attributeId, businessId))
                .thenReturn(Optional.of(existingAttribute));

        when(businessAttributeRepository
                .existsByBusinessIdAndCodeIgnoreCase(businessId, "SIZE"))
                .thenReturn(true);

        when(businessAttributeRepository.save(any(BusinessAttribute.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BusinessAttributeResponse response =
                businessAttributeService.updateAttribute(
                        businessId, attributeId, request);

        assertEquals("Product Size", response.name());
        assertEquals("SIZE", response.code());

        verify(businessAttributeRepository).save(existingAttribute);
    }

    private BusinessAttribute createAttribute(
            UUID id,
            UUID businessId,
            String name,
            String code
    ) {
        BusinessAttribute attribute = new BusinessAttribute();
        attribute.setId(id);
        attribute.setBusinessId(businessId);
        attribute.setName(name);
        attribute.setCode(code);
        return attribute;
    }
}
