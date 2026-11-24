package com.possaas.possaasbackend.platformadmin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PlatformAdminLoginRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email")
        String email,

        @NotBlank(message = "Password is required")
        String password

) {
}