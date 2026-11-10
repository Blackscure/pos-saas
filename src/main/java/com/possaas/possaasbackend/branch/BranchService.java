
package com.possaas.possaasbackend.branch;

import com.possaas.possaasbackend.branch.dto.CreateBranchRequest;
import com.possaas.possaasbackend.branch.dto.UpdateBranchRequest;
import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class BranchService {

    private final BranchRepository branchRepository;
    private final BusinessRepository businessRepository;

    public BranchService(
            BranchRepository branchRepository,
            BusinessRepository businessRepository
    ) {
        this.branchRepository = branchRepository;
        this.businessRepository = businessRepository;
    }

    private Business getBusinessByTenant(
            UUID tenantId,
            UUID businessId
    ) {
        return businessRepository.findByIdAndTenantId(
                businessId,
                tenantId
        ).orElseThrow(() ->
                new IllegalArgumentException("Business not found"));
    }

    public Branch createBranch(
            UUID tenantId,
            UUID businessId,
            CreateBranchRequest request
    ) {
        Business business = getBusinessByTenant(tenantId, businessId);

        if (!business.isActive()) {
            throw new IllegalArgumentException(
                    "Cannot create a branch under an inactive business"
            );
        }

        if (branchRepository.existsByBusiness_IdAndName(
                businessId, request.name())) {
            throw new IllegalArgumentException(
                    "Branch name already exists"
            );
        }

        if (branchRepository.existsByBusiness_IdAndCode(
                businessId, request.code())) {
            throw new IllegalArgumentException(
                    "Branch code already exists"
            );
        }

        Branch branch = new Branch();
        branch.setName(request.name());
        branch.setCode(request.code());
        branch.setPhone(request.phone());
        branch.setEmail(request.email());
        branch.setAddress(request.address());
        branch.setBusiness(business);
        branch.setActive(true);

        return branchRepository.save(branch);
    }

    @Transactional(readOnly = true)
    public Page<Branch> getBranchesByBusiness(
            UUID tenantId,
            UUID businessId,
            Pageable pageable
    ) {
        getBusinessByTenant(tenantId, businessId);

        return branchRepository.findAllByBusiness_Id(
                businessId, pageable
        );
    }

    @Transactional(readOnly = true)
    public Branch getBranchById(
            UUID tenantId,
            UUID businessId,
            UUID branchId
    ) {
        getBusinessByTenant(tenantId, businessId);

        return branchRepository.findByIdAndBusiness_Id(
                branchId, businessId
        ).orElseThrow(() ->
                new IllegalArgumentException("Branch not found"));
    }

    public Branch updateBranch(
            UUID tenantId,
            UUID businessId,
            UUID branchId,
            UpdateBranchRequest request
    ) {
        getBusinessByTenant(tenantId, businessId);

        Branch branch = getBranchById(
                tenantId, businessId, branchId
        );

        if (!branch.getName().equals(request.name())
                && branchRepository.existsByBusiness_IdAndName(
                businessId, request.name())) {
            throw new IllegalArgumentException(
                    "Branch name already exists"
            );
        }

        if (!branch.getCode().equals(request.code())
                && branchRepository.existsByBusiness_IdAndCode(
                businessId, request.code())) {
            throw new IllegalArgumentException(
                    "Branch code already exists"
            );
        }

        branch.setName(request.name());
        branch.setCode(request.code());
        branch.setPhone(request.phone());
        branch.setEmail(request.email());
        branch.setAddress(request.address());

        return branchRepository.save(branch);
    }

    public Branch updateBranchStatus(
            UUID tenantId,
            UUID businessId,
            UUID branchId,
            boolean active
    ) {
        Business business = getBusinessByTenant(tenantId, businessId);

        Branch branch = getBranchById(
                tenantId, businessId, branchId
        );

        if (active && !business.isActive()) {
            throw new IllegalArgumentException(
                    "Cannot activate a branch under an inactive business"
            );
        }

        branch.setActive(active);

        return branchRepository.save(branch);
    }

    public Branch deleteBranch(
            UUID tenantId,
            UUID businessId,
            UUID branchId
    ) {
        Branch branch = getBranchById(
                tenantId, businessId, branchId
        );

        branch.setActive(false);

        return branchRepository.save(branch);
    }
}
