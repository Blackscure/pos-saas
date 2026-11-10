
package com.possaas.possaasbackend.branch;

import com.possaas.possaasbackend.branch.dto.BranchResponse;
import com.possaas.possaasbackend.branch.dto.CreateBranchRequest;
import com.possaas.possaasbackend.branch.dto.UpdateBranchRequest;
import com.possaas.possaasbackend.common.pagination.PageResponse;
import com.possaas.possaasbackend.common.pagination.PaginationUtils;
import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.security.AuthenticatedUser;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PreAuthorize("hasAuthority('BRANCH_CREATE') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(
            @PathVariable UUID businessId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateBranchRequest request) {

        Branch createdBranch = branchService.createBranch(
                authenticatedUser.tenantId(),
                businessId,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Branch created successfully",
                        BranchResponse.fromEntity(createdBranch)
                ));
    }

    // Get all branches belonging to a business
    @GetMapping
    @PreAuthorize("hasAuthority('BRANCH_VIEW') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<PageResponse<BranchResponse>>> getBranches(
            @PathVariable UUID businessId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationUtils.DEFAULT_SORT_DIRECTION) String sortDir) {

        Pageable pageable = PaginationUtils.createPageable(
                page, size, sortBy, sortDir
        );

        Page<Branch> branchPage = branchService.getBranchesByBusiness(
                authenticatedUser.tenantId(),
                businessId,
                pageable
        );

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
    @PreAuthorize("hasAuthority('BRANCH_VIEW') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranchById(
            @PathVariable UUID businessId,
            @PathVariable UUID branchId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        Branch branch = branchService.getBranchById(
                authenticatedUser.tenantId(),
                businessId,
                branchId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Branch fetched successfully",
                        BranchResponse.fromEntity(branch)
                )
        );
    }

    // Update branch details
    @PutMapping("/{branchId}")
    @PreAuthorize("hasAuthority('BRANCH_UPDATE') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @PathVariable UUID businessId,
            @PathVariable UUID branchId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody UpdateBranchRequest request) {

        Branch updatedBranch = branchService.updateBranch(
                authenticatedUser.tenantId(),
                businessId,
                branchId,
                request
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
    @PreAuthorize("(hasAuthority('BRANCH_UPDATE') or hasAuthority('BRANCH_DELETE')) and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranchStatus(
            @PathVariable UUID businessId,
            @PathVariable UUID branchId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam boolean active) {

        Branch branch = branchService.updateBranchStatus(
                authenticatedUser.tenantId(),
                businessId,
                branchId,
                active
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
    @PreAuthorize("hasAuthority('BRANCH_DELETE') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<BranchResponse>> deleteBranch(
            @PathVariable UUID businessId,
            @PathVariable UUID branchId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        Branch branch = branchService.deleteBranch(
                authenticatedUser.tenantId(),
                businessId,
                branchId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Branch deleted successfully",
                        BranchResponse.fromEntity(branch)
                )
        );
    }
}
