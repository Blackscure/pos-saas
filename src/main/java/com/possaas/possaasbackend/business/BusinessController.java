package com.possaas.possaasbackend.business;

import com.possaas.possaasbackend.business.dto.BusinessRequest;
import com.possaas.possaasbackend.business.dto.BusinessResponse;
import com.possaas.possaasbackend.common.pagination.PageResponse;
import com.possaas.possaasbackend.common.pagination.PaginationUtils;
import com.possaas.possaasbackend.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants/{tenantId}/businesses")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    // Create a business
    @PostMapping
    public ResponseEntity<ApiResponse<BusinessResponse>> createBusiness(
            @PathVariable UUID tenantId,
            @Valid @RequestBody BusinessRequest request) {

        Business createdBusiness = businessService.createBusiness(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Business created successfully", BusinessResponse.fromEntity(createdBusiness)));
    }

    // Get all businesses belonging to a tenant with pagination
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BusinessResponse>>> getBusinesses(
            @PathVariable UUID tenantId,
            @RequestParam(required = false) BusinessType businessType,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_DIRECTION) String sortDir) {

        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDir);
        Page<Business> businessPage;
        if (businessType != null) {
            businessPage = businessService.getBusinessesByTenantAndType(tenantId, businessType, pageable);
        } else {
            businessPage = businessService.getBusinessesByTenant(tenantId, pageable);
        }

        List<BusinessResponse> businessResponses = businessPage.getContent().stream()
                .map(BusinessResponse::fromEntity)
                .toList();
        PageResponse<BusinessResponse> pageResponse = PageResponse.of(businessPage, businessResponses);

        return ResponseEntity.ok(ApiResponse.success("Businesses fetched successfully", pageResponse));
    }

    // Get a specific business
    @GetMapping("/{businessId}")
    public ResponseEntity<ApiResponse<BusinessResponse>> getBusinessById(
            @PathVariable UUID tenantId,
            @PathVariable UUID businessId) {

        Business business = businessService.getBusinessById(tenantId, businessId);
        return ResponseEntity.ok(ApiResponse.success("Business fetched successfully", BusinessResponse.fromEntity(business)));
    }

    // Update business details
    @PutMapping("/{businessId}")
    public ResponseEntity<ApiResponse<BusinessResponse>> updateBusiness(
            @PathVariable UUID tenantId,
            @PathVariable UUID businessId,
            @Valid @RequestBody BusinessRequest request) {

        Business updatedBusiness = businessService.updateBusiness(tenantId, businessId, request);
        return ResponseEntity.ok(ApiResponse.success("Business updated successfully", BusinessResponse.fromEntity(updatedBusiness)));
    }

    // Activate or deactivate a business
    @PatchMapping("/{businessId}/status")
    public ResponseEntity<ApiResponse<BusinessResponse>> updateBusinessStatus(
            @PathVariable UUID tenantId,
            @PathVariable UUID businessId,
            @RequestParam boolean active) {

        Business business = businessService.updateBusinessStatus(tenantId, businessId, active);
        String message = active ? "Business activated successfully" : "Business deactivated successfully";
        return ResponseEntity.ok(ApiResponse.success(message, BusinessResponse.fromEntity(business)));
    }

    // Soft-delete a business
    @DeleteMapping("/{businessId}")
    public ResponseEntity<ApiResponse<BusinessResponse>> deleteBusiness(
            @PathVariable UUID tenantId,
            @PathVariable UUID businessId) {

        Business business = businessService.deleteBusiness(tenantId, businessId);
        return ResponseEntity.ok(ApiResponse.success("Business deleted successfully", BusinessResponse.fromEntity(business)));
    }
}
