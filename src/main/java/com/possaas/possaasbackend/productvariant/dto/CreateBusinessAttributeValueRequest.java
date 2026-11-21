package com.possaas.possaasbackend.productvariant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBusinessAttributeValueRequest(

        @NotBlank(message = "Attribute value is required")
        @Size(max = 100, message = "Attribute value cannot exceed 100 characters")
        String value

) {
}
