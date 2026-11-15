package com.possaas.possaasbackend.category.dto;

import com.possaas.possaasbackend.category.Category;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponse(

        UUID id,
        UUID businessId,
        UUID parentCategoryId,
        String parentCategoryName,
        String name,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {

    public static CategoryResponse fromEntity(Category category) {

        return new CategoryResponse(
                category.getId(),
                category.getBusiness().getId(),
                category.getParentCategory() != null
                        ? category.getParentCategory().getId()
                        : null,
                category.getParentCategory() != null
                        ? category.getParentCategory().getName()
                        : null,
                category.getName(),
                category.getDescription(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
