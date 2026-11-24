package com.possaas.possaasbackend.productvariant;

import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.productvariant.dto.BusinessAttributeValueResponse;
import com.possaas.possaasbackend.productvariant.dto.CreateBusinessAttributeValueRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/businesses/{businessId}/attributes/{attributeId}/values")
@RequiredArgsConstructor
public class BusinessAttributeValueController {

    private final BusinessAttributeValueService businessAttributeValueService;

    @PostMapping
    @PreAuthorize("hasAuthority('ATTRIBUTE_VALUE_CREATE') and #businessId == authentication.principal.businessId")
    public ApiResponse<BusinessAttributeValueResponse> createAttributeValue(
            @PathVariable UUID businessId,
            @PathVariable UUID attributeId,
            @RequestBody CreateBusinessAttributeValueRequest request
    ) {
        BusinessAttributeValueResponse response =
                businessAttributeValueService.createAttributeValue(
                        businessId,
                        attributeId,
                        request
                );

        return ApiResponse.success("Attribute value created successfully", response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ATTRIBUTE_VALUE_VIEW') and #businessId == authentication.principal.businessId")
    public ApiResponse<List<BusinessAttributeValueResponse>> getAttributeValues(
            @PathVariable UUID businessId,
            @PathVariable UUID attributeId
    ) {
        List<BusinessAttributeValueResponse> response =
                businessAttributeValueService.getAttributeValues(
                        businessId,
                        attributeId
                );

        return ApiResponse.success("Attribute values retrieved successfully", response);
    }

    @GetMapping("/{valueId}")
    @PreAuthorize("hasAuthority('ATTRIBUTE_VALUE_VIEW') and #businessId == authentication.principal.businessId")
    public ApiResponse<BusinessAttributeValueResponse> getAttributeValueById(
            @PathVariable UUID businessId,
            @PathVariable UUID attributeId,
            @PathVariable UUID valueId
    ) {
        BusinessAttributeValueResponse response =
                businessAttributeValueService.getAttributeValueById(
                        businessId,
                        attributeId,
                        valueId
                );

        return ApiResponse.success("Attribute value retrieved successfully", response);
    }

    @PutMapping("/{valueId}")
    @PreAuthorize("hasAuthority('ATTRIBUTE_VALUE_UPDATE') and #businessId == authentication.principal.businessId")
    public ApiResponse<BusinessAttributeValueResponse> updateAttributeValue(
            @PathVariable UUID businessId,
            @PathVariable UUID attributeId,
            @PathVariable UUID valueId,
            @RequestBody CreateBusinessAttributeValueRequest request
    ) {
        BusinessAttributeValueResponse response =
                businessAttributeValueService.updateAttributeValue(
                        businessId,
                        attributeId,
                        valueId,
                        request
                );

        return ApiResponse.success("Attribute value updated successfully", response);
    }

    @DeleteMapping("/{valueId}")
    @PreAuthorize("hasAuthority('ATTRIBUTE_VALUE_DELETE') and #businessId == authentication.principal.businessId")
    public ApiResponse<Void> deleteAttributeValue(
            @PathVariable UUID businessId,
            @PathVariable UUID attributeId,
            @PathVariable UUID valueId
    ) {
        businessAttributeValueService.deleteAttributeValue(
                businessId,
                attributeId,
                valueId
        );

        return ApiResponse.success("Attribute value deleted successfully", null);
    }
}
