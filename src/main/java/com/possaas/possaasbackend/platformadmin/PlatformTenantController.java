package com.possaas.possaasbackend.platformadmin;

import com.possaas.possaasbackend.common.pagination.PageResponse;
import com.possaas.possaasbackend.common.pagination.PaginationUtils;
import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.tenant.Tenant;
import com.possaas.possaasbackend.tenant.TenantService;
import com.possaas.possaasbackend.tenant.dto.TenantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/platform/tenants")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class PlatformTenantController {

    private final TenantService tenantService;

    public PlatformTenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TenantResponse>>> getAllTenants(
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_DIRECTION) String sortDir
    ) {
        Pageable pageable = PaginationUtils.createPageable(
                page, size, sortBy, sortDir
        );

        Page<Tenant> tenantPage = tenantService.getAllTenants(pageable);

        List<TenantResponse> responses = tenantPage.getContent()
                .stream()
                .map(TenantResponse::fromEntity)
                .toList();

        PageResponse<TenantResponse> pageResponse =
                PageResponse.of(tenantPage, responses);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tenants retrieved successfully",
                        pageResponse
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenantById(
            @PathVariable UUID id
    ) {
        Tenant tenant = tenantService.getTenantById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tenant retrieved successfully",
                        TenantResponse.fromEntity(tenant)
                )
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TenantResponse>> updateTenantStatus(
            @PathVariable UUID id,
            @RequestParam boolean active
    ) {
        Tenant tenant = tenantService.updateTenantStatus(id, active);

        String message = active
                ? "Tenant activated successfully"
                : "Tenant suspended successfully";

        return ResponseEntity.ok(
                ApiResponse.success(
                        message,
                        TenantResponse.fromEntity(tenant)
                )
        );
    }
}
