package com.possaas.possaasbackend.category;

import com.possaas.possaasbackend.category.dto.CategoryResponse;
import com.possaas.possaasbackend.category.dto.CreateCategoryRequest;
import com.possaas.possaasbackend.category.dto.UpdateCategoryRequest;
import com.possaas.possaasbackend.common.pagination.PageResponse;
import com.possaas.possaasbackend.common.pagination.PaginationUtils;
import com.possaas.possaasbackend.common.response.ApiResponse;
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
@RequestMapping("/api/businesses/{businessId}/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // Create a category
    @PostMapping
    @PreAuthorize("hasAuthority('CATEGORY_CREATE') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @PathVariable UUID businessId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateCategoryRequest request
    ) {

        Category category = categoryService.createCategory(
                authenticatedUser.tenantId(),
                businessId,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Category created successfully",
                        CategoryResponse.fromEntity(category)
                ));
    }

    // Get all categories with pagination
    @GetMapping
    @PreAuthorize("hasAuthority('CATEGORY_VIEW') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<PageResponse<CategoryResponse>>> getCategories(
            @PathVariable UUID businessId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_DIRECTION) String sortDir
    ) {

        Pageable pageable = PaginationUtils.createPageable(
                page, size, sortBy, sortDir
        );

        Page<Category> categoryPage = categoryService.getCategories(
                authenticatedUser.tenantId(),
                businessId,
                pageable
        );

        List<CategoryResponse> categoryResponses = categoryPage.getContent()
                .stream()
                .map(CategoryResponse::fromEntity)
                .toList();

        PageResponse<CategoryResponse> pageResponse =
                PageResponse.of(categoryPage, categoryResponses);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Categories fetched successfully",
                        pageResponse
                )
        );
    }

    // Get one category
    @GetMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('CATEGORY_VIEW') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @PathVariable UUID businessId,
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {

        Category category = categoryService.getCategoryById(
                authenticatedUser.tenantId(),
                businessId,
                categoryId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Category fetched successfully",
                        CategoryResponse.fromEntity(category)
                )
        );
    }

    // Update category details
    @PutMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('CATEGORY_UPDATE') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID businessId,
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {

        Category category = categoryService.updateCategory(
                authenticatedUser.tenantId(),
                businessId,
                categoryId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Category updated successfully",
                        CategoryResponse.fromEntity(category)
                )
        );
    }

    // Activate or deactivate a category
    @PatchMapping("/{categoryId}/status")
    @PreAuthorize("(hasAuthority('CATEGORY_UPDATE') or hasAuthority('CATEGORY_DELETE')) and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategoryStatus(
            @PathVariable UUID businessId,
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam boolean active
    ) {

        Category category = categoryService.updateCategoryStatus(
                authenticatedUser.tenantId(),
                businessId,
                categoryId,
                active
        );

        String message = active
                ? "Category activated successfully"
                : "Category deactivated successfully";

        return ResponseEntity.ok(
                ApiResponse.success(
                        message,
                        CategoryResponse.fromEntity(category)
                )
        );
    }

    // Soft-delete a category
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('CATEGORY_DELETE') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<CategoryResponse>> deleteCategory(
            @PathVariable UUID businessId,
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {

        Category category = categoryService.deleteCategory(
                authenticatedUser.tenantId(),
                businessId,
                categoryId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Category deleted successfully",
                        CategoryResponse.fromEntity(category)
                )
        );
    }
}
