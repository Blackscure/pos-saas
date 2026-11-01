package com.possaas.possaasbackend.tenant;

import com.possaas.possaasbackend.tenant.dto.TenantRequest;
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

    // Register a new tenant from request DTO
    public Tenant createTenant(TenantRequest request) {
        if (tenantRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException(
                    "A tenant with this business code already exists"
            );
        }

        if (tenantRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "A tenant with this email already exists"
            );
        }

        Tenant tenant = new Tenant();
        tenant.setName(request.getName());
        tenant.setCode(request.getCode());
        tenant.setEmail(request.getEmail());
        if (request.getActive() != null) {
            tenant.setActive(request.getActive());
        }

        return tenantRepository.save(tenant);
    }

    // Register a new tenant from entity
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

    // Update an existing tenant from request DTO
    public Tenant updateTenant(UUID id, TenantRequest request) {
        Tenant tenant = getTenantById(id);

        if (request.getCode() != null && !request.getCode().equalsIgnoreCase(tenant.getCode())) {
            if (tenantRepository.existsByCode(request.getCode())) {
                throw new IllegalArgumentException(
                        "A tenant with this business code already exists"
                );
            }
            tenant.setCode(request.getCode());
        }

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(tenant.getEmail())) {
            if (tenantRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException(
                        "A tenant with this email already exists"
                );
            }
            tenant.setEmail(request.getEmail());
        }

        if (request.getName() != null) {
            tenant.setName(request.getName());
        }

        if (request.getActive() != null) {
            tenant.setActive(request.getActive());
        }

        return tenantRepository.save(tenant);
    }

    // Update an existing tenant from entity
    public Tenant updateTenant(UUID id, Tenant updatedTenant) {
        Tenant tenant = getTenantById(id);

        if (updatedTenant.getCode() != null && !updatedTenant.getCode().equalsIgnoreCase(tenant.getCode())) {
            if (tenantRepository.existsByCode(updatedTenant.getCode())) {
                throw new IllegalArgumentException(
                        "A tenant with this business code already exists"
                );
            }
            tenant.setCode(updatedTenant.getCode());
        }

        if (updatedTenant.getEmail() != null && !updatedTenant.getEmail().equalsIgnoreCase(tenant.getEmail())) {
            if (tenantRepository.existsByEmail(updatedTenant.getEmail())) {
                throw new IllegalArgumentException(
                        "A tenant with this email already exists"
                );
            }
            tenant.setEmail(updatedTenant.getEmail());
        }

        if (updatedTenant.getName() != null) {
            tenant.setName(updatedTenant.getName());
        }

        tenant.setActive(updatedTenant.isActive());

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
