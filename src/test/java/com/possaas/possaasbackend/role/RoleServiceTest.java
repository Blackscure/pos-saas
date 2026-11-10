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
import static org.mockito.ArgumentMatchers.any;
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

    private UUID tenantId;
    private UUID businessId;
    private UUID roleId;

    private Business business;
    private Role role;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        businessId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        business = mock(Business.class);

        lenient().when(business.isActive()).thenReturn(true);
        lenient().when(business.getId()).thenReturn(businessId);

        lenient().when(
                businessRepository.findByIdAndTenantId(businessId, tenantId)
        ).thenReturn(Optional.of(business));

        role = new Role();
        role.setId(roleId);
        role.setBusiness(business);
        role.setName("MANAGER");
        role.setDescription("Business manager");
        role.setActive(true);
        role.setPermissions(new HashSet<>());
    }

    @Test
    void shouldCreateRoleSuccessfully() {
        CreateRoleRequest request = new CreateRoleRequest(
                "SUPERVISOR",
                "Store supervisor"
        );

        when(roleRepository.existsByBusiness_IdAndName(
                businessId,
                "SUPERVISOR"
        )).thenReturn(false);

        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RoleResponse result = roleService.createRole(
                tenantId,
                businessId,
                request
        );

        assertNotNull(result);
        assertEquals("SUPERVISOR", result.name());
        assertEquals("Store supervisor", result.description());
        assertEquals(businessId, result.businessId());
        assertTrue(result.active());

        verify(businessRepository)
                .findByIdAndTenantId(businessId, tenantId);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void shouldRejectCreatingDuplicateRole() {
        CreateRoleRequest request = new CreateRoleRequest(
                "MANAGER",
                "Business manager"
        );

        when(roleRepository.existsByBusiness_IdAndName(
                businessId,
                "MANAGER"
        )).thenReturn(true);

        assertThrows(RuntimeException.class, () ->
                roleService.createRole(tenantId, businessId, request)
        );

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void shouldRejectCreatingRoleForBusinessOutsideTenant() {
        when(businessRepository.findByIdAndTenantId(
                businessId,
                tenantId
        )).thenReturn(Optional.empty());

        CreateRoleRequest request = new CreateRoleRequest(
                "SUPERVISOR",
                "Store supervisor"
        );

        assertThrows(RuntimeException.class, () ->
                roleService.createRole(tenantId, businessId, request)
        );

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void shouldGetRolesByBusiness() {
        Page<Role> roles = new PageImpl<>(List.of(role));

        when(roleRepository.findAllByBusiness_Id(
                businessId,
                PageRequest.of(0, 10)
        )).thenReturn(roles);

        Page<RoleResponse> result = roleService.getRolesByBusiness(
                tenantId,
                businessId,
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals("MANAGER", result.getContent().get(0).name());
        verify(businessRepository)
                .findByIdAndTenantId(businessId, tenantId);
    }

    @Test
    void shouldRejectGettingRolesForBusinessOutsideTenant() {
        UUID anotherTenantId = UUID.randomUUID();

        when(businessRepository.findByIdAndTenantId(
                businessId,
                anotherTenantId
        )).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                roleService.getRolesByBusiness(
                        anotherTenantId,
                        businessId,
                        PageRequest.of(0, 10)
                )
        );

        verify(roleRepository, never())
                .findAllByBusiness_Id(any(UUID.class), any());
    }

    @Test
    void shouldGetRoleById() {
        when(roleRepository.findByIdAndBusiness_Id(roleId, businessId))
                .thenReturn(Optional.of(role));

        RoleResponse result = roleService.getRoleById(
                tenantId,
                businessId,
                roleId
        );

        assertNotNull(result);
        assertEquals(roleId, result.id());
        assertEquals("MANAGER", result.name());
    }

    @Test
    void shouldThrowWhenRoleDoesNotExist() {
        when(roleRepository.findByIdAndBusiness_Id(roleId, businessId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                roleService.getRoleById(tenantId, businessId, roleId)
        );
    }

    @Test
    void shouldUpdateRoleSuccessfully() {
        UpdateRoleRequest request = new UpdateRoleRequest(
                "SENIOR_MANAGER",
                "Senior business manager"
        );

        when(roleRepository.findByIdAndBusiness_Id(roleId, businessId))
                .thenReturn(Optional.of(role));

        when(roleRepository.existsByBusiness_IdAndName(
                businessId,
                "SENIOR_MANAGER"
        )).thenReturn(false);

        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RoleResponse result = roleService.updateRole(
                tenantId,
                businessId,
                roleId,
                request
        );

        assertEquals("SENIOR_MANAGER", result.name());
        assertEquals("Senior business manager", result.description());
    }

    @Test
    void shouldRejectUpdatingRoleToDuplicateName() {
        UpdateRoleRequest request = new UpdateRoleRequest(
                "CASHIER",
                "Cashier role"
        );

        when(roleRepository.findByIdAndBusiness_Id(roleId, businessId))
                .thenReturn(Optional.of(role));

        when(roleRepository.existsByBusiness_IdAndName(
                businessId,
                "CASHIER"
        )).thenReturn(true);

        assertThrows(RuntimeException.class, () ->
                roleService.updateRole(
                        tenantId,
                        businessId,
                        roleId,
                        request
                )
        );
    }

    @Test
    void shouldUpdateRoleStatus() {
        when(roleRepository.findByIdAndBusiness_Id(roleId, businessId))
                .thenReturn(Optional.of(role));

        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RoleResponse result = roleService.updateRoleStatus(
                tenantId,
                businessId,
                roleId,
                false
        );

        assertFalse(result.active());
    }

    @Test
    void shouldNotDeactivateOwnerRole() {
        role.setName("OWNER");

        when(roleRepository.findByIdAndBusiness_Id(roleId, businessId))
                .thenReturn(Optional.of(role));

        assertThrows(RuntimeException.class, () ->
                roleService.updateRoleStatus(
                        tenantId,
                        businessId,
                        roleId,
                        false
                )
        );

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void shouldDeleteRoleSuccessfully() {
        role.setName("SUPERVISOR");

        when(roleRepository.findByIdAndBusiness_Id(roleId, businessId))
                .thenReturn(Optional.of(role));

        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        roleService.deleteRole(tenantId, businessId, roleId);

        assertFalse(role.isActive());
        verify(roleRepository).save(role);
    }

    @Test
    void shouldNotDeleteSystemRole() {
        role.setName("MANAGER");

        when(roleRepository.findByIdAndBusiness_Id(roleId, businessId))
                .thenReturn(Optional.of(role));

        assertThrows(RuntimeException.class, () ->
                roleService.deleteRole(tenantId, businessId, roleId)
        );

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void shouldAssignPermissionsToRole() {
        UUID permissionId = UUID.randomUUID();

        Permission permission = mock(Permission.class);
        when(permission.getId()).thenReturn(permissionId);
        when(permission.getCode()).thenReturn("USER_VIEW");
        when(permission.getName()).thenReturn("View Users");
        when(permission.getDescription()).thenReturn("View business users");
        when(permission.getCategory()).thenReturn("USER");

        RolePermissionRequest request = new RolePermissionRequest(
                Set.of(permissionId)
        );

        when(roleRepository.findByIdAndBusinessIdWithPermissions(roleId, businessId))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findAllById(request.permissionIds()))
                .thenReturn(List.of(permission));

        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<PermissionResponse> result = roleService.assignPermissions(
                tenantId,
                businessId,
                roleId,
                request
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(permissionId, result.get(0).id());
        assertEquals("USER_VIEW", result.get(0).code());
        assertEquals(1, role.getPermissions().size());
        assertTrue(role.getPermissions().contains(permission));
        verify(roleRepository).save(role);
    }

    @Test
    void shouldGetRolePermissions() {
        UUID permissionId = UUID.randomUUID();

        Permission permission = mock(Permission.class);
        when(permission.getId()).thenReturn(permissionId);
        when(permission.getCode()).thenReturn("USER_VIEW");
        when(permission.getName()).thenReturn("View Users");
        when(permission.getDescription()).thenReturn("View business users");
        when(permission.getCategory()).thenReturn("USER");

        role.setPermissions(new HashSet<>(Set.of(permission)));

        when(roleRepository.findByIdAndBusinessIdWithPermissions(roleId, businessId))
                .thenReturn(Optional.of(role));

        List<PermissionResponse> result = roleService.getRolePermissions(
                tenantId,
                businessId,
                roleId
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(permissionId, result.get(0).id());
        assertEquals("USER_VIEW", result.get(0).code());
    }
}
