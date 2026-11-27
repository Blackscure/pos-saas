package com.possaas.possaasbackend.supplier;

import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessRepository;
import com.possaas.possaasbackend.supplier.dto.CreateSupplierRequest;
import com.possaas.possaasbackend.supplier.dto.UpdateSupplierRequest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private SupplierService supplierService;

    private UUID tenantId;
    private UUID businessId;
    private UUID supplierId;
    private Business business;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        businessId = UUID.randomUUID();
        supplierId = UUID.randomUUID();

        business = new Business();
        business.setId(businessId);
        business.setActive(true);
    }

    private void mockBusinessExists() {
        when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                .thenReturn(Optional.of(business));
    }

    private Supplier buildSupplier(UUID id, String name, boolean active) {
        Supplier supplier = new Supplier();
        ReflectionTestUtils.setField(supplier, "id", id);
        ReflectionTestUtils.setField(supplier, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(supplier, "updatedAt", LocalDateTime.now());
        supplier.setBusiness(business);
        supplier.setName(name);
        supplier.setContactPerson("Jane Doe");
        supplier.setPhone("+254700000000");
        supplier.setEmail("jane@acme.test");
        supplier.setAddress("123 Market Street");
        supplier.setTaxPin("P051234567X");
        supplier.setPaymentTerms("Net 30");
        supplier.setNotes("Primary distributor");
        supplier.setActive(active);
        return supplier;
    }

    // =========================================================================
    // CREATE SUPPLIER
    // =========================================================================
    @Nested
    @DisplayName("Create Supplier Tests")
    class CreateSupplierTests {

        @Test
        @DisplayName("Should create supplier successfully with all fields and active true")
        void shouldCreateSupplierSuccessfully() {
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "Acme Distributors",
                    "Jane Doe",
                    "+254700000000",
                    "jane@acme.test",
                    "123 Market Street",
                    "P051234567X",
                    "Net 30",
                    "Primary distributor"
            );

            mockBusinessExists();
            when(supplierRepository.existsByBusiness_IdAndNameIgnoreCase(businessId, "Acme Distributors"))
                    .thenReturn(false);
            when(supplierRepository.save(any(Supplier.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Supplier result = supplierService.createSupplier(tenantId, businessId, request);

            assertNotNull(result);
            assertSame(business, result.getBusiness());
            assertEquals("Acme Distributors", result.getName());
            assertEquals("Jane Doe", result.getContactPerson());
            assertEquals("+254700000000", result.getPhone());
            assertEquals("jane@acme.test", result.getEmail());
            assertEquals("123 Market Street", result.getAddress());
            assertEquals("P051234567X", result.getTaxPin());
            assertEquals("Net 30", result.getPaymentTerms());
            assertEquals("Primary distributor", result.getNotes());
            assertTrue(result.isActive());

            verify(supplierRepository).save(any(Supplier.class));
        }

        @Test
        @DisplayName("Should trim supplier name before duplicate check and saving")
        void shouldTrimSupplierName() {
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "   Acme Distributors   ",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            mockBusinessExists();
            when(supplierRepository.existsByBusiness_IdAndNameIgnoreCase(businessId, "Acme Distributors"))
                    .thenReturn(false);
            when(supplierRepository.save(any(Supplier.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Supplier result = supplierService.createSupplier(tenantId, businessId, request);

            assertEquals("Acme Distributors", result.getName());
            verify(supplierRepository).existsByBusiness_IdAndNameIgnoreCase(businessId, "Acme Distributors");
            verify(supplierRepository).save(any(Supplier.class));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when business does not exist")
        void shouldThrowWhenBusinessNotFound() {
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "Acme Distributors",
                    null, null, null, null, null, null, null
            );

            when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                    .thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(
                    EntityNotFoundException.class,
                    () -> supplierService.createSupplier(tenantId, businessId, request)
            );

            assertEquals("Business not found", ex.getMessage());
            verifyNoInteractions(supplierRepository);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when business is inactive")
        void shouldThrowWhenBusinessInactive() {
            business.setActive(false);
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "Acme Distributors",
                    null, null, null, null, null, null, null
            );

            mockBusinessExists();

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> supplierService.createSupplier(tenantId, businessId, request)
            );

            assertEquals("Business is inactive", ex.getMessage());
            verifyNoInteractions(supplierRepository);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when supplier name already exists")
        void shouldThrowWhenDuplicateNameExists() {
            CreateSupplierRequest request = new CreateSupplierRequest(
                    "  Acme Distributors ",
                    null, null, null, null, null, null, null
            );

            mockBusinessExists();
            when(supplierRepository.existsByBusiness_IdAndNameIgnoreCase(businessId, "Acme Distributors"))
                    .thenReturn(true);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> supplierService.createSupplier(tenantId, businessId, request)
            );

            assertEquals("Supplier with this name already exists", ex.getMessage());
            verify(supplierRepository, never()).save(any());
        }
    }

    // =========================================================================
    // GET SUPPLIERS
    // =========================================================================
    @Nested
    @DisplayName("Get Suppliers Tests")
    class GetSuppliersTests {

        @Test
        @DisplayName("Should return paged suppliers with trimmed search query")
        void shouldReturnPagedSuppliersWithTrimmedSearch() {
            Pageable pageable = PageRequest.of(0, 20);
            Supplier supplier = buildSupplier(supplierId, "Acme Distributors", true);
            Page<Supplier> expectedPage = new PageImpl<>(List.of(supplier), pageable, 1);

            mockBusinessExists();
            when(supplierRepository.searchByBusiness(businessId, "Acme", pageable))
                    .thenReturn(expectedPage);

            Page<Supplier> result = supplierService.getSuppliers(tenantId, businessId, "  Acme  ", pageable);

            assertEquals(1, result.getTotalElements());
            assertSame(supplier, result.getContent().get(0));
            verify(supplierRepository).searchByBusiness(businessId, "Acme", pageable);
        }

        @Test
        @DisplayName("Should pass null search when search is null")
        void shouldPassNullWhenSearchIsNull() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Supplier> expectedPage = new PageImpl<>(List.of(), pageable, 0);

            mockBusinessExists();
            when(supplierRepository.searchByBusiness(businessId, null, pageable))
                    .thenReturn(expectedPage);

            Page<Supplier> result = supplierService.getSuppliers(tenantId, businessId, null, pageable);

            assertTrue(result.isEmpty());
            verify(supplierRepository).searchByBusiness(businessId, null, pageable);
        }

        @Test
        @DisplayName("Should normalize blank search to null")
        void shouldNormalizeBlankSearchToNull() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Supplier> expectedPage = new PageImpl<>(List.of(), pageable, 0);

            mockBusinessExists();
            when(supplierRepository.searchByBusiness(businessId, null, pageable))
                    .thenReturn(expectedPage);

            Page<Supplier> result = supplierService.getSuppliers(tenantId, businessId, "   ", pageable);

            assertTrue(result.isEmpty());
            verify(supplierRepository).searchByBusiness(businessId, null, pageable);
        }

        @Test
        @DisplayName("Should allow reading suppliers even when business is inactive")
        void shouldAllowReadingWhenBusinessInactive() {
            business.setActive(false);
            Pageable pageable = PageRequest.of(0, 20);
            Supplier supplier = buildSupplier(supplierId, "Acme Distributors", true);
            Page<Supplier> expectedPage = new PageImpl<>(List.of(supplier), pageable, 1);

            mockBusinessExists();
            when(supplierRepository.searchByBusiness(businessId, null, pageable))
                    .thenReturn(expectedPage);

            Page<Supplier> result = supplierService.getSuppliers(tenantId, businessId, null, pageable);

            assertEquals(1, result.getTotalElements());
            verify(supplierRepository).searchByBusiness(businessId, null, pageable);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when business does not exist")
        void shouldThrowWhenBusinessNotFound() {
            Pageable pageable = PageRequest.of(0, 20);

            when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                    .thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(
                    EntityNotFoundException.class,
                    () -> supplierService.getSuppliers(tenantId, businessId, "Acme", pageable)
            );

            assertEquals("Business not found", ex.getMessage());
            verifyNoInteractions(supplierRepository);
        }
    }

    // =========================================================================
    // GET SUPPLIER BY ID
    // =========================================================================
    @Nested
    @DisplayName("Get Supplier By Id Tests")
    class GetSupplierByIdTests {

        @Test
        @DisplayName("Should return supplier when business and supplier exist")
        void shouldReturnSupplierSuccessfully() {
            Supplier supplier = buildSupplier(supplierId, "Acme Distributors", true);

            mockBusinessExists();
            when(supplierRepository.findByIdAndBusiness_Id(supplierId, businessId))
                    .thenReturn(Optional.of(supplier));

            Supplier result = supplierService.getSupplierById(tenantId, businessId, supplierId);

            assertSame(supplier, result);
            verify(supplierRepository).findByIdAndBusiness_Id(supplierId, businessId);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when business does not exist")
        void shouldThrowWhenBusinessNotFound() {
            when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                    .thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(
                    EntityNotFoundException.class,
                    () -> supplierService.getSupplierById(tenantId, businessId, supplierId)
            );

            assertEquals("Business not found", ex.getMessage());
            verifyNoInteractions(supplierRepository);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when supplier does not exist")
        void shouldThrowWhenSupplierNotFound() {
            mockBusinessExists();
            when(supplierRepository.findByIdAndBusiness_Id(supplierId, businessId))
                    .thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(
                    EntityNotFoundException.class,
                    () -> supplierService.getSupplierById(tenantId, businessId, supplierId)
            );

            assertEquals("Supplier not found", ex.getMessage());
        }
    }

    // =========================================================================
    // UPDATE SUPPLIER
    // =========================================================================
    @Nested
    @DisplayName("Update Supplier Tests")
    class UpdateSupplierTests {

        @Test
        @DisplayName("Should update supplier fields and trim name successfully")
        void shouldUpdateSupplierSuccessfully() {
            Supplier existing = buildSupplier(supplierId, "Old Name", false);
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "  New Name  ",
                    "John Smith",
                    "+254711111111",
                    "john@newname.test",
                    "456 Industrial Area",
                    "P059999999Z",
                    "Net 60",
                    "Updated terms"
            );

            mockBusinessExists();
            when(supplierRepository.findByIdAndBusiness_Id(supplierId, businessId))
                    .thenReturn(Optional.of(existing));
            when(supplierRepository.existsByBusiness_IdAndNameIgnoreCaseAndIdNot(businessId, "New Name", supplierId))
                    .thenReturn(false);
            when(supplierRepository.save(existing)).thenReturn(existing);

            Supplier result = supplierService.updateSupplier(tenantId, businessId, supplierId, request);

            assertSame(existing, result);
            assertEquals("New Name", result.getName());
            assertEquals("John Smith", result.getContactPerson());
            assertEquals("+254711111111", result.getPhone());
            assertEquals("john@newname.test", result.getEmail());
            assertEquals("456 Industrial Area", result.getAddress());
            assertEquals("P059999999Z", result.getTaxPin());
            assertEquals("Net 60", result.getPaymentTerms());
            assertEquals("Updated terms", result.getNotes());
            assertFalse(result.isActive());

            verify(supplierRepository).existsByBusiness_IdAndNameIgnoreCaseAndIdNot(businessId, "New Name", supplierId);
            verify(supplierRepository).save(existing);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when business does not exist")
        void shouldThrowWhenBusinessNotFound() {
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "New Name", null, null, null, null, null, null, null
            );

            when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                    .thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(
                    EntityNotFoundException.class,
                    () -> supplierService.updateSupplier(tenantId, businessId, supplierId, request)
            );

            assertEquals("Business not found", ex.getMessage());
            verifyNoInteractions(supplierRepository);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when business is inactive")
        void shouldThrowWhenBusinessInactive() {
            business.setActive(false);
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "New Name", null, null, null, null, null, null, null
            );

            mockBusinessExists();

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> supplierService.updateSupplier(tenantId, businessId, supplierId, request)
            );

            assertEquals("Business is inactive", ex.getMessage());
            verifyNoInteractions(supplierRepository);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when supplier does not exist")
        void shouldThrowWhenSupplierNotFound() {
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "New Name", null, null, null, null, null, null, null
            );

            mockBusinessExists();
            when(supplierRepository.findByIdAndBusiness_Id(supplierId, businessId))
                    .thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(
                    EntityNotFoundException.class,
                    () -> supplierService.updateSupplier(tenantId, businessId, supplierId, request)
            );

            assertEquals("Supplier not found", ex.getMessage());
            verify(supplierRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when another supplier uses the same name")
        void shouldThrowWhenDuplicateNameExists() {
            Supplier existing = buildSupplier(supplierId, "Old Name", true);
            UpdateSupplierRequest request = new UpdateSupplierRequest(
                    "  Existing Name  ", null, null, null, null, null, null, null
            );

            mockBusinessExists();
            when(supplierRepository.findByIdAndBusiness_Id(supplierId, businessId))
                    .thenReturn(Optional.of(existing));
            when(supplierRepository.existsByBusiness_IdAndNameIgnoreCaseAndIdNot(
                    businessId, "Existing Name", supplierId
            )).thenReturn(true);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> supplierService.updateSupplier(tenantId, businessId, supplierId, request)
            );

            assertEquals("Supplier with this name already exists", ex.getMessage());
            verify(supplierRepository, never()).save(any());
        }
    }

    // =========================================================================
    // UPDATE SUPPLIER STATUS
    // =========================================================================
    @Nested
    @DisplayName("Update Supplier Status Tests")
    class UpdateSupplierStatusTests {

        @Test
        @DisplayName("Should deactivate supplier when active is false")
        void shouldDeactivateSupplierSuccessfully() {
            Supplier supplier = buildSupplier(supplierId, "Acme Distributors", true);

            mockBusinessExists();
            when(supplierRepository.findByIdAndBusiness_Id(supplierId, businessId))
                    .thenReturn(Optional.of(supplier));
            when(supplierRepository.save(supplier)).thenReturn(supplier);

            Supplier result = supplierService.updateSupplierStatus(tenantId, businessId, supplierId, false);

            assertFalse(result.isActive());
            verify(supplierRepository).save(supplier);
        }

        @Test
        @DisplayName("Should activate supplier when active is true")
        void shouldActivateSupplierSuccessfully() {
            Supplier supplier = buildSupplier(supplierId, "Acme Distributors", false);

            mockBusinessExists();
            when(supplierRepository.findByIdAndBusiness_Id(supplierId, businessId))
                    .thenReturn(Optional.of(supplier));
            when(supplierRepository.save(supplier)).thenReturn(supplier);

            Supplier result = supplierService.updateSupplierStatus(tenantId, businessId, supplierId, true);

            assertTrue(result.isActive());
            verify(supplierRepository).save(supplier);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when business does not exist")
        void shouldThrowWhenBusinessNotFound() {
            when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                    .thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(
                    EntityNotFoundException.class,
                    () -> supplierService.updateSupplierStatus(tenantId, businessId, supplierId, false)
            );

            assertEquals("Business not found", ex.getMessage());
            verifyNoInteractions(supplierRepository);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when business is inactive")
        void shouldThrowWhenBusinessInactive() {
            business.setActive(false);
            mockBusinessExists();

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> supplierService.updateSupplierStatus(tenantId, businessId, supplierId, false)
            );

            assertEquals("Business is inactive", ex.getMessage());
            verifyNoInteractions(supplierRepository);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when supplier does not exist")
        void shouldThrowWhenSupplierNotFound() {
            mockBusinessExists();
            when(supplierRepository.findByIdAndBusiness_Id(supplierId, businessId))
                    .thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(
                    EntityNotFoundException.class,
                    () -> supplierService.updateSupplierStatus(tenantId, businessId, supplierId, true)
            );

            assertEquals("Supplier not found", ex.getMessage());
            verify(supplierRepository, never()).save(any());
        }
    }

    // =========================================================================
    // DELETE SUPPLIER
    // =========================================================================
    @Nested
    @DisplayName("Delete Supplier Tests")
    class DeleteSupplierTests {

        @Test
        @DisplayName("Should soft-delete supplier by setting active to false")
        void shouldDeleteSupplierSuccessfully() {
            Supplier supplier = buildSupplier(supplierId, "Acme Distributors", true);

            mockBusinessExists();
            when(supplierRepository.findByIdAndBusiness_Id(supplierId, businessId))
                    .thenReturn(Optional.of(supplier));
            when(supplierRepository.save(supplier)).thenReturn(supplier);

            Supplier result = supplierService.deleteSupplier(tenantId, businessId, supplierId);

            assertFalse(result.isActive());
            verify(supplierRepository).save(supplier);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when business does not exist")
        void shouldThrowWhenBusinessNotFound() {
            when(businessRepository.findByIdAndTenantId(businessId, tenantId))
                    .thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(
                    EntityNotFoundException.class,
                    () -> supplierService.deleteSupplier(tenantId, businessId, supplierId)
            );

            assertEquals("Business not found", ex.getMessage());
            verifyNoInteractions(supplierRepository);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when business is inactive")
        void shouldThrowWhenBusinessInactive() {
            business.setActive(false);
            mockBusinessExists();

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> supplierService.deleteSupplier(tenantId, businessId, supplierId)
            );

            assertEquals("Business is inactive", ex.getMessage());
            verifyNoInteractions(supplierRepository);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when supplier does not exist")
        void shouldThrowWhenSupplierNotFound() {
            mockBusinessExists();
            when(supplierRepository.findByIdAndBusiness_Id(supplierId, businessId))
                    .thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(
                    EntityNotFoundException.class,
                    () -> supplierService.deleteSupplier(tenantId, businessId, supplierId)
            );

            assertEquals("Supplier not found", ex.getMessage());
            verify(supplierRepository, never()).save(any());
        }
    }
}
