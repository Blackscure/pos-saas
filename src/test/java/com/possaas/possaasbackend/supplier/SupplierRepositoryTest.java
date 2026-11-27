package com.possaas.possaasbackend.supplier;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class SupplierRepositoryTest {

    @Test
    void searchByBusinessDelegatesToFindAllWhenSearchIsNull() {
        SupplierRepository repository = mock(SupplierRepository.class, CALLS_REAL_METHODS);
        UUID businessId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Supplier> expected = new PageImpl<>(List.of(), pageable, 0);

        when(repository.findAllByBusiness_Id(businessId, pageable)).thenReturn(expected);

        Page<Supplier> result = repository.searchByBusiness(businessId, null, pageable);

        assertSame(expected, result);
        verify(repository).findAllByBusiness_Id(businessId, pageable);
        verify(repository, never()).searchByBusinessWithPattern(any(), any(), any());
    }

    @Test
    void searchByBusinessDelegatesToFindAllWhenSearchIsBlank() {
        SupplierRepository repository = mock(SupplierRepository.class, CALLS_REAL_METHODS);
        UUID businessId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Supplier> expected = new PageImpl<>(List.of(), pageable, 0);

        when(repository.findAllByBusiness_Id(businessId, pageable)).thenReturn(expected);

        Page<Supplier> result = repository.searchByBusiness(businessId, "   ", pageable);

        assertSame(expected, result);
        verify(repository).findAllByBusiness_Id(businessId, pageable);
        verify(repository, never()).searchByBusinessWithPattern(any(), any(), any());
    }

    @Test
    void searchByBusinessDelegatesToPatternQueryWhenSearchIsPresent() {
        SupplierRepository repository = mock(SupplierRepository.class, CALLS_REAL_METHODS);
        UUID businessId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Supplier> expected = new PageImpl<>(List.of(), pageable, 0);

        when(repository.searchByBusinessWithPattern(businessId, "%acme%", pageable)).thenReturn(expected);

        Page<Supplier> result = repository.searchByBusiness(businessId, "  AcMe  ", pageable);

        assertSame(expected, result);
        verify(repository).searchByBusinessWithPattern(businessId, "%acme%", pageable);
        verify(repository, never()).findAllByBusiness_Id(any(), any());
    }
}
