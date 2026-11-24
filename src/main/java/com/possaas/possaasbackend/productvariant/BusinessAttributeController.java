package com.possaas.possaasbackend.productvariant;

import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.productvariant.dto.BusinessAttributeResponse;
import com.possaas.possaasbackend.productvariant.dto.CreateBusinessAttributeRequest;
import com.possaas.possaasbackend.productvariant.dto.UpdateBusinessAttributeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/businesses/{businessId}/attributes")
@RequiredArgsConstructor
public class BusinessAttributeController {

    private final BusinessAttributeService businessAttributeService;

    @PostMapping
    @PreAuthorize("hasAuthority('ATTRIBUTE_CREATE') and #businessId == authentication.principal.businessId")
    public ApiResponse<BusinessAttributeResponse> createAttribute(
            @PathVariable UUID businessId,
            @RequestBody CreateBusinessAttributeRequest request
    ) {
        BusinessAttributeResponse response =
                businessAttributeService.createAttribute(businessId, request);

        return ApiResponse.success("Attribute created successfully", response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ATTRIBUTE_VIEW') and #businessId == authentication.principal.businessId")
    public ApiResponse<List<BusinessAttributeResponse>> getAllAttributes(
            @PathVariable UUID businessId
    ) {
        List<BusinessAttributeResponse> response =
                businessAttributeService.getAllAttributes(businessId);

        return ApiResponse.success("Attributes retrieved successfully", response);
    }

    @GetMapping("/{attributeId}")
    @PreAuthorize("hasAuthority('ATTRIBUTE_VIEW') and #businessId == authentication.principal.businessId")
    public ApiResponse<BusinessAttributeResponse> getAttributeById(
            @PathVariable UUID businessId,
            @PathVariable UUID attributeId
    ) {
        BusinessAttributeResponse response =
                businessAttributeService.getAttributeById(businessId, attributeId);

        return ApiResponse.success("Attribute retrieved successfully", response);
    }

    @PutMapping("/{attributeId}")
    @PreAuthorize("hasAuthority('ATTRIBUTE_UPDATE') and #businessId == authentication.principal.businessId")
    public ApiResponse<BusinessAttributeResponse> updateAttribute(
            @PathVariable UUID businessId,
            @PathVariable UUID attributeId,
            @RequestBody UpdateBusinessAttributeRequest request
    ) {
        BusinessAttributeResponse response =
                businessAttributeService.updateAttribute(
                        businessId,
                        attributeId,
                        request
                );

        return ApiResponse.success("Attribute updated successfully", response);
    }
}
