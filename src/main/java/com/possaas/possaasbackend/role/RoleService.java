
package com.possaas.possaasbackend.role;

import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessRepository;
import com.possaas.possaasbackend.permission.Permission;
import com.possaas.possaasbackend.permission.PermissionRepository;
import com.possaas.possaasbackend.permission.dto.PermissionResponse;
import com.possaas.possaasbackend.role.dto.CreateRoleRequest;
import com.possaas.possaasbackend.role.dto.RolePermissionRequest;
import com.possaas.possaasbackend.role.dto.RoleResponse;
import com.possaas.possaasbackend.role.dto.UpdateRoleRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final BusinessRepository businessRepository;
    private final PermissionRepository permissionRepository;

    private static final Set<String> SYSTEM_ROLES = Set.of(
            "OWNER",
            "MANAGER",
            "CASHIER",
            "STOCK_KEEPER"
    );

    public RoleService(
            RoleRepository roleRepository,
            BusinessRepository businessRepository,
            PermissionRepository permissionRepository
    ) {
        this.roleRepository = roleRepository;
        this.businessRepository = businessRepository;
        this.permissionRepository = permissionRepository;
    }

    // Create a role
    public RoleResponse createRole(UUID businessId, CreateRoleRequest request) {

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() ->
                        new RuntimeException("Business not found"));

        if (!business.isActive()) {
            throw new RuntimeException(
                    "Cannot create a role for an inactive business"
            );
        }

        String roleName = normalizeRoleName(request.name());

        if (roleRepository.existsByBusiness_IdAndName(businessId, roleName)) {
            throw new RuntimeException(
                    "A role with this name already exists"
            );
        }

        Role role = new Role();
        role.setBusiness(business);
        role.setName(roleName);
        role.setDescription(normalizeDescription(request.description()));
        role.setActive(true);

        Role savedRole = roleRepository.save(role);

        return RoleResponse.fromEntity(savedRole);
    }

    // Get all roles belonging to a business
    @Transactional(readOnly = true)
    public Page<RoleResponse> getRolesByBusiness(
            UUID businessId,
            Pageable pageable
    ) {

        if (!businessRepository.existsById(businessId)) {
            throw new RuntimeException("Business not found");
        }

        return roleRepository.findAllByBusiness_Id(businessId, pageable)
                .map(RoleResponse::fromEntity);
    }

    // Get a specific role
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(UUID businessId, UUID roleId) {

        Role role = getRoleOrThrow(businessId, roleId);

        return RoleResponse.fromEntity(role);
    }

    // Update role details
    public RoleResponse updateRole(
            UUID businessId,
            UUID roleId,
            UpdateRoleRequest request
    ) {

        Role role = getRoleOrThrow(businessId, roleId);

        String newName = normalizeRoleName(request.name());

        boolean nameChanged = !role.getName().equals(newName);

        if (nameChanged &&
                roleRepository.existsByBusiness_IdAndName(businessId, newName)) {
            throw new RuntimeException(
                    "A role with this name already exists"
            );
        }

        role.setName(newName);
        role.setDescription(normalizeDescription(request.description()));

        Role updatedRole = roleRepository.save(role);

        return RoleResponse.fromEntity(updatedRole);
    }

    // Activate or deactivate a role
    public RoleResponse updateRoleStatus(
            UUID businessId,
            UUID roleId,
            boolean active
    ) {

        Role role = getRoleOrThrow(businessId, roleId);

        if (!active && "OWNER".equals(role.getName())) {
            throw new RuntimeException(
                    "The OWNER role cannot be deactivated"
            );
        }

        role.setActive(active);

        Role updatedRole = roleRepository.save(role);

        return RoleResponse.fromEntity(updatedRole);
    }

    // Soft delete a role
    public void deleteRole(UUID businessId, UUID roleId) {

        Role role = getRoleOrThrow(businessId, roleId);

        if (SYSTEM_ROLES.contains(role.getName())) {
            throw new RuntimeException("System roles cannot be deleted");
        }

        role.setActive(false);

        roleRepository.save(role);
    }

    // Assign or replace permissions belonging to a role
    public List<PermissionResponse> assignPermissions(
            UUID businessId,
            UUID roleId,
            RolePermissionRequest request
    ) {

        Role role = getRoleWithPermissionsOrThrow(businessId, roleId);

        if (request.permissionIds() == null ||
                request.permissionIds().isEmpty()) {
            throw new RuntimeException(
                    "At least one permission must be selected"
            );
        }

        List<Permission> permissions =
                permissionRepository.findAllById(request.permissionIds());

        if (permissions.size() != request.permissionIds().size()) {
            throw new RuntimeException(
                    "One or more permission IDs are invalid"
            );
        }

        // Replace the role's existing permissions
        role.setPermissions(new HashSet<>(permissions));

        Role updatedRole = roleRepository.save(role);

        return updatedRole.getPermissions()
                .stream()
                .map(PermissionResponse::fromEntity)
                .toList();
    }

    // Get all permissions assigned to a role
    @Transactional(readOnly = true)
    public List<PermissionResponse> getRolePermissions(
            UUID businessId,
            UUID roleId
    ) {

        Role role = getRoleWithPermissionsOrThrow(businessId, roleId);

        return role.getPermissions()
                .stream()
                .map(PermissionResponse::fromEntity)
                .toList();
    }

    // Find role within the specified business
    private Role getRoleOrThrow(UUID businessId, UUID roleId) {

        return roleRepository.findByIdAndBusiness_Id(roleId, businessId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Role not found for this business"
                        ));
    }

    // Find role with its assigned permissions
    private Role getRoleWithPermissionsOrThrow(
            UUID businessId,
            UUID roleId
    ) {

        return roleRepository
                .findByIdAndBusinessIdWithPermissions(roleId, businessId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Role not found for this business"
                        ));
    }

    // Normalize role names
    private String normalizeRoleName(String name) {

        if (name == null || name.isBlank()) {
            throw new RuntimeException("Role name is required");
        }

        return name.trim()
                .replaceAll("\\s+", "_")
                .toUpperCase(Locale.ROOT);
    }

    // Normalize optional descriptions
    private String normalizeDescription(String description) {

        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }
}
