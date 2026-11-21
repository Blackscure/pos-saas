package com.possaas.possaasbackend.productvariant.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record BusinessAttributeResponse(

        UUID id,

        UUID businessId,

        String name,

        String code,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
