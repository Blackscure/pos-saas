package com.possaas.possaasbackend.tenant;

import com.possaas.possaasbackend.common.pagination.PageResponse;
import com.possaas.possaasbackend.common.pagination.PaginationUtils;
import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.tenant.dto.TenantRequest;
import com.possaas.possaasbackend.tenant.dto.TenantResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    // Register a new tenant
    @PostMapping
    public ResponseEntity<ApiResponse<TenantResponse>> createTenant(@Valid @RequestBody TenantRequest request) {
        Tenant tenant = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tenant registered successfully", TenantResponse.fromEntity(tenant)));
    }

    // Update an existing tenant
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponse>> updateTenant(
            @PathVariable UUID id,
            @Valid @RequestBody TenantRequest request
    ) {
        Tenant tenant = tenantService.updateTenant(id, request);
        return ResponseEntity.ok(ApiResponse.success("Tenant updated successfully", TenantResponse.fromEntity(tenant)));
    }

    // Get all tenants with pagination
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TenantResponse>>> getAllTenants(
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_DIRECTION) String sortDir
    ) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDir);
        Page<Tenant> tenantPage = tenantService.getAllTenants(pageable);
        List<TenantResponse> tenantResponses = tenantPage.getContent().stream()
                .map(TenantResponse::fromEntity)
                .toList();
        PageResponse<TenantResponse> pageResponse = PageResponse.of(tenantPage, tenantResponses);

        return ResponseEntity.ok(ApiResponse.success("Tenants fetched successfully", pageResponse));
    }

    // Get tenant by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenantById(@PathVariable UUID id) {
        Tenant tenant = tenantService.getTenantById(id);
        return ResponseEntity.ok(ApiResponse.success("Tenant fetched successfully", TenantResponse.fromEntity(tenant)));
    }

    // Activate or deactivate tenant
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TenantResponse>> updateTenantStatus(
            @PathVariable UUID id,
            @RequestParam boolean active
    ) {
        Tenant tenant = tenantService.updateTenantStatus(id, active);
        String message = active ? "Tenant activated successfully" : "Tenant deactivated successfully";
        return ResponseEntity.ok(ApiResponse.success(message, TenantResponse.fromEntity(tenant)));
    }

    // Delete a tenant (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantResponse>> deleteTenant(@PathVariable UUID id) {
        Tenant tenant = tenantService.deleteTenant(id);
        return ResponseEntity.ok(ApiResponse.success("Tenant deleted successfully", TenantResponse.fromEntity(tenant)));
    }
}
