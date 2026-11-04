
package com.possaas.possaasbackend.permission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private PermissionService permissionService;

    private Permission permission;

    @BeforeEach
    void setUp() {
        permission = mock(Permission.class);
    }

    @Test
    void shouldGetAllPermissionsSuccessfully() {
        when(permissionRepository.findAllByOrderByCategoryAscNameAsc())
                .thenReturn(List.of(permission));

        List<Permission> result = permissionService.getAllPermissions();

        assertEquals(1, result.size());
        assertSame(permission, result.get(0));

        verify(permissionRepository).findAllByOrderByCategoryAscNameAsc();
    }

    @Test
    void shouldGetPermissionsByCategorySuccessfully() {
        when(permissionRepository.findAllByCategoryOrderByNameAsc("USER"))
                .thenReturn(List.of(permission));

        List<Permission> result =
                permissionService.getPermissionsByCategory("USER");

        assertEquals(1, result.size());
        assertSame(permission, result.get(0));

        verify(permissionRepository).findAllByCategoryOrderByNameAsc("USER");
    }

    @Test
    void shouldNormalizeCategoryWhenGettingPermissions() {
        when(permissionRepository.findAllByCategoryOrderByNameAsc("USER"))
                .thenReturn(List.of(permission));

        List<Permission> result =
                permissionService.getPermissionsByCategory(" user ");

        assertEquals(1, result.size());

        verify(permissionRepository).findAllByCategoryOrderByNameAsc("USER");
    }

    @Test
    void shouldRejectBlankCategory() {
        assertThrows(
                IllegalArgumentException.class,
                () -> permissionService.getPermissionsByCategory(" ")
        );

        verifyNoInteractions(permissionRepository);
    }

    @Test
    void shouldRejectNullCategory() {
        assertThrows(
                IllegalArgumentException.class,
                () -> permissionService.getPermissionsByCategory(null)
        );

        verifyNoInteractions(permissionRepository);
    }

    @Test
    void shouldGetPermissionByCodeSuccessfully() {
        when(permissionRepository.findByCode("USER_VIEW"))
                .thenReturn(Optional.of(permission));

        Permission result =
                permissionService.getPermissionByCode("USER_VIEW");

        assertSame(permission, result);

        verify(permissionRepository).findByCode("USER_VIEW");
    }

    @Test
    void shouldNormalizePermissionCodeWhenGettingPermission() {
        when(permissionRepository.findByCode("USER_VIEW"))
                .thenReturn(Optional.of(permission));

        Permission result =
                permissionService.getPermissionByCode(" user_view ");

        assertSame(permission, result);

        verify(permissionRepository).findByCode("USER_VIEW");
    }

    @Test
    void shouldThrowExceptionWhenPermissionCodeDoesNotExist() {
        when(permissionRepository.findByCode("UNKNOWN_PERMISSION"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> permissionService.getPermissionByCode("UNKNOWN_PERMISSION")
        );

        verify(permissionRepository).findByCode("UNKNOWN_PERMISSION");
    }

    @Test
    void shouldRejectBlankPermissionCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> permissionService.getPermissionByCode(" ")
        );

        verifyNoInteractions(permissionRepository);
    }

    @Test
    void shouldRejectNullPermissionCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> permissionService.getPermissionByCode(null)
        );

        verifyNoInteractions(permissionRepository);
    }

    @Test
    void shouldReturnTrueWhenPermissionExists() {
        when(permissionRepository.existsByCode("USER_VIEW"))
                .thenReturn(true);

        assertTrue(permissionService.permissionExists("USER_VIEW"));

        verify(permissionRepository).existsByCode("USER_VIEW");
    }

    @Test
    void shouldReturnFalseWhenPermissionDoesNotExist() {
        when(permissionRepository.existsByCode("UNKNOWN_PERMISSION"))
                .thenReturn(false);

        assertFalse(permissionService.permissionExists("UNKNOWN_PERMISSION"));

        verify(permissionRepository).existsByCode("UNKNOWN_PERMISSION");
    }

    @Test
    void shouldReturnFalseWhenCheckingBlankPermissionCode() {
        assertFalse(permissionService.permissionExists(" "));

        verifyNoInteractions(permissionRepository);
    }

    @Test
    void shouldReturnFalseWhenCheckingNullPermissionCode() {
        assertFalse(permissionService.permissionExists(null));

        verifyNoInteractions(permissionRepository);
    }
}
