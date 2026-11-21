package com.possaas.possaasbackend.productvariant.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignProductAttributeRequest(

        @NotNull(message = "Attribute ID is required")
        UUID attributeId

) {
}
