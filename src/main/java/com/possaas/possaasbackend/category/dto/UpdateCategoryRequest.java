package com.possaas.possaasbackend.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateCategoryRequest(

        @NotBlank(message = "Category name is required")
        @Size(max = 150, message = "Category name cannot exceed 150 characters")
        String name,

        String description,

        UUID parentCategoryId

) {
}
