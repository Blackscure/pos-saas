
package com.possaas.possaasbackend.supplier.dto;

import com.possaas.possaasbackend.supplier.Supplier;

import java.time.LocalDateTime;
import java.util.UUID;

public record SupplierResponse(
        UUID id,
        UUID businessId,
        String name,
        String contactPerson,
        String phone,
        String email,
        String address,
        String taxPin,
        String paymentTerms,
        String notes,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static SupplierResponse from(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getBusiness().getId(),
                supplier.getName(),
                supplier.getContactPerson(),
                supplier.getPhone(),
                supplier.getEmail(),
                supplier.getAddress(),
                supplier.getTaxPin(),
                supplier.getPaymentTerms(),
                supplier.getNotes(),
                supplier.isActive(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt()
        );
    }
}
