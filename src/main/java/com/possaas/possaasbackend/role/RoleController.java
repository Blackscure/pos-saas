
package com.possaas.possaasbackend.role;

import com.possaas.possaasbackend.common.pagination.PageResponse;
import com.possaas.possaasbackend.common.pagination.PaginationUtils;
import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.role.dto.CreateRoleRequest;
import com.possaas.possaasbackend.role.dto.RolePermissionRequest;
import com.possaas.possaasbackend.role.dto.RoleResponse;
import com.possaas.possaasbackend.role.dto.UpdateRoleRequest;
import com.possaas.possaasbackend.permission.dto.PermissionResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @PathVariable UUID businessId,
            @Valid @RequestBody CreateRoleRequest request
    ) {
        RoleResponse response =
                roleService.createRole(businessId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Role created successfully",
                        response
                ));
    }

    // Get all roles
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RoleResponse>>> getRoles(
            @PathVariable UUID businessId,
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

        Page<RoleResponse> roles =
                roleService.getRolesByBusiness(businessId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Roles retrieved successfully",
                        PageResponse.of(roles)
                )
        );
    }

    // Get role by ID
    @GetMapping("/{roleId}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(
            @PathVariable UUID businessId,
            @PathVariable UUID roleId
    ) {
        RoleResponse response =
                roleService.getRoleById(businessId, roleId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role retrieved successfully",
                        response
                )
        );
    }

    // Update role
    @PutMapping("/{roleId}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable UUID businessId,
            @PathVariable UUID roleId,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        RoleResponse response =
                roleService.updateRole(businessId, roleId, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role updated successfully",
                        response
                )
        );
    }

    // Activate or deactivate role
    @PatchMapping("/{roleId}/status")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRoleStatus(
            @PathVariable UUID businessId,
            @PathVariable UUID roleId,
            @RequestParam boolean active
    ) {
        RoleResponse response =
                roleService.updateRoleStatus(
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
    public ResponseEntity<ApiResponse<Void>> deleteRole(
            @PathVariable UUID businessId,
            @PathVariable UUID roleId
    ) {
        roleService.deleteRole(businessId, roleId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role deactivated successfully",
                        null
                )
        );
    }

    // Assign or replace permissions assigned to a role
    @PutMapping("/{roleId}/permissions")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> assignPermissions(
            @PathVariable UUID businessId,
            @PathVariable UUID roleId,
            @Valid @RequestBody RolePermissionRequest request
    ) {
        List<PermissionResponse> response =
                roleService.assignPermissions(businessId, roleId, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role permissions assigned successfully",
                        response
                )
        );
    }

    // Get permissions assigned to a role
    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getRolePermissions(
            @PathVariable UUID businessId,
            @PathVariable UUID roleId
    ) {
        List<PermissionResponse> response =
                roleService.getRolePermissions(businessId, roleId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Role permissions retrieved successfully",
                        response
                )
        );
    }
}
