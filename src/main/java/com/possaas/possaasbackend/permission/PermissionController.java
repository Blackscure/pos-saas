package com.possaas.possaasbackend.permission;

import com.possaas.possaasbackend.common.response.ApiResponse;
import com.possaas.possaasbackend.permission.dto.PermissionResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    // Retrieve all permissions
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {

        List<PermissionResponse> permissions =
                permissionService.getAllPermissions()
                        .stream()
                        .map(PermissionResponse::fromEntity)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Permissions retrieved successfully",
                        permissions
                )
        );
    }

    // Retrieve permissions by category
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissionsByCategory(
            @PathVariable String category
    ) {

        List<PermissionResponse> permissions =
                permissionService.getPermissionsByCategory(category)
                        .stream()
                        .map(PermissionResponse::fromEntity)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Permissions retrieved successfully",
                        permissions
                )
        );
    }

    // Retrieve a permission by its unique code
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionByCode(
            @PathVariable String code
    ) {

        PermissionResponse permission = PermissionResponse.fromEntity(
                permissionService.getPermissionByCode(code)
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Permission retrieved successfully",
                        permission
                )
        );
    }
}
