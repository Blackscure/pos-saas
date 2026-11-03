
package com.possaas.possaasbackend.branch;

import com.possaas.possaasbackend.branch.dto.BranchResponse;
import com.possaas.possaasbackend.branch.dto.CreateBranchRequest;
import com.possaas.possaasbackend.branch.dto.UpdateBranchRequest;
import com.possaas.possaasbackend.common.pagination.PageResponse;
import com.possaas.possaasbackend.common.pagination.PaginationUtils;
import com.possaas.possaasbackend.common.response.ApiResponse;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/businesses/{businessId}/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    // Create a branch
    @PostMapping
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(
            @PathVariable UUID businessId,
            @Valid @RequestBody CreateBranchRequest request) {

        Branch createdBranch = branchService.createBranch(businessId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Branch created successfully",
                        BranchResponse.fromEntity(createdBranch)
                ));
    }

    // Get all branches belonging to a business with pagination
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BranchResponse>>> getBranches(
            @PathVariable UUID businessId,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_DIRECTION) String sortDir) {

        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDir);

        Page<Branch> branchPage = branchService.getBranchesByBusiness(businessId, pageable);

        List<BranchResponse> branchResponses = branchPage.getContent()
                .stream()
                .map(BranchResponse::fromEntity)
                .toList();

        PageResponse<BranchResponse> pageResponse =
                PageResponse.of(branchPage, branchResponses);

        return ResponseEntity.ok(
                ApiResponse.success("Branches fetched successfully", pageResponse)
        );
    }

    // Get one branch
    @GetMapping("/{branchId}")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(
            @PathVariable UUID businessId,
            @PathVariable UUID branchId) {

        Branch branch = branchService.getBranchById(businessId, branchId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Branch fetched successfully",
                        BranchResponse.fromEntity(branch)
                )
        );
    }

    // Update branch details
    @PutMapping("/{branchId}")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @PathVariable UUID businessId,
            @PathVariable UUID branchId,
            @Valid @RequestBody UpdateBranchRequest request) {

        Branch updatedBranch = branchService.updateBranch(
                businessId, branchId, request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Branch updated successfully",
                        BranchResponse.fromEntity(updatedBranch)
                )
        );
    }

    // Activate or deactivate a branch
    @PatchMapping("/{branchId}/status")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranchStatus(
            @PathVariable UUID businessId,
            @PathVariable UUID branchId,
            @RequestParam boolean active) {

        Branch branch = branchService.updateBranchStatus(
                businessId, branchId, active
        );

        String message = active
                ? "Branch activated successfully"
                : "Branch deactivated successfully";

        return ResponseEntity.ok(
                ApiResponse.success(message, BranchResponse.fromEntity(branch))
        );
    }

    // Soft-delete a branch
    @DeleteMapping("/{branchId}")
    public ResponseEntity<ApiResponse<BranchResponse>> deleteBranch(
            @PathVariable UUID businessId,
            @PathVariable UUID branchId) {

        Branch branch = branchService.deleteBranch(businessId, branchId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Branch deleted successfully",
                        BranchResponse.fromEntity(branch)
                )
        );
    }
}
