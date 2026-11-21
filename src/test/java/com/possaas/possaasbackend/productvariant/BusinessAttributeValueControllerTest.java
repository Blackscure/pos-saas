package com.possaas.possaasbackend.productvariant;

import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.productvariant.dto.BusinessAttributeValueResponse;
import com.possaas.possaasbackend.productvariant.dto.CreateBusinessAttributeValueRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessAttributeValueControllerTest {

    @Mock
    private BusinessAttributeValueService businessAttributeValueService;

    @InjectMocks
    private BusinessAttributeValueController businessAttributeValueController;

    private final UUID businessId = UUID.randomUUID();
    private final UUID attributeId = UUID.randomUUID();
    private final UUID valueId = UUID.randomUUID();

    // =========================
    // CREATE ATTRIBUTE VALUE
    // =========================

    @Test
    void createAttributeValue_shouldReturnSuccessResponse() {

        CreateBusinessAttributeValueRequest request =
                new CreateBusinessAttributeValueRequest("Red");

        BusinessAttributeValueResponse expectedResponse =
                mock(BusinessAttributeValueResponse.class);

        when(businessAttributeValueService.createAttributeValue(
                businessId,
                attributeId,
                request
        )).thenReturn(expectedResponse);

        ApiResponse<BusinessAttributeValueResponse> response =
                businessAttributeValueController.createAttributeValue(
                        businessId,
                        attributeId,
                        request
                );

        assertNotNull(response);

        verify(businessAttributeValueService).createAttributeValue(
                businessId,
                attributeId,
                request
        );
    }

    // =========================
    // GET ALL ATTRIBUTE VALUES
    // =========================

    @Test
    void getAttributeValues_shouldReturnList() {

        List<BusinessAttributeValueResponse> expectedResponse =
                List.of(
                        mock(BusinessAttributeValueResponse.class),
                        mock(BusinessAttributeValueResponse.class)
                );

        when(businessAttributeValueService.getAttributeValues(
                businessId,
                attributeId
        )).thenReturn(expectedResponse);

        ApiResponse<List<BusinessAttributeValueResponse>> response =
                businessAttributeValueController.getAttributeValues(
                        businessId,
                        attributeId
                );

        assertNotNull(response);

        verify(businessAttributeValueService).getAttributeValues(
                businessId,
                attributeId
        );
    }

    @Test
    void getAttributeValues_shouldReturnEmptyList() {

        when(businessAttributeValueService.getAttributeValues(
                businessId,
                attributeId
        )).thenReturn(List.of());

        ApiResponse<List<BusinessAttributeValueResponse>> response =
                businessAttributeValueController.getAttributeValues(
                        businessId,
                        attributeId
                );

        assertNotNull(response);

        verify(businessAttributeValueService).getAttributeValues(
                businessId,
                attributeId
        );
    }

    // =========================
    // GET ATTRIBUTE VALUE BY ID
    // =========================

    @Test
    void getAttributeValueById_shouldReturnSuccessResponse() {

        BusinessAttributeValueResponse expectedResponse =
                mock(BusinessAttributeValueResponse.class);

        when(businessAttributeValueService.getAttributeValueById(
                businessId,
                attributeId,
                valueId
        )).thenReturn(expectedResponse);

        ApiResponse<BusinessAttributeValueResponse> response =
                businessAttributeValueController.getAttributeValueById(
                        businessId,
                        attributeId,
                        valueId
                );

        assertNotNull(response);

        verify(businessAttributeValueService).getAttributeValueById(
                businessId,
                attributeId,
                valueId
        );
    }

    // =========================
    // UPDATE ATTRIBUTE VALUE
    // =========================

    @Test
    void updateAttributeValue_shouldReturnSuccessResponse() {

        CreateBusinessAttributeValueRequest request =
                new CreateBusinessAttributeValueRequest("Blue");

        BusinessAttributeValueResponse expectedResponse =
                mock(BusinessAttributeValueResponse.class);

        when(businessAttributeValueService.updateAttributeValue(
                businessId,
                attributeId,
                valueId,
                request
        )).thenReturn(expectedResponse);

        ApiResponse<BusinessAttributeValueResponse> response =
                businessAttributeValueController.updateAttributeValue(
                        businessId,
                        attributeId,
                        valueId,
                        request
                );

        assertNotNull(response);

        verify(businessAttributeValueService).updateAttributeValue(
                businessId,
                attributeId,
                valueId,
                request
        );
    }

    // =========================
    // DELETE ATTRIBUTE VALUE
    // =========================

    @Test
    void deleteAttributeValue_shouldReturnSuccessResponse() {

        doNothing().when(businessAttributeValueService).deleteAttributeValue(
                businessId,
                attributeId,
                valueId
        );

        ApiResponse<Void> response =
                businessAttributeValueController.deleteAttributeValue(
                        businessId,
                        attributeId,
                        valueId
                );

        assertNotNull(response);

        verify(businessAttributeValueService).deleteAttributeValue(
                businessId,
                attributeId,
                valueId
        );
    }
}