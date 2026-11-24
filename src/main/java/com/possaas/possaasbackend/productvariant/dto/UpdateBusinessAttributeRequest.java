package com.possaas.possaasbackend.productvariant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBusinessAttributeRequest(

        @NotBlank(message = "Attribute name is required")
        @Size(max = 100, message = "Attribute name cannot exceed 100 characters")
        String name,

        @NotBlank(message = "Attribute code is required")
        @Size(max = 100, message = "Attribute code cannot exceed 100 characters")
        String code

) {
}
