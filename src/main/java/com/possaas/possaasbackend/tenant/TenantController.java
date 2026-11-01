package com.possaas.possaasbackend.tenant;

import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.CREATED)
    public Tenant createTenant(@RequestBody Tenant tenant) {
        return tenantService.createTenant(tenant);
    }

    // Get all tenants
    @GetMapping
    public List<Tenant> getAllTenants() {
        return tenantService.getAllTenants();
    }

    // Get tenant by ID
    @GetMapping("/{id}")
    public Tenant getTenantById(@PathVariable UUID id) {
        return tenantService.getTenantById(id);
    }

    // Activate or deactivate tenant
    @PatchMapping("/{id}/status")
    public Tenant updateTenantStatus(
            @PathVariable UUID id,
            @RequestParam boolean active
    ) {
        return tenantService.updateTenantStatus(id, active);
    }
}