
package com.possaas.possaasbackend.role;

import com.possaas.possaasbackend.common.pagination.PageResponse;
import com.possaas.possaasbackend.common.pagination.PaginationUtils;
import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.permission.dto.PermissionResponse;
import com.possaas.possaasbackend.role.dto.CreateRoleRequest;
import com.possaas.possaasbackend.role.dto.RolePermissionRequest;
import com.possaas.possaasbackend.role.dto.RoleResponse;
import com.possaas.possaasbackend.role.dto.UpdateRoleRequest;
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
@RequestMapping("/api/businesses/{businessId}/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    // Create role
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @PathVariable UUID businessId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody CreateRoleRequest request
    ) {
        RoleResponse response = roleService.createRole(
                authenticatedUser.tenantId(),
                businessId,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Role created successfully",
                        response
                ));
    }

    // Get all roles
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_VIEW') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<PageResponse<RoleResponse>>> getRoles(
            @PathVariable UUID businessId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Pageable pageable = PaginationUtils.createPageable(
                page,
                size,
                sortBy,
                sortDir
        );

        Page<RoleResponse> roles = roleService.getRolesByBusiness(
                authenticatedUser.tenantId(),
                businessId,
                pageable
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Roles retrieved successfully",
                        PageResponse.of(roles)
                )
        );
    }

    // Get role by ID
    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_VIEW') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(
            @PathVariable UUID businessId,
            @PathVariable UUID roleId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        RoleResponse response = roleService.getRoleById(
                authenticatedUser.tenantId(),
                businessId,
                roleId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role retrieved successfully",
                        response
                )
        );
    }

    // Update role
    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable UUID businessId,
            @PathVariable UUID roleId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        RoleResponse response = roleService.updateRole(
                authenticatedUser.tenantId(),
                businessId,
                roleId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role updated successfully",
                        response
                )
        );
    }

    // Activate or deactivate role
    @PatchMapping("/{roleId}/status")
    @PreAuthorize("hasAuthority('ROLE_UPDATE') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRoleStatus(
            @PathVariable UUID businessId,
            @PathVariable UUID roleId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam boolean active
    ) {
        RoleResponse response = roleService.updateRoleStatus(
                authenticatedUser.tenantId(),
                businessId,
                roleId,
                active
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role status updated successfully",
                        response
                )
        );
    }

    // Soft delete role
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_DELETE') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<Void>> deleteRole(
            @PathVariable UUID businessId,
            @PathVariable UUID roleId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        roleService.deleteRole(
                authenticatedUser.tenantId(),
                businessId,
                roleId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role deactivated successfully",
                        null
                )
        );
    }

    // Assign or replace permissions assigned to a role
    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('ROLE_ASSIGN') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> assignPermissions(
            @PathVariable UUID businessId,
            @PathVariable UUID roleId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody RolePermissionRequest request
    ) {
        List<PermissionResponse> response = roleService.assignPermissions(
                authenticatedUser.tenantId(),
                businessId,
                roleId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role permissions assigned successfully",
                        response
                )
        );
    }

    // Get permissions assigned to a role
    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('ROLE_VIEW') and #businessId == authentication.principal.businessId")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getRolePermissions(
            @PathVariable UUID businessId,
            @PathVariable UUID roleId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        List<PermissionResponse> response = roleService.getRolePermissions(
                authenticatedUser.tenantId(),
                businessId,
                roleId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role permissions retrieved successfully",
                        response
                )
        );
    }
}
