
package com.possaas.possaasbackend.business;

import com.possaas.possaasbackend.business.dto.BusinessRequest;
import com.possaas.possaasbackend.tenant.Tenant;
import com.possaas.possaasbackend.tenant.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessServiceTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private BusinessService businessService;

    private UUID tenantId;
    private Tenant activeTenant;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();

        activeTenant = new Tenant();
        activeTenant.setName("Test Tenant");
        activeTenant.setCode("TEST01");
        activeTenant.setEmail("tenant@example.com");
        activeTenant.setActive(true);
    }

    @Test
    void createBusiness_Success() {
        BusinessRequest request = BusinessRequest.builder()
                .name("Supermart Downtown")
                .businessType(BusinessType.SUPERMARKET)
                .registrationNumber("REG-123")
                .phone("+254700000000")
                .email("info@supermart.com")
                .address("CBD")
                .currency("KES")
                .taxEnabled(true)
                .active(true)
                .build();

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(activeTenant));

        when(businessRepository.existsByTenantIdAndName(
                tenantId, "Supermart Downtown"
        )).thenReturn(false);

        when(businessRepository.existsByTenantIdAndRegistrationNumber(
                tenantId, "REG-123"
        )).thenReturn(false);

        when(businessRepository.save(any(Business.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Business created = businessService.createBusiness(tenantId, request);

        assertNotNull(created);
        assertEquals("Supermart Downtown", created.getName());
        assertEquals(BusinessType.SUPERMARKET, created.getBusinessType());
        assertEquals("REG-123", created.getRegistrationNumber());
        assertEquals(activeTenant, created.getTenant());
        assertTrue(created.isActive());

        verify(businessRepository).save(any(Business.class));
    }

    @Test
    void createBusiness_TenantNotFound_ThrowsException() {
        BusinessRequest request = BusinessRequest.builder()
                .name("Supermart Downtown")
                .businessType(BusinessType.SUPERMARKET)
                .build();

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> businessService.createBusiness(tenantId, request)
        );

        assertEquals("Tenant not found", ex.getMessage());

        verify(businessRepository, never()).save(any());
    }

    @Test
    void createBusiness_InactiveTenant_ThrowsException() {
        activeTenant.setActive(false);

        BusinessRequest request = BusinessRequest.builder()
                .name("Supermart Downtown")
                .businessType(BusinessType.SUPERMARKET)
                .build();

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(activeTenant));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> businessService.createBusiness(tenantId, request)
        );

        assertEquals(
                "Cannot create a business under an inactive tenant",
                ex.getMessage()
        );

        verify(businessRepository, never()).save(any());
    }

    @Test
    void createBusiness_DuplicateName_ThrowsException() {
        BusinessRequest request = BusinessRequest.builder()
                .name("Supermart Downtown")
                .businessType(BusinessType.SUPERMARKET)
                .build();

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(activeTenant));

        when(businessRepository.existsByTenantIdAndName(
                tenantId, "Supermart Downtown"
        )).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> businessService.createBusiness(tenantId, request)
        );

        assertEquals(
                "A business with this name already exists for this tenant",
                ex.getMessage()
        );

        verify(businessRepository, never()).save(any());
    }

    @Test
    void createBusiness_DuplicateRegistrationNumber_ThrowsException() {
        BusinessRequest request = BusinessRequest.builder()
                .name("Supermart Downtown")
                .businessType(BusinessType.SUPERMARKET)
                .registrationNumber("REG-DUPLICATE")
                .build();

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(activeTenant));

        when(businessRepository.existsByTenantIdAndName(
                tenantId, "Supermart Downtown"
        )).thenReturn(false);

        when(businessRepository.existsByTenantIdAndRegistrationNumber(
                tenantId, "REG-DUPLICATE"
        )).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> businessService.createBusiness(tenantId, request)
        );

        assertEquals(
                "A business with this registration number already exists for this tenant",
                ex.getMessage()
        );

        verify(businessRepository, never()).save(any());
    }

    @Test
    void getBusinessesByTenant_Paginated_Success() {
        Pageable pageable = PageRequest.of(0, 10);

        Business b1 = new Business();
        b1.setName("Business 1");
        b1.setTenant(activeTenant);

        Page<Business> page = new PageImpl<>(
                List.of(b1), pageable, 1
        );

        when(tenantRepository.existsById(tenantId)).thenReturn(true);

        when(businessRepository.findAllByTenantId(tenantId, pageable))
                .thenReturn(page);

        Page<Business> result =
                businessService.getBusinessesByTenant(tenantId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Business 1", result.getContent().get(0).getName());

        verify(businessRepository).findAllByTenantId(tenantId, pageable);
    }

    @Test
    void getBusinessesByTenant_TenantNotFound_ThrowsException() {
        Pageable pageable = PageRequest.of(0, 10);

        when(tenantRepository.existsById(tenantId)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> businessService.getBusinessesByTenant(tenantId, pageable)
        );

        assertEquals("Tenant not found", ex.getMessage());
    }

    @Test
    void getBusinessesByTenantAndType_Paginated_Success() {
        Pageable pageable = PageRequest.of(0, 10);

        Business b1 = new Business();
        b1.setName("Pharmacy 1");
        b1.setBusinessType(BusinessType.PHARMACY);
        b1.setTenant(activeTenant);

        Page<Business> page = new PageImpl<>(
                List.of(b1), pageable, 1
        );

        when(tenantRepository.existsById(tenantId)).thenReturn(true);

        when(businessRepository.findAllByTenantIdAndBusinessType(
                tenantId, BusinessType.PHARMACY, pageable
        )).thenReturn(page);

        Page<Business> result =
                businessService.getBusinessesByTenantAndType(
                        tenantId,
                        BusinessType.PHARMACY,
                        pageable
                );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Pharmacy 1", result.getContent().get(0).getName());
    }

    @Test
    void getBusinessById_Success() {
        UUID businessId = UUID.randomUUID();

        Business business = new Business();
        business.setId(businessId);
        business.setName("Test Business");
        business.setTenant(activeTenant);

        when(tenantRepository.existsById(tenantId)).thenReturn(true);

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.of(business));

        Business result = businessService.getBusinessById(
                tenantId, businessId
        );

        assertNotNull(result);
        assertEquals(businessId, result.getId());
        assertEquals("Test Business", result.getName());
    }

    @Test
    void getBusinessById_NotFound_ThrowsException() {
        UUID businessId = UUID.randomUUID();

        when(tenantRepository.existsById(tenantId)).thenReturn(true);

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> businessService.getBusinessById(tenantId, businessId)
        );

        assertEquals("Business not found", ex.getMessage());
    }

    @Test
    void updateBusiness_Success() {
        UUID businessId = UUID.randomUUID();

        Business existing = new Business();
        existing.setId(businessId);
        existing.setName("Old Name");
        existing.setRegistrationNumber("REG-OLD");
        existing.setBusinessType(BusinessType.RETAIL);
        existing.setTenant(activeTenant);

        BusinessRequest request = BusinessRequest.builder()
                .name("New Name")
                .registrationNumber("REG-NEW")
                .businessType(BusinessType.HARDWARE)
                .build();

        when(tenantRepository.existsById(tenantId)).thenReturn(true);

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.of(existing));

        when(businessRepository.existsByTenantIdAndName(
                tenantId, "New Name"
        )).thenReturn(false);

        when(businessRepository.existsByTenantIdAndRegistrationNumber(
                tenantId, "REG-NEW"
        )).thenReturn(false);

        when(businessRepository.save(any(Business.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Business updated = businessService.updateBusiness(
                tenantId, businessId, request
        );

        assertEquals("New Name", updated.getName());
        assertEquals("REG-NEW", updated.getRegistrationNumber());
        assertEquals(BusinessType.HARDWARE, updated.getBusinessType());
    }

    @Test
    void updateBusinessStatus_Success() {
        UUID businessId = UUID.randomUUID();

        Business business = new Business();
        business.setId(businessId);
        business.setActive(true);
        business.setTenant(activeTenant);

        when(tenantRepository.existsById(tenantId)).thenReturn(true);

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.of(business));

        when(businessRepository.save(any(Business.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Business result = businessService.updateBusinessStatus(
                tenantId, businessId, false
        );

        assertFalse(result.isActive());
    }

    @Test
    void deleteBusiness_Success() {
        UUID businessId = UUID.randomUUID();

        Business business = new Business();
        business.setId(businessId);
        business.setActive(true);
        business.setTenant(activeTenant);

        when(tenantRepository.existsById(tenantId)).thenReturn(true);

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.of(business));

        when(businessRepository.save(any(Business.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Business result = businessService.deleteBusiness(
                tenantId, businessId
        );

        assertFalse(result.isActive());
    }

    // TENANT ISOLATION TESTS

    @Test
    void getBusinessById_BusinessBelongsToAnotherTenant_ThrowsException() {
        UUID businessId = UUID.randomUUID();

        when(tenantRepository.existsById(tenantId)).thenReturn(true);

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> businessService.getBusinessById(tenantId, businessId)
        );

        assertEquals("Business not found", ex.getMessage());
    }

    @Test
    void updateBusiness_BusinessBelongsToAnotherTenant_ThrowsException() {
        UUID businessId = UUID.randomUUID();

        BusinessRequest request = BusinessRequest.builder()
                .name("Unauthorized Update")
                .build();

        when(tenantRepository.existsById(tenantId)).thenReturn(true);

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> businessService.updateBusiness(tenantId, businessId, request)
        );

        verify(businessRepository, never()).save(any(Business.class));
    }

    @Test
    void updateBusinessStatus_BusinessBelongsToAnotherTenant_ThrowsException() {
        UUID businessId = UUID.randomUUID();

        when(tenantRepository.existsById(tenantId)).thenReturn(true);

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> businessService.updateBusinessStatus(
                        tenantId, businessId, false
                )
        );

        verify(businessRepository, never()).save(any(Business.class));
    }

    @Test
    void deleteBusiness_BusinessBelongsToAnotherTenant_ThrowsException() {
        UUID businessId = UUID.randomUUID();

        when(tenantRepository.existsById(tenantId)).thenReturn(true);

        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> businessService.deleteBusiness(tenantId, businessId)
        );

        verify(businessRepository, never()).save(any(Business.class));
    }
}
