package com.possaas.possaasbackend.tenant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantRequest {

    @NotBlank(message = "Tenant name is required")
    @Size(max = 150, message = "Tenant name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Tenant code is required")
    @Size(max = 100, message = "Tenant code must not exceed 100 characters")
    private String code;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    private Boolean active;
}
