package com.possaas.possaasbackend.supplier;

import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessRepository;
import com.possaas.possaasbackend.supplier.dto.CreateSupplierRequest;
import com.possaas.possaasbackend.supplier.dto.UpdateSupplierRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final BusinessRepository businessRepository;

    public SupplierService(
            SupplierRepository supplierRepository,
            BusinessRepository businessRepository
    ) {
        this.supplierRepository = supplierRepository;
        this.businessRepository = businessRepository;
    }

    public Supplier createSupplier(
            UUID tenantId,
            UUID businessId,
            CreateSupplierRequest request
    ) {
        Business business = getBusiness(tenantId, businessId);
        validateBusinessIsActive(business);

        String name = request.name().trim();

        if (supplierRepository.existsByBusiness_IdAndNameIgnoreCase(
                businessId, name
        )) {
            throw new IllegalArgumentException(
                    "Supplier with this name already exists"
            );
        }

        Supplier supplier = new Supplier();
        supplier.setBusiness(business);
        supplier.setName(name);
        supplier.setContactPerson(request.contactPerson());
        supplier.setPhone(request.phone());
        supplier.setEmail(request.email());
        supplier.setAddress(request.address());
        supplier.setTaxPin(request.taxPin());
        supplier.setPaymentTerms(request.paymentTerms());
        supplier.setNotes(request.notes());
        supplier.setActive(true);

        return supplierRepository.save(supplier);
    }

    @Transactional(readOnly = true)
    public Page<Supplier> getSuppliers(
            UUID tenantId,
            UUID businessId,
            String search,
            Pageable pageable
    ) {
        getBusiness(tenantId, businessId);

        String normalizedSearch = search == null || search.isBlank()
                ? null
                : search.trim();

        return supplierRepository.searchByBusiness(
                businessId,
                normalizedSearch,
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Supplier getSupplierById(
            UUID tenantId,
            UUID businessId,
            UUID supplierId
    ) {
        getBusiness(tenantId, businessId);

        return supplierRepository.findByIdAndBusiness_Id(
                supplierId,
                businessId
        ).orElseThrow(() ->
                new EntityNotFoundException("Supplier not found")
        );
    }

    public Supplier updateSupplier(
            UUID tenantId,
            UUID businessId,
            UUID supplierId,
            UpdateSupplierRequest request
    ) {
        Business business = getBusiness(tenantId, businessId);
        validateBusinessIsActive(business);

        Supplier supplier = getSupplierById(
                tenantId,
                businessId,
                supplierId
        );

        String name = request.name().trim();

        if (supplierRepository.existsByBusiness_IdAndNameIgnoreCaseAndIdNot(
                businessId,
                name,
                supplierId
        )) {
            throw new IllegalArgumentException(
                    "Supplier with this name already exists"
            );
        }

        supplier.setName(name);
        supplier.setContactPerson(request.contactPerson());
        supplier.setPhone(request.phone());
        supplier.setEmail(request.email());
        supplier.setAddress(request.address());
        supplier.setTaxPin(request.taxPin());
        supplier.setPaymentTerms(request.paymentTerms());
        supplier.setNotes(request.notes());

        return supplierRepository.save(supplier);
    }

    public Supplier updateSupplierStatus(
            UUID tenantId,
            UUID businessId,
            UUID supplierId,
            boolean active
    ) {
        Business business = getBusiness(tenantId, businessId);
        validateBusinessIsActive(business);

        Supplier supplier = getSupplierById(
                tenantId,
                businessId,
                supplierId
        );

        supplier.setActive(active);

        return supplierRepository.save(supplier);
    }

    public Supplier deleteSupplier(
            UUID tenantId,
            UUID businessId,
            UUID supplierId
    ) {
        return updateSupplierStatus(
                tenantId,
                businessId,
                supplierId,
                false
        );
    }

    private Business getBusiness(UUID tenantId, UUID businessId) {
        return businessRepository.findByIdAndTenantId(
                businessId,
                tenantId
        ).orElseThrow(() ->
                new EntityNotFoundException("Business not found")
        );
    }

    private void validateBusinessIsActive(Business business) {
        if (!business.isActive()) {
            throw new IllegalArgumentException("Business is inactive");
        }
    }
}
