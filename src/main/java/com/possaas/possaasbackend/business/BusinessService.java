package com.possaas.possaasbackend.business;

import com.possaas.possaasbackend.business.dto.BusinessRequest;
import com.possaas.possaasbackend.tenant.Tenant;
import com.possaas.possaasbackend.tenant.TenantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final TenantRepository tenantRepository;

    public BusinessService(
            BusinessRepository businessRepository,
            TenantRepository tenantRepository) {
        this.businessRepository = businessRepository;
        this.tenantRepository = tenantRepository;
    }

    public Business createBusiness(UUID tenantId, BusinessRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        if (!tenant.isActive()) {
            throw new IllegalArgumentException("Cannot create a business under an inactive tenant");
        }

        if (businessRepository.existsByTenantIdAndName(tenantId, request.getName())) {
            throw new IllegalArgumentException("A business with this name already exists for this tenant");
        }

        if (request.getRegistrationNumber() != null && !request.getRegistrationNumber().isBlank()
                && businessRepository.existsByTenantIdAndRegistrationNumber(tenantId, request.getRegistrationNumber())) {
            throw new IllegalArgumentException("A business with this registration number already exists for this tenant");
        }

        Business business = new Business();
        business.setTenant(tenant);
        business.setName(request.getName());
        business.setBusinessType(request.getBusinessType());
        business.setRegistrationNumber(request.getRegistrationNumber());
        business.setPhone(request.getPhone());
        business.setEmail(request.getEmail());
        business.setAddress(request.getAddress());
        business.setCurrency(request.getCurrency() != null && !request.getCurrency().isBlank()
                ? request.getCurrency() : "KES");
        business.setTaxEnabled(request.getTaxEnabled() != null ? request.getTaxEnabled() : true);
        business.setActive(request.getActive() != null ? request.getActive() : true);

        return businessRepository.save(business);
    }

    public Business createBusiness(UUID tenantId, Business business) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Tenant not found"));

        if (!tenant.isActive()) {
            throw new IllegalArgumentException(
                    "Cannot create a business under an inactive tenant");
        }

        if (businessRepository.existsByTenantIdAndName(
                tenantId, business.getName())) {
            throw new IllegalArgumentException(
                    "A business with this name already exists for this tenant");
        }

        if (business.getRegistrationNumber() != null && !business.getRegistrationNumber().isBlank()
                && businessRepository.existsByTenantIdAndRegistrationNumber(tenantId, business.getRegistrationNumber())) {
            throw new IllegalArgumentException(
                    "A business with this registration number already exists for this tenant");
        }

        business.setTenant(tenant);
        if (business.getCurrency() == null || business.getCurrency().isBlank()) {
            business.setCurrency("KES");
        }
        business.setActive(true);

        return businessRepository.save(business);
    }

    @Transactional(readOnly = true)
    public Page<Business> getBusinessesByTenant(UUID tenantId, Pageable pageable) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new IllegalArgumentException("Tenant not found");
        }

        return businessRepository.findAllByTenantId(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Business> getBusinessesByTenantAndType(
            UUID tenantId,
            BusinessType businessType,
            Pageable pageable) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new IllegalArgumentException("Tenant not found");
        }

        return businessRepository.findAllByTenantIdAndBusinessType(tenantId, businessType, pageable);
    }

    @Transactional(readOnly = true)
    public List<Business> getBusinessesByTenant(UUID tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new IllegalArgumentException("Tenant not found");
        }

        return businessRepository.findAllByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public Business getBusinessById(UUID tenantId, UUID businessId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new IllegalArgumentException("Tenant not found");
        }

        return businessRepository
                .findByIdAndTenantId(businessId, tenantId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business not found"));
    }

    public Business updateBusiness(
            UUID tenantId,
            UUID businessId,
            BusinessRequest request) {
        Business existingBusiness = getBusinessById(tenantId, businessId);

        if (request.getName() != null && !request.getName().equals(existingBusiness.getName())) {
            if (businessRepository.existsByTenantIdAndName(tenantId, request.getName())) {
                throw new IllegalArgumentException("A business with this name already exists for this tenant");
            }
            existingBusiness.setName(request.getName());
        }

        if (request.getRegistrationNumber() != null
                && !request.getRegistrationNumber().isBlank()
                && !request.getRegistrationNumber().equals(existingBusiness.getRegistrationNumber())) {
            if (businessRepository.existsByTenantIdAndRegistrationNumber(tenantId, request.getRegistrationNumber())) {
                throw new IllegalArgumentException("A business with this registration number already exists for this tenant");
            }
            existingBusiness.setRegistrationNumber(request.getRegistrationNumber());
        }

        if (request.getBusinessType() != null) {
            existingBusiness.setBusinessType(request.getBusinessType());
        }
        if (request.getPhone() != null) {
            existingBusiness.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            existingBusiness.setEmail(request.getEmail());
        }
        if (request.getAddress() != null) {
            existingBusiness.setAddress(request.getAddress());
        }
        if (request.getCurrency() != null && !request.getCurrency().isBlank()) {
            existingBusiness.setCurrency(request.getCurrency());
        }
        if (request.getTaxEnabled() != null) {
            existingBusiness.setTaxEnabled(request.getTaxEnabled());
        }
        if (request.getActive() != null) {
            existingBusiness.setActive(request.getActive());
        }

        return businessRepository.save(existingBusiness);
    }

    public Business updateBusiness(
            UUID tenantId,
            UUID businessId,
            Business updatedBusiness) {
        Business existingBusiness = getBusinessById(tenantId, businessId);

        if (updatedBusiness.getName() != null && !existingBusiness.getName().equals(updatedBusiness.getName())
                && businessRepository.existsByTenantIdAndName(
                tenantId, updatedBusiness.getName())) {
            throw new IllegalArgumentException(
                    "A business with this name already exists for this tenant");
        }

        if (updatedBusiness.getRegistrationNumber() != null
                && !updatedBusiness.getRegistrationNumber().isBlank()
                && !updatedBusiness.getRegistrationNumber().equals(existingBusiness.getRegistrationNumber())
                && businessRepository.existsByTenantIdAndRegistrationNumber(
                tenantId, updatedBusiness.getRegistrationNumber())) {
            throw new IllegalArgumentException(
                    "A business with this registration number already exists for this tenant");
        }

        if (updatedBusiness.getName() != null) {
            existingBusiness.setName(updatedBusiness.getName());
        }
        if (updatedBusiness.getBusinessType() != null) {
            existingBusiness.setBusinessType(updatedBusiness.getBusinessType());
        }
        if (updatedBusiness.getRegistrationNumber() != null) {
            existingBusiness.setRegistrationNumber(updatedBusiness.getRegistrationNumber());
        }
        if (updatedBusiness.getPhone() != null) {
            existingBusiness.setPhone(updatedBusiness.getPhone());
        }
        if (updatedBusiness.getEmail() != null) {
            existingBusiness.setEmail(updatedBusiness.getEmail());
        }
        if (updatedBusiness.getAddress() != null) {
            existingBusiness.setAddress(updatedBusiness.getAddress());
        }
        if (updatedBusiness.getCurrency() != null && !updatedBusiness.getCurrency().isBlank()) {
            existingBusiness.setCurrency(updatedBusiness.getCurrency());
        }
        existingBusiness.setTaxEnabled(updatedBusiness.isTaxEnabled());
        existingBusiness.setActive(updatedBusiness.isActive());

        return businessRepository.save(existingBusiness);
    }

    public Business updateBusinessStatus(
            UUID tenantId,
            UUID businessId,
            boolean active) {
        Business business = getBusinessById(tenantId, businessId);
        business.setActive(active);

        return businessRepository.save(business);
    }

    public Business deleteBusiness(UUID tenantId, UUID businessId) {
        Business business = getBusinessById(tenantId, businessId);
        business.setActive(false);

        return businessRepository.save(business);
    }
}
