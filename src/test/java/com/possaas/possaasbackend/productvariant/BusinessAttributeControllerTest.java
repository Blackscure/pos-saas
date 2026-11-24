package com.possaas.possaasbackend.productvariant;

import com.possaas.possaasbackend.common.response.ApiResponse;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessAttributeControllerTest {

    @Mock
    private BusinessAttributeService businessAttributeService;

    @InjectMocks
    private BusinessAttributeController controller;

    private UUID businessId;
    private UUID attributeId;

    @BeforeEach
    void setUp() {
        businessId = UUID.randomUUID();
        attributeId = UUID.randomUUID();
    }

    // --------------------------------------------------
    // CREATE ATTRIBUTE
    // --------------------------------------------------

    @Test
    void createAttribute_shouldReturnSuccessResponse() {

        CreateBusinessAttributeRequest request =
                new CreateBusinessAttributeRequest(
                        "Color",
                        "COLOR"
                );

        BusinessAttributeResponse expected =
                mock(BusinessAttributeResponse.class);

        when(businessAttributeService.createAttribute(
                businessId,
                request
        )).thenReturn(expected);

        ApiResponse<BusinessAttributeResponse> response =
                controller.createAttribute(
                        businessId,
                        request
                );

        assertNotNull(response);

        assertEquals(
                "Attribute created successfully",
                response.getMessage()
        );

        assertSame(expected, response.getData());

        verify(businessAttributeService).createAttribute(
                businessId,
                request
        );
    }

    // --------------------------------------------------
    // GET ALL ATTRIBUTES
    // --------------------------------------------------

    @Test
    void getAllAttributes_shouldReturnAttributesSuccessfully() {

        BusinessAttributeResponse attribute1 =
                mock(BusinessAttributeResponse.class);

        BusinessAttributeResponse attribute2 =
                mock(BusinessAttributeResponse.class);

        List<BusinessAttributeResponse> expected =
                List.of(attribute1, attribute2);

        when(businessAttributeService.getAllAttributes(businessId))
                .thenReturn(expected);

        ApiResponse<List<BusinessAttributeResponse>> response =
                controller.getAllAttributes(businessId);

        assertNotNull(response);

        assertEquals(
                "Attributes retrieved successfully",
                response.getMessage()
        );

        assertEquals(expected, response.getData());
        assertEquals(2, response.getData().size());

        verify(businessAttributeService).getAllAttributes(businessId);
    }

    @Test
    void getAllAttributes_shouldReturnEmptyList() {

        when(businessAttributeService.getAllAttributes(businessId))
                .thenReturn(List.of());

        ApiResponse<List<BusinessAttributeResponse>> response =
                controller.getAllAttributes(businessId);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertTrue(response.getData().isEmpty());

        verify(businessAttributeService).getAllAttributes(businessId);
    }

    // --------------------------------------------------
    // GET ATTRIBUTE BY ID
    // --------------------------------------------------

    @Test
    void getAttributeById_shouldReturnAttributeSuccessfully() {

        BusinessAttributeResponse expected =
                mock(BusinessAttributeResponse.class);

        when(businessAttributeService.getAttributeById(
                businessId,
                attributeId
        )).thenReturn(expected);

        ApiResponse<BusinessAttributeResponse> response =
                controller.getAttributeById(
                        businessId,
                        attributeId
                );

        assertNotNull(response);

        assertEquals(
                "Attribute retrieved successfully",
                response.getMessage()
        );

        assertSame(expected, response.getData());

        verify(businessAttributeService).getAttributeById(
                businessId,
                attributeId
        );
    }

    // --------------------------------------------------
    // UPDATE ATTRIBUTE
    // --------------------------------------------------

    @Test
    void updateAttribute_shouldReturnSuccessResponse() {

        UpdateBusinessAttributeRequest request =
                new UpdateBusinessAttributeRequest(
                        "Updated Color",
                        "UPDATED_COLOR"
                );

        BusinessAttributeResponse expected =
                mock(BusinessAttributeResponse.class);

        when(businessAttributeService.updateAttribute(
                businessId,
                attributeId,
                request
        )).thenReturn(expected);

        ApiResponse<BusinessAttributeResponse> response =
                controller.updateAttribute(
                        businessId,
                        attributeId,
                        request
                );

        assertNotNull(response);

        assertEquals(
                "Attribute updated successfully",
                response.getMessage()
        );

        assertSame(expected, response.getData());

        verify(businessAttributeService).updateAttribute(
                businessId,
                attributeId,
                request
        );
    }
}
