
package com.possaas.possaasbackend.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSupplierRequest(

        @NotBlank(message = "Supplier name is required")
        @Size(max = 150, message = "Supplier name must not exceed 150 characters")
        String name,

        @Size(max = 150, message = "Contact person must not exceed 150 characters")
        String contactPerson,

        @Size(max = 30, message = "Phone must not exceed 30 characters")
        String phone,

        @Email(message = "Invalid email address")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        String address,

        @Size(max = 50, message = "Tax PIN must not exceed 50 characters")
        String taxPin,

        @Size(max = 100, message = "Payment terms must not exceed 100 characters")
        String paymentTerms,

        String notes
) {
}
