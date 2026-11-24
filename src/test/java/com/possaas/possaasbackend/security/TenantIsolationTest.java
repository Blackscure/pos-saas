package com.possaas.possaasbackend.security;

import com.possaas.possaasbackend.branch.BranchRepository;
import com.possaas.possaasbackend.branch.BranchService;
import com.possaas.possaasbackend.branch.dto.CreateBranchRequest;
import com.possaas.possaasbackend.business.BusinessRepository;
import com.possaas.possaasbackend.business.BusinessService;
import com.possaas.possaasbackend.business.dto.BusinessRequest;
import com.possaas.possaasbackend.role.RoleRepository;
import com.possaas.possaasbackend.role.RoleService;
import com.possaas.possaasbackend.role.dto.CreateRoleRequest;
import com.possaas.possaasbackend.tenant.TenantRepository;
import com.possaas.possaasbackend.user.UserRepository;
import com.possaas.possaasbackend.user.UserService;
import com.possaas.possaasbackend.user.dto.CreateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantIsolationTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private BusinessService businessService;
    private BranchService branchService;
    private RoleService roleService;
    private UserService userService;

    private UUID tenantAId;
    private UUID tenantBId;
    private UUID businessBId;
    private UUID branchBId;
    private UUID roleBId;
    private UUID userBId;

    @BeforeEach
    void setUp() {
        businessService = new BusinessService(businessRepository, tenantRepository);
        branchService = new BranchService(branchRepository, businessRepository);
        roleService = new RoleService(roleRepository, businessRepository, null);
        userService = new UserService(userRepository, businessRepository, passwordEncoder);

        tenantAId = UUID.randomUUID();
        tenantBId = UUID.randomUUID();
        businessBId = UUID.randomUUID();
        branchBId = UUID.randomUUID();
        roleBId = UUID.randomUUID();
        userBId = UUID.randomUUID();

        // When queried with Tenant A's ID for a resource belonging to Tenant B,
        // the repository returns empty because the business is not owned by Tenant A.
        lenient().when(businessRepository.findByIdAndTenantId(businessBId, tenantAId))
                .thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("Tenant A cannot access a Business belonging to Tenant B")
    void tenantACannotAccessTenantBBusiness() {
        when(tenantRepository.existsById(tenantAId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                businessService.getBusinessById(tenantAId, businessBId)
        );

        assertThrows(IllegalArgumentException.class, () ->
                businessService.updateBusiness(tenantAId, businessBId, new BusinessRequest())
        );

        assertThrows(IllegalArgumentException.class, () ->
                businessService.deleteBusiness(tenantAId, businessBId)
        );

        verify(businessRepository, times(3)).findByIdAndTenantId(businessBId, tenantAId);
    }

    @Test
    @DisplayName("Tenant A cannot access Branches under Tenant B's Business")
    void tenantACannotAccessTenantBBranches() {
        CreateBranchRequest request = new CreateBranchRequest(
                "Branch B", "BR01", "+254700000000", "b@b.com", "Location"
        );

        assertThrows(IllegalArgumentException.class, () ->
                branchService.createBranch(tenantAId, businessBId, request)
        );

        assertThrows(IllegalArgumentException.class, () ->
                branchService.getBranchesByBusiness(tenantAId, businessBId, PageRequest.of(0, 10))
        );

        assertThrows(IllegalArgumentException.class, () ->
                branchService.getBranchById(tenantAId, businessBId, branchBId)
        );

        assertThrows(IllegalArgumentException.class, () ->
                branchService.deleteBranch(tenantAId, businessBId, branchBId)
        );

        verify(branchRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tenant A cannot access Roles under Tenant B's Business")
    void tenantACannotAccessTenantBRoles() {
        CreateRoleRequest request = new CreateRoleRequest("MANAGER", "Store Manager");

        assertThrows(RuntimeException.class, () ->
                roleService.createRole(tenantAId, businessBId, request)
        );

        assertThrows(RuntimeException.class, () ->
                roleService.getRolesByBusiness(tenantAId, businessBId, PageRequest.of(0, 10))
        );

        assertThrows(RuntimeException.class, () ->
                roleService.getRoleById(tenantAId, businessBId, roleBId)
        );

        assertThrows(RuntimeException.class, () ->
                roleService.deleteRole(tenantAId, businessBId, roleBId)
        );

        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tenant A cannot access Users under Tenant B's Business")
    void tenantACannotAccessTenantBUsers() {
        CreateUserRequest request = new CreateUserRequest(
                "Alice", "Smith", "alice@example.com", "+254711111111", "password123"
        );

        assertThrows(RuntimeException.class, () ->
                userService.createUser(tenantAId, businessBId, request)
        );

        assertThrows(RuntimeException.class, () ->
                userService.getUsersByBusiness(tenantAId, businessBId, PageRequest.of(0, 10))
        );

        assertThrows(RuntimeException.class, () ->
                userService.getUserById(tenantAId, businessBId, userBId)
        );

        assertThrows(RuntimeException.class, () ->
                userService.deleteUser(tenantAId, businessBId, userBId)
        );

        verify(userRepository, never()).save(any());
    }
}
