package com.possaas.possaasbackend.category.dto;

import com.possaas.possaasbackend.business.dto.BusinessResponse;
import com.possaas.possaasbackend.category.Category;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponse(

        UUID id,
        BusinessResponse business,
        ParentCategorySummary parentCategory,
        String name,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {

    public static CategoryResponse fromEntity(Category category) {

        if (category == null) {
            return null;
        }

        return new CategoryResponse(
                category.getId(),
                BusinessResponse.fromEntity(category.getBusiness()),
                category.getParentCategory() != null
                        ? ParentCategorySummary.fromEntity(category.getParentCategory())
                        : null,
                category.getName(),
                category.getDescription(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    public record ParentCategorySummary(
            UUID id,
            String name,
            String description,
            boolean active
    ) {

        public static ParentCategorySummary fromEntity(Category category) {

            if (category == null) {
                return null;
            }

            return new ParentCategorySummary(
                    category.getId(),
                    category.getName(),
                    category.getDescription(),
                    category.isActive()
            );
        }
    }
}
