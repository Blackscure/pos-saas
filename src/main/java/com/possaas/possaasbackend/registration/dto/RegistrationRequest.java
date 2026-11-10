package com.possaas.possaasbackend.registration.dto;

import com.possaas.possaasbackend.business.BusinessType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(

        @Valid
        @NotNull(message = "Tenant details are required")
        TenantDetails tenant,

        @Valid
        @NotNull(message = "Business details are required")
        BusinessDetails business,

        @Valid
        @NotNull(message = "Branch details are required")
        BranchDetails branch,

        @Valid
        @NotNull(message = "Owner details are required")
        OwnerDetails owner

) {

    public record TenantDetails(

            @NotBlank(message = "Tenant name is required")
            @Size(max = 150)
            String name,

            @NotBlank(message = "Tenant code is required")
            @Pattern(
                    regexp = "^[A-Za-z0-9_-]{3,30}$",
                    message = "Tenant code must be 3-30 characters and contain only letters, numbers, hyphens or underscores"
            )
            String code,

            @NotBlank(message = "Tenant email is required")
            @Email(message = "Please provide a valid tenant email")
            @Size(max = 150)
            String email

    ) {}

    public record BusinessDetails(

            @NotBlank(message = "Business name is required")
            @Size(max = 150)
            String name,

            @NotNull(message = "Business type is required")
            BusinessType businessType,

            @Size(max = 100)
            String registrationNumber,

            @Size(max = 30)
            String phone,

            @Email(message = "Please provide a valid business email")
            @Size(max = 150)
            String email,

            @Size(max = 255)
            String address,

            @Size(max = 10)
            String currency,

            Boolean taxEnabled

    ) {}

    public record BranchDetails(

            @NotBlank(message = "Branch name is required")
            @Size(max = 150)
            String name,

            @NotBlank(message = "Branch code is required")
            @Pattern(
                    regexp = "^[A-Za-z0-9_-]{2,30}$",
                    message = "Branch code must be 2-30 characters and contain only letters, numbers, hyphens or underscores"
            )
            String code,

            @Size(max = 30)
            String phone,

            @Email(message = "Please provide a valid branch email")
            @Size(max = 150)
            String email,

            @Size(max = 255)
            String address

    ) {}

    public record OwnerDetails(

            @NotBlank(message = "Owner first name is required")
            @Size(max = 100)
            String firstName,

            @NotBlank(message = "Owner last name is required")
            @Size(max = 100)
            String lastName,

            @NotBlank(message = "Owner email is required")
            @Email(message = "Please provide a valid owner email")
            @Size(max = 150)
            String email,

            @Size(max = 30)
            String phone,

            @NotBlank(message = "Owner password is required")
            @Size(
                    min = 8,
                    max = 100,
                    message = "Password must be between 8 and 100 characters"
            )
            String password

    ) {}
}