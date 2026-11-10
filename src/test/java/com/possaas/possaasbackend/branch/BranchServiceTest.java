package com.possaas.possaasbackend.branch;

import com.possaas.possaasbackend.branch.dto.CreateBranchRequest;
import com.possaas.possaasbackend.branch.dto.UpdateBranchRequest;
import com.possaas.possaasbackend.business.Business;
import com.possaas.possaasbackend.business.BusinessRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private BranchService branchService;

    private UUID tenantId;
    private UUID businessId;
    private UUID branchId;
    private Business business;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        businessId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        business = new Business();
        business.setActive(true);
    }

    private void mockBusinessExists() {
        when(businessRepository.findByIdAndTenantId(
                businessId, tenantId))
                .thenReturn(Optional.of(business));
    }

    // CREATE BRANCH

    @Test
    void shouldCreateBranchSuccessfully() {

        CreateBranchRequest request = new CreateBranchRequest(
                "Nairobi Main Branch",
                "NRB001",
                "+254712345678",
                "nairobi@example.com",
                "Westlands, Nairobi"
        );

        mockBusinessExists();

        when(branchRepository.existsByBusiness_IdAndName(
                businessId, request.name()))
                .thenReturn(false);

        when(branchRepository.existsByBusiness_IdAndCode(
                businessId, request.code()))
                .thenReturn(false);

        when(branchRepository.save(any(Branch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Branch result = branchService.createBranch(
                tenantId, businessId, request);

        assertNotNull(result);
        assertEquals(request.name(), result.getName());
        assertEquals(request.code(), result.getCode());
        assertEquals(request.phone(), result.getPhone());
        assertEquals(request.email(), result.getEmail());
        assertEquals(request.address(), result.getAddress());
        assertSame(business, result.getBusiness());
        assertTrue(result.isActive());

        verify(branchRepository).save(any(Branch.class));
    }

    @Test
    void shouldThrowExceptionWhenBusinessDoesNotExist() {

        CreateBranchRequest request = new CreateBranchRequest(
                "Nairobi Main Branch",
                "NRB001",
                null,
                null,
                null
        );

        when(businessRepository.findByIdAndTenantId(
                businessId, tenantId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> branchService.createBranch(
                        tenantId, businessId, request));

        verify(branchRepository, never()).save(any());
    }

    @Test
    void shouldNotCreateBranchUnderInactiveBusiness() {

        business.setActive(false);

        CreateBranchRequest request = new CreateBranchRequest(
                "Nairobi Main Branch",
                "NRB001",
                null,
                null,
                null
        );

        mockBusinessExists();

        assertThrows(IllegalArgumentException.class,
                () -> branchService.createBranch(
                        tenantId, businessId, request));

        verify(branchRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateBranchName() {

        CreateBranchRequest request = new CreateBranchRequest(
                "Nairobi Main Branch",
                "NRB001",
                null,
                null,
                null
        );

        mockBusinessExists();

        when(branchRepository.existsByBusiness_IdAndName(
                businessId, request.name()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> branchService.createBranch(
                        tenantId, businessId, request));

        verify(branchRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateBranchCode() {

        CreateBranchRequest request = new CreateBranchRequest(
                "Nairobi Main Branch",
                "NRB001",
                null,
                null,
                null
        );

        mockBusinessExists();

        when(branchRepository.existsByBusiness_IdAndName(
                businessId, request.name()))
                .thenReturn(false);

        when(branchRepository.existsByBusiness_IdAndCode(
                businessId, request.code()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> branchService.createBranch(
                        tenantId, businessId, request));

        verify(branchRepository, never()).save(any());
    }

    // GET BRANCH

    @Test
    void shouldReturnBranchById() {

        Branch branch = new Branch();

        mockBusinessExists();

        when(branchRepository.findByIdAndBusiness_Id(
                branchId, businessId))
                .thenReturn(Optional.of(branch));

        Branch result = branchService.getBranchById(
                tenantId, businessId, branchId);

        assertNotNull(result);

        verify(branchRepository)
                .findByIdAndBusiness_Id(branchId, businessId);
    }

    @Test
    void shouldThrowExceptionWhenBranchDoesNotExist() {

        mockBusinessExists();

        when(branchRepository.findByIdAndBusiness_Id(
                branchId, businessId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> branchService.getBranchById(
                        tenantId, businessId, branchId));
    }

    // UPDATE BRANCH

    @Test
    void shouldUpdateBranchSuccessfully() {

        Branch existingBranch = new Branch();
        existingBranch.setName("Old Branch");
        existingBranch.setCode("OLD001");
        existingBranch.setBusiness(business);

        UpdateBranchRequest request = new UpdateBranchRequest(
                "Updated Branch",
                "NEW001",
                "+254700000000",
                "updated@example.com",
                "Nairobi"
        );

        mockBusinessExists();

        when(branchRepository.findByIdAndBusiness_Id(
                branchId, businessId))
                .thenReturn(Optional.of(existingBranch));

        when(branchRepository.existsByBusiness_IdAndName(
                businessId, request.name()))
                .thenReturn(false);

        when(branchRepository.existsByBusiness_IdAndCode(
                businessId, request.code()))
                .thenReturn(false);

        when(branchRepository.save(any(Branch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Branch result = branchService.updateBranch(
                tenantId, businessId, branchId, request);

        assertEquals("Updated Branch", result.getName());
        assertEquals("NEW001", result.getCode());
        assertEquals("+254700000000", result.getPhone());

        verify(branchRepository).save(existingBranch);
    }

    // STATUS

    @Test
    void shouldDeactivateBranch() {

        Branch branch = new Branch();
        branch.setActive(true);
        branch.setBusiness(business);

        mockBusinessExists();

        when(branchRepository.findByIdAndBusiness_Id(
                branchId, businessId))
                .thenReturn(Optional.of(branch));

        when(branchRepository.save(any(Branch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Branch result = branchService.updateBranchStatus(
                tenantId, businessId, branchId, false);

        assertFalse(result.isActive());
    }

    @Test
    void shouldNotActivateBranchUnderInactiveBusiness() {

        business.setActive(false);

        Branch branch = new Branch();
        branch.setActive(false);
        branch.setBusiness(business);

        mockBusinessExists();

        when(branchRepository.findByIdAndBusiness_Id(
                branchId, businessId))
                .thenReturn(Optional.of(branch));

        assertThrows(IllegalArgumentException.class,
                () -> branchService.updateBranchStatus(
                        tenantId, businessId, branchId, true));

        verify(branchRepository, never()).save(any());
    }

    // SOFT DELETE

    @Test
    void shouldSoftDeleteBranch() {

        Branch branch = new Branch();
        branch.setActive(true);
        branch.setBusiness(business);

        mockBusinessExists();

        when(branchRepository.findByIdAndBusiness_Id(
                branchId, businessId))
                .thenReturn(Optional.of(branch));

        when(branchRepository.save(any(Branch.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Branch result = branchService.deleteBranch(
                tenantId, businessId, branchId);

        assertFalse(result.isActive());

        verify(branchRepository).save(branch);
    }
}
