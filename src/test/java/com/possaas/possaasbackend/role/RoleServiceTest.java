
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private RoleService roleService;

    private UUID businessId;
    private UUID roleId;
    private Business business;
    private Role role;

    @BeforeEach
    void setUp() {
        businessId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        business = mock(Business.class);

        role = new Role();
        role.setId(roleId);
        role.setBusiness(business);
        role.setName("MANAGER");
        role.setDescription("Business manager");
        role.setActive(true);
        role.setPermissions(new HashSet<>());
    }

    // ---------------------------------------------------------
    // ROLE CREATION TESTS
    // ---------------------------------------------------------

    @Test
    void shouldCreateRoleSuccessfully() {
        when(businessRepository.findById(businessId))
                .thenReturn(Optional.of(business));
        when(business.isActive()).thenReturn(true);
        when(business.getId()).thenReturn(businessId);
        when(roleRepository.existsByBusiness_IdAndName(businessId, "MANAGER"))
                .thenReturn(false);
        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> {
                    Role saved = invocation.getArgument(0);
                    saved.setId(roleId);
                    return saved;
                });

        RoleResponse response = roleService.createRole(
                businessId,
                new CreateRoleRequest("Manager", "Business manager")
        );

        assertNotNull(response);
        assertEquals(roleId, response.id());
        assertEquals("MANAGER", response.name());
        assertEquals(businessId, response.businessId());
        assertTrue(response.active());

        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void shouldNormalizeRoleNameWhenCreatingRole() {
        when(businessRepository.findById(businessId))
                .thenReturn(Optional.of(business));
        when(business.isActive()).thenReturn(true);
        when(business.getId()).thenReturn(businessId);
        when(roleRepository.existsByBusiness_IdAndName(businessId, "STOCK_KEEPER"))
                .thenReturn(false);
        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> {
                    Role saved = invocation.getArgument(0);
                    saved.setId(roleId);
                    return saved;
                });

        RoleResponse response = roleService.createRole(
                businessId,
                new CreateRoleRequest(" Stock Keeper ", "Stock management")
        );

        assertEquals("STOCK_KEEPER", response.name());
    }

    @Test
    void shouldRejectDuplicateRoleName() {
        when(businessRepository.findById(businessId))
                .thenReturn(Optional.of(business));
        when(business.isActive()).thenReturn(true);
        when(roleRepository.existsByBusiness_IdAndName(businessId, "MANAGER"))
                .thenReturn(true);

        assertThrows(RuntimeException.class, () ->
                roleService.createRole(
                        businessId,
                        new CreateRoleRequest("Manager", "Manager role")
                )
        );

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void shouldRejectRoleCreationForInactiveBusiness() {
        when(businessRepository.findById(businessId))
                .thenReturn(Optional.of(business));
        when(business.isActive()).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                roleService.createRole(
                        businessId,
                        new CreateRoleRequest("CASHIER", "Cashier role")
                )
        );

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void shouldRejectRoleCreationForNonexistentBusiness() {
        when(businessRepository.findById(businessId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                roleService.createRole(
                        businessId,
                        new CreateRoleRequest("CASHIER", "Cashier role")
                )
        );
    }

    // ---------------------------------------------------------
    // ROLE RETRIEVAL TESTS
    // ---------------------------------------------------------

    @Test
    void shouldGetRolesByBusiness() {
        when(businessRepository.existsById(businessId))
                .thenReturn(true);

        Page<Role> rolePage = new PageImpl<>(
                List.of(role),
                PageRequest.of(0, 10),
                1
        );

        when(roleRepository.findAllByBusiness_Id(eq(businessId), any()))
                .thenReturn(rolePage);

        Page<RoleResponse> response =
                roleService.getRolesByBusiness(
                        businessId,
                        PageRequest.of(0, 10)
                );

        assertEquals(1, response.getTotalElements());
        assertEquals("MANAGER", response.getContent().get(0).name());
    }

    @Test
    void shouldRejectGettingRolesForNonexistentBusiness() {
        when(businessRepository.existsById(businessId))
                .thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                roleService.getRolesByBusiness(
                        businessId,
                        PageRequest.of(0, 10)
                )
        );
    }

    @Test
    void shouldGetRoleById() {
        when(roleRepository.findByIdAndBusiness_Id(roleId, businessId))
                .thenReturn(Optional.of(role));

        RoleResponse response =
                roleService.getRoleById(businessId, roleId);

        assertEquals(roleId, response.id());
        assertEquals("MANAGER", response.name());
    }

    @Test
    void shouldRejectRoleFromAnotherBusiness() {
        when(roleRepository.findByIdAndBusiness_Id(roleId, businessId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                roleService.getRoleById(businessId, roleId)
        );
    }

    // ---------------------------------------------------------
    // ROLE UPDATE TESTS
    // ---------------------------------------------------------

    @Test
    void shouldUpdateRoleSuccessfully() {
        when(roleRepository.findByIdAndBusiness_Id(roleId, businessId))
                .thenReturn(Optional.of(role));
        when(roleRepository.existsByBusiness_IdAndName(businessId, "SUPERVISOR"))
                .thenReturn(false);
        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RoleResponse response = roleService.updateRole(
                businessId,
                roleId,
                new UpdateRoleRequest(
                        "Supervisor",
                        "Supervises daily business operations"
                )
        );

        assertEquals("SUPERVISOR", response.name());
        assertEquals(
                "Supervises daily business operations",
                response.description()
        );
    }

    @Test
    void shouldRejectDuplicateNameWhenUpdatingRole() {
        when(roleRepository.findByIdAndBusiness_Id(roleId, businessId))
                .thenReturn(Optional.of(role));
        when(roleRepository.existsByBusiness_IdAndName(businessId, "CASHIER"))
                .thenReturn(true);

        assertThrows(RuntimeException.class, () ->
                roleService.updateRole(
                        businessId,
                        roleId,
                        new UpdateRoleRequest("Cashier", "Cashier role")
                )
        );

        verify(roleRepository, never()).save(any(Role.class));
    }

    // ---------------------------------------------------------
    // ROLE STATUS TESTS
    // ---------------------------------------------------------

    @Test
    void shouldDeactivateRoleSuccessfully() {
        when(roleRepository.findByIdAndBusiness_Id(roleId, businessId))
                .thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RoleResponse response =
                roleService.updateRoleStatus(businessId, roleId, false);

        assertFalse(response.active());
    }

    @Test
    void shouldActivateRoleSuccessfully() {
        role.setActive(false);

        when(roleRepository.findByIdAndBusiness_Id(roleId, businessId))
                .thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RoleResponse response =
                roleService.updateRoleStatus(businessId, roleId, true);

        assertTrue(response.active());
    }

    @Test
    void shouldPreventDeactivatingOwnerRole() {
        role.setName("OWNER");

        when(roleRepository.findByIdAndBusiness_Id(roleId, businessId))
                .thenReturn(Optional.of(role));

        assertThrows(RuntimeException.class, () ->
                roleService.updateRoleStatus(businessId, roleId, false)
        );

        verify(roleRepository, never()).save(any(Role.class));
    }

    // ---------------------------------------------------------
    // ROLE DELETION TESTS
    // ---------------------------------------------------------

    @Test
    void shouldSoftDeleteCustomRole() {
        role.setName("SUPERVISOR");

        when(roleRepository.findByIdAndBusiness_Id(roleId, businessId))
                .thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        roleService.deleteRole(businessId, roleId);

        assertFalse(role.isActive());
        verify(roleRepository).save(role);
    }

    @Test
    void shouldPreventDeletingSystemRole() {
        role.setName("MANAGER");

        when(roleRepository.findByIdAndBusiness_Id(roleId, businessId))
                .thenReturn(Optional.of(role));

        assertThrows(RuntimeException.class, () ->
                roleService.deleteRole(businessId, roleId)
        );

        verify(roleRepository, never()).save(any(Role.class));
    }

    // ---------------------------------------------------------
    // ROLE PERMISSION ASSIGNMENT TESTS
    // ---------------------------------------------------------

    @Test
    void shouldAssignPermissionsToRoleSuccessfully() {
        UUID permissionId = UUID.randomUUID();

        Permission permission = mock(Permission.class);

        when(permission.getId()).thenReturn(permissionId);
        when(permission.getCode()).thenReturn("USER_VIEW");
        when(permission.getName()).thenReturn("View Users");
        when(permission.getDescription()).thenReturn("View business users");
        when(permission.getCategory()).thenReturn("USER");

        when(roleRepository.findByIdAndBusinessIdWithPermissions(roleId, businessId))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findAllById(Set.of(permissionId)))
                .thenReturn(List.of(permission));

        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RolePermissionRequest request =
                new RolePermissionRequest(Set.of(permissionId));

        List<PermissionResponse> response =
                roleService.assignPermissions(businessId, roleId, request);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(permissionId, response.get(0).id());
        assertEquals("USER_VIEW", response.get(0).code());
        assertEquals("View Users", response.get(0).name());
        assertEquals("USER", response.get(0).category());

        assertEquals(1, role.getPermissions().size());

        verify(roleRepository).save(role);
    }

    @Test
    void shouldRejectAssigningInvalidPermissionIds() {
        UUID permissionId = UUID.randomUUID();

        when(roleRepository.findByIdAndBusinessIdWithPermissions(roleId, businessId))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findAllById(Set.of(permissionId)))
                .thenReturn(List.of());

        RolePermissionRequest request =
                new RolePermissionRequest(Set.of(permissionId));

        assertThrows(RuntimeException.class, () ->
                roleService.assignPermissions(businessId, roleId, request)
        );

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void shouldRejectAssigningEmptyPermissionList() {
        when(roleRepository.findByIdAndBusinessIdWithPermissions(roleId, businessId))
                .thenReturn(Optional.of(role));

        RolePermissionRequest request =
                new RolePermissionRequest(Set.of());

        assertThrows(RuntimeException.class, () ->
                roleService.assignPermissions(businessId, roleId, request)
        );

        verify(permissionRepository, never()).findAllById(anySet());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void shouldRejectAssigningPermissionsToNonexistentRole() {
        when(roleRepository.findByIdAndBusinessIdWithPermissions(roleId, businessId))
                .thenReturn(Optional.empty());

        UUID permissionId = UUID.randomUUID();

        RolePermissionRequest request =
                new RolePermissionRequest(Set.of(permissionId));

        assertThrows(RuntimeException.class, () ->
                roleService.assignPermissions(businessId, roleId, request)
        );

        verify(permissionRepository, never()).findAllById(anySet());
    }

    // ---------------------------------------------------------
    // ROLE PERMISSION RETRIEVAL TESTS
    // ---------------------------------------------------------

    @Test
    void shouldGetRolePermissionsSuccessfully() {
        UUID permissionId = UUID.randomUUID();

        Permission permission = mock(Permission.class);

        when(permission.getId()).thenReturn(permissionId);
        when(permission.getCode()).thenReturn("SALE_CREATE");
        when(permission.getName()).thenReturn("Create Sales");
        when(permission.getDescription()).thenReturn("Create new sales");
        when(permission.getCategory()).thenReturn("SALES");

        role.setPermissions(new HashSet<>(Set.of(permission)));

        when(roleRepository.findByIdAndBusinessIdWithPermissions(roleId, businessId))
                .thenReturn(Optional.of(role));

        List<PermissionResponse> response =
                roleService.getRolePermissions(businessId, roleId);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(permissionId, response.get(0).id());
        assertEquals("SALE_CREATE", response.get(0).code());
        assertEquals("Create Sales", response.get(0).name());
        assertEquals("SALES", response.get(0).category());
    }

    @Test
    void shouldReturnEmptyListWhenRoleHasNoPermissions() {
        role.setPermissions(new HashSet<>());

        when(roleRepository.findByIdAndBusinessIdWithPermissions(roleId, businessId))
                .thenReturn(Optional.of(role));

        List<PermissionResponse> response =
                roleService.getRolePermissions(businessId, roleId);

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void shouldRejectGettingPermissionsForNonexistentRole() {
        when(roleRepository.findByIdAndBusinessIdWithPermissions(roleId, businessId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                roleService.getRolePermissions(businessId, roleId)
        );
    }
}
