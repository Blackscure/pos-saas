package com.possaas.possaasbackend.productvariant.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SetProductVariantAttributeValueRequest(

        @NotNull(message = "Attribute value ID is required")
        UUID attributeValueId

) {
}
