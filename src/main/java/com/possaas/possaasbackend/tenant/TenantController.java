package com.possaas.possaasbackend.tenant;

import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.tenant.dto.TenantRequest;
import com.possaas.possaasbackend.tenant.dto.TenantResponse;
import jakarta.validation.Valid;
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

    // Get all tenants
    @GetMapping
    public ResponseEntity<ApiResponse<List<TenantResponse>>> getAllTenants() {
        List<TenantResponse> tenants = tenantService.getAllTenants().stream()
                .map(TenantResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Tenants fetched successfully", tenants));
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
}
