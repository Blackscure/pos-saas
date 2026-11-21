
package com.possaas.possaasbackend.productvariant;

import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.productvariant.dto.CreateProductVariantRequest;
import com.possaas.possaasbackend.productvariant.dto.UpdateProductVariantRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/businesses/{businessId}/products/{productId}/variants")
public class ProductVariantController {

    private final ProductVariantService productVariantService;

    public ProductVariantController(ProductVariantService productVariantService) {
        this.productVariantService = productVariantService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VARIANT_CREATE') and #businessId == authentication.principal.businessId")
    public ApiResponse<ProductVariant> createVariant(
            @PathVariable UUID businessId,
            @PathVariable UUID productId,
            @Valid @RequestBody CreateProductVariantRequest request) {

        ProductVariant response = productVariantService.createVariant(
                businessId, productId, request
        );

        return ApiResponse.success("Product variant created successfully", response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VARIANT_VIEW') and #businessId == authentication.principal.businessId")
    public ApiResponse<List<ProductVariant>> getProductVariants(
            @PathVariable UUID businessId,
            @PathVariable UUID productId) {

        List<ProductVariant> response =
                productVariantService.getProductVariants(businessId, productId);

        return ApiResponse.success("Product variants retrieved successfully", response);
    }

    @GetMapping("/{variantId}")
    @PreAuthorize("hasAuthority('VARIANT_VIEW') and #businessId == authentication.principal.businessId")
    public ApiResponse<ProductVariant> getVariantById(
            @PathVariable UUID businessId,
            @PathVariable UUID productId,
            @PathVariable UUID variantId) {

        ProductVariant response = productVariantService.getVariantById(
                businessId, productId, variantId
        );

        return ApiResponse.success("Product variant retrieved successfully", response);
    }

    @PutMapping("/{variantId}")
    @PreAuthorize("hasAuthority('VARIANT_UPDATE') and #businessId == authentication.principal.businessId")
    public ApiResponse<ProductVariant> updateVariant(
            @PathVariable UUID businessId,
            @PathVariable UUID productId,
            @PathVariable UUID variantId,
            @Valid @RequestBody UpdateProductVariantRequest request) {

        ProductVariant response = productVariantService.updateVariant(
                businessId, productId, variantId, request
        );

        return ApiResponse.success("Product variant updated successfully", response);
    }

    @PatchMapping("/{variantId}/status")
    @PreAuthorize("hasAuthority('VARIANT_UPDATE') and #businessId == authentication.principal.businessId")
    public ApiResponse<ProductVariant> updateVariantStatus(
            @PathVariable UUID businessId,
            @PathVariable UUID productId,
            @PathVariable UUID variantId,
            @RequestParam boolean active) {

        ProductVariant response = productVariantService.updateVariantStatus(
                businessId, productId, variantId, active
        );

        return ApiResponse.success(
                active ? "Product variant activated successfully"
                        : "Product variant deactivated successfully",
                response
        );
    }
}
