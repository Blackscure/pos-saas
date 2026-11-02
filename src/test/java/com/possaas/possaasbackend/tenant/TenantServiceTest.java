package com.possaas.possaasbackend.tenant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantService tenantService;

    @Test
    void deleteTenant_Success() {
        UUID id = UUID.randomUUID();
        Tenant tenant = new Tenant();
        tenant.setName("Test Tenant");
        tenant.setCode("TEST01");
        tenant.setEmail("tenant@example.com");
        tenant.setActive(true);

        when(tenantRepository.findById(id)).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Tenant deletedTenant = tenantService.deleteTenant(id);

        assertNotNull(deletedTenant);
        assertFalse(deletedTenant.isActive());
        verify(tenantRepository).findById(id);
        verify(tenantRepository).save(tenant);
    }

    @Test
    void deleteTenant_NotFound_ThrowsException() {
        UUID id = UUID.randomUUID();
        when(tenantRepository.findById(id)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tenantService.deleteTenant(id)
        );

        assertEquals("Tenant not found", exception.getMessage());
        verify(tenantRepository).findById(id);
        verify(tenantRepository, never()).save(any(Tenant.class));
    }
}
