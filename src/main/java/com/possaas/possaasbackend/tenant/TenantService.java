package com.possaas.possaasbackend.tenant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    // Register a new tenant
    public Tenant createTenant(Tenant tenant) {

        if (tenantRepository.existsByCode(tenant.getCode())) {
            throw new IllegalArgumentException(
                    "A tenant with this business code already exists"
            );
        }

        if (tenantRepository.existsByEmail(tenant.getEmail())) {
            throw new IllegalArgumentException(
                    "A tenant with this email already exists"
            );
        }

        return tenantRepository.save(tenant);
    }

    // Get all tenants
    @Transactional(readOnly = true)
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    // Get tenant by ID
    @Transactional(readOnly = true)
    public Tenant getTenantById(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Tenant not found")
                );
    }

    // Get tenant by business code
    @Transactional(readOnly = true)
    public Tenant getTenantByCode(String code) {
        return tenantRepository.findByCode(code)
                .orElseThrow(() ->
                        new IllegalArgumentException("Tenant not found")
                );
    }

    // Activate or deactivate a tenant
    public Tenant updateTenantStatus(UUID id, boolean active) {
        Tenant tenant = getTenantById(id);
        tenant.setActive(active);

        return tenantRepository.save(tenant);
    }
}