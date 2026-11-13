package com.possaas.possaasbackend.platformadmin;

import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessService;
import com.possaas.possaasbackend.business.dto.BusinessResponse;
import com.possaas.possaasbackend.common.pagination.PageResponse;
import com.possaas.possaasbackend.common.pagination.PaginationUtils;
import com.possaas.possaasbackend.common.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/platform/businesses")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class PlatformBusinessController {

    private final BusinessService businessService;

    public PlatformBusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    // Get all businesses across all tenants
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BusinessResponse>>> getAllBusinesses(
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_DIRECTION) String sortDir
    ) {
        Pageable pageable = PaginationUtils.createPageable(
                page, size, sortBy, sortDir
        );

        Page<Business> businessPage =
                businessService.getAllBusinesses(pageable);

        List<BusinessResponse> responses = businessPage.getContent()
                .stream()
                .map(BusinessResponse::fromEntity)
                .toList();

        PageResponse<BusinessResponse> pageResponse =
                PageResponse.of(businessPage, responses);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Businesses retrieved successfully",
                        pageResponse
                )
        );
    }

    // Get a business by ID across all tenants
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BusinessResponse>> getBusinessById(
            @PathVariable UUID id
    ) {
        Business business = businessService.getBusinessById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Business retrieved successfully",
                        BusinessResponse.fromEntity(business)
                )
        );
    }

    // Activate or suspend a business
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<BusinessResponse>> updateBusinessStatus(
            @PathVariable UUID id,
            @RequestParam boolean active
    ) {
        Business business =
                businessService.updatePlatformBusinessStatus(id, active);

        String message = active
                ? "Business activated successfully"
                : "Business suspended successfully";

        return ResponseEntity.ok(
                ApiResponse.success(
                        message,
                        BusinessResponse.fromEntity(business)
                )
        );
    }
}
