package com.possaas.possaasbackend.productvariant;

import com.possaas.possaasbackend.common.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/businesses/{businessId}/products/{productId}/variants/{variantId}/attributes")
public class ProductVariantAttributeValueController {

    private final ProductVariantAttributeValueService service;

    public ProductVariantAttributeValueController(
            ProductVariantAttributeValueService service) {
        this.service = service;
    }

    @PutMapping("/{productAttributeId}/values/{attributeValueId}")
    @PreAuthorize("hasAuthority('VARIANT_UPDATE') and #businessId == authentication.principal.businessId")
    public ApiResponse<ProductVariantAttributeValue> setAttributeValue(
            @PathVariable UUID businessId,
            @PathVariable UUID productId,
            @PathVariable UUID variantId,
            @PathVariable UUID productAttributeId,
            @PathVariable UUID attributeValueId) {

        ProductVariantAttributeValue response = service.setAttributeValue(
                businessId,
                productId,
                variantId,
                productAttributeId,
                attributeValueId
        );

        return ApiResponse.success(
                "Variant attribute value assigned successfully",
                response
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VARIANT_VIEW') and #businessId == authentication.principal.businessId")
    public ApiResponse<List<ProductVariantAttributeValue>> getVariantAttributeValues(
            @PathVariable UUID businessId,
            @PathVariable UUID productId,
            @PathVariable UUID variantId) {

        List<ProductVariantAttributeValue> response =
                service.getVariantAttributeValues(
                        businessId, productId, variantId
                );

        return ApiResponse.success(
                "Variant attribute values retrieved successfully",
                response
        );
    }

    @DeleteMapping("/{productAttributeId}")
    @PreAuthorize("hasAuthority('VARIANT_UPDATE') and #businessId == authentication.principal.businessId")
    public ApiResponse<Void> removeAttributeValue(
            @PathVariable UUID businessId,
            @PathVariable UUID productId,
            @PathVariable UUID variantId,
            @PathVariable UUID productAttributeId) {

        service.removeAttributeValue(
                businessId,
                productId,
                variantId,
                productAttributeId
        );

        return ApiResponse.success(
                "Variant attribute value removed successfully",
                null
        );
    }
}
