package com.possaas.possaasbackend.business.dto;

import com.possaas.possaasbackend.business.BusinessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessRequest {

    @NotBlank(message = "Business name is required")
    @Size(max = 150, message = "Business name must not exceed 150 characters")
    private String name;

    @NotNull(message = "Business type is required")
    private BusinessType businessType;

    @Size(max = 100, message = "Registration number must not exceed 100 characters")
    private String registrationNumber;

    @Size(max = 30, message = "Phone must not exceed 30 characters")
    private String phone;

    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Size(max = 3, message = "Currency must not exceed 3 characters")
    private String currency;

    private Boolean taxEnabled;

    private Boolean active;
}
