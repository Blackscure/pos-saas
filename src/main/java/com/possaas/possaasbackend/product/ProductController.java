package com.possaas.possaasbackend.product;

import com.possaas.possaasbackend.common.pagination.PageResponse;
import com.possaas.possaasbackend.common.pagination.PaginationUtils;
import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.product.dto.CreateProductRequest;
import com.possaas.possaasbackend.product.dto.ProductResponse;
import com.possaas.possaasbackend.product.dto.UpdateProductRequest;
import com.possaas.possaasbackend.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/businesses/{businessId}/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_CREATE') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @PathVariable UUID businessId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateProductRequest request
    ) {

        Product product = productService.createProduct(
                authenticatedUser.tenantId(),
                businessId,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        "Product created successfully",
                        ProductResponse.fromEntity(product)
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_VIEW') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @PathVariable UUID businessId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_DIRECTION) String sortDir
    ) {

        Pageable pageable = PaginationUtils.createPageable(
                page,
                size,
                sortBy,
                sortDir
        );

        Page<Product> productPage = productService.getProducts(
                authenticatedUser.tenantId(),
                businessId,
                pageable
        );

        List<ProductResponse> responses = productPage.getContent()
                .stream()
                .map(ProductResponse::fromEntity)
                .toList();

        PageResponse<ProductResponse> pageResponse =
                PageResponse.of(productPage, responses);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Products fetched successfully",
                        pageResponse
                )
        );
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAuthority('PRODUCT_VIEW') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable UUID businessId,
            @PathVariable UUID productId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {

        Product product = productService.getProductById(
                authenticatedUser.tenantId(),
                businessId,
                productId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Product fetched successfully",
                        ProductResponse.fromEntity(product)
                )
        );
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable UUID businessId,
            @PathVariable UUID productId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody UpdateProductRequest request
    ) {

        Product product = productService.updateProduct(
                authenticatedUser.tenantId(),
                businessId,
                productId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Product updated successfully",
                        ProductResponse.fromEntity(product)
                )
        );
    }

    @PatchMapping("/{productId}/status")
    @PreAuthorize("(hasAuthority('PRODUCT_UPDATE') or hasAuthority('PRODUCT_DELETE')) and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProductStatus(
            @PathVariable UUID businessId,
            @PathVariable UUID productId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam boolean active
    ) {

        Product product = productService.updateProductStatus(
                authenticatedUser.tenantId(),
                businessId,
                productId,
                active
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Product status updated successfully",
                        ProductResponse.fromEntity(product)
                )
        );
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<ProductResponse>> deleteProduct(
            @PathVariable UUID businessId,
            @PathVariable UUID productId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {

        Product product = productService.deleteProduct(
                authenticatedUser.tenantId(),
                businessId,
                productId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Product deactivated successfully",
                        ProductResponse.fromEntity(product)
                )
        );
    }
}
