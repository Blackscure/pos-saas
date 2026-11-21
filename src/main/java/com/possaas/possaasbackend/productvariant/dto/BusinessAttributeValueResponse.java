package com.possaas.possaasbackend.productvariant.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record BusinessAttributeValueResponse(

        UUID id,

        UUID businessId,

        UUID attributeId,

        String value,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
