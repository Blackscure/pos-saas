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
            BusinessRepository businessRepository) {

        this.branchRepository = branchRepository;
        this.businessRepository = businessRepository;
    }

    // Create a branch
    public Branch createBranch(UUID businessId, CreateBranchRequest request) {

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Business not found"));

        if (!business.isActive()) {
            throw new IllegalArgumentException(
                    "Cannot create a branch under an inactive business");
        }

        if (branchRepository.existsByBusiness_IdAndName(
                businessId, request.name())) {

            throw new IllegalArgumentException(
                    "A branch with this name already exists");
        }

        if (branchRepository.existsByBusiness_IdAndCode(
                businessId, request.code())) {

            throw new IllegalArgumentException(
                    "A branch with this code already exists");
        }

        Branch branch = new Branch();

        branch.setBusiness(business);
        branch.setName(request.name());
        branch.setCode(request.code());
        branch.setPhone(request.phone());
        branch.setEmail(request.email());
        branch.setAddress(request.address());
        branch.setActive(true);

        return branchRepository.save(branch);
    }

    // Get all branches belonging to a business with pagination
    @Transactional(readOnly = true)
    public Page<Branch> getBranchesByBusiness(
            UUID businessId,
            Pageable pageable) {

        if (!businessRepository.existsById(businessId)) {
            throw new IllegalArgumentException("Business not found");
        }

        return branchRepository.findAllByBusiness_Id(businessId, pageable);
    }

    // Get a specific branch
    @Transactional(readOnly = true)
    public Branch getBranchById(UUID businessId, UUID branchId) {

        return branchRepository.findByIdAndBusiness_Id(branchId, businessId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Branch not found"));
    }

    // Update branch details
    public Branch updateBranch(
            UUID businessId,
            UUID branchId,
            UpdateBranchRequest request) {

        Branch existingBranch = getBranchById(businessId, branchId);

        if (!existingBranch.getName().equals(request.name())
                && branchRepository.existsByBusiness_IdAndName(
                businessId, request.name())) {

            throw new IllegalArgumentException(
                    "A branch with this name already exists");
        }

        if (!existingBranch.getCode().equals(request.code())
                && branchRepository.existsByBusiness_IdAndCode(
                businessId, request.code())) {

            throw new IllegalArgumentException(
                    "A branch with this code already exists");
        }

        existingBranch.setName(request.name());
        existingBranch.setCode(request.code());
        existingBranch.setPhone(request.phone());
        existingBranch.setEmail(request.email());
        existingBranch.setAddress(request.address());

        return branchRepository.save(existingBranch);
    }

    // Activate or deactivate a branch
    public Branch updateBranchStatus(
            UUID businessId,
            UUID branchId,
            boolean active) {

        Branch branch = getBranchById(businessId, branchId);

        if (active && !branch.getBusiness().isActive()) {
            throw new IllegalArgumentException(
                    "Cannot activate a branch under an inactive business");
        }

        branch.setActive(active);

        return branchRepository.save(branch);
    }

    // Soft-delete a branch
    public Branch deleteBranch(UUID businessId, UUID branchId) {

        Branch branch = getBranchById(businessId, branchId);

        branch.setActive(false);

        return branchRepository.save(branch);
    }
}
