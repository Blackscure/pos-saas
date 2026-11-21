package com.possaas.possaasbackend.productvariant;

import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.productvariant.dto.AssignProductAttributeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/businesses/{businessId}/products/{productId}/attributes")
@RequiredArgsConstructor
public class ProductAttributeController {

    private final ProductAttributeService productAttributeService;

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_ATTRIBUTE_ASSIGN') and #businessId == authentication.principal.businessId")
    public ApiResponse<ProductAttribute> assignAttribute(
            @PathVariable UUID businessId,
            @PathVariable UUID productId,
            @RequestBody AssignProductAttributeRequest request
    ) {
        ProductAttribute response =
                productAttributeService.assignAttribute(
                        businessId,
                        productId,
                        request
                );

        return ApiResponse.success("Attribute assigned to product successfully", response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_ATTRIBUTE_VIEW') and #businessId == authentication.principal.businessId")
    public ApiResponse<List<ProductAttribute>> getProductAttributes(
            @PathVariable UUID businessId,
            @PathVariable UUID productId
    ) {
        List<ProductAttribute> response =
                productAttributeService.getProductAttributes(
                        businessId,
                        productId
                );

        return ApiResponse.success("Product attributes retrieved successfully", response);
    }

    @GetMapping("/{productAttributeId}")
    @PreAuthorize("hasAuthority('PRODUCT_ATTRIBUTE_VIEW') and #businessId == authentication.principal.businessId")
    public ApiResponse<ProductAttribute> getProductAttributeById(
            @PathVariable UUID businessId,
            @PathVariable UUID productId,
            @PathVariable UUID productAttributeId
    ) {
        ProductAttribute response =
                productAttributeService.getProductAttributeById(
                        businessId,
                        productId,
                        productAttributeId
                );

        return ApiResponse.success("Product attribute retrieved successfully", response);
    }

    @DeleteMapping("/{productAttributeId}")
    @PreAuthorize("hasAuthority('PRODUCT_ATTRIBUTE_REMOVE') and #businessId == authentication.principal.businessId")
    public ApiResponse<Void> removeAttribute(
            @PathVariable UUID businessId,
            @PathVariable UUID productId,
            @PathVariable UUID productAttributeId
    ) {
        productAttributeService.removeAttribute(
                businessId,
                productId,
                productAttributeId
        );

        return ApiResponse.success("Attribute removed from product successfully", null);
    }
}
