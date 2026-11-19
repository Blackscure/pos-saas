package com.possaas.possaasbackend.product.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductRequest(

        @NotBlank(message = "SKU is required")
        @Size(max = 100, message = "SKU cannot exceed 100 characters")
        String sku,

        @NotBlank(message = "Product name is required")
        @Size(max = 200, message = "Product name cannot exceed 200 characters")
        String name,

        String description,

        @Size(max = 100, message = "Barcode cannot exceed 100 characters")
        String barcode,

        @Size(max = 1000, message = "Image URL cannot exceed 1000 characters")
        String imageUrl,

        @NotNull(message = "Purchase price is required")
        @DecimalMin(value = "0.00", message = "Purchase price cannot be negative")
        @Digits(integer = 15, fraction = 4)
        BigDecimal purchasePrice,

        @NotNull(message = "Selling price is required")
        @DecimalMin(value = "0.00", message = "Selling price cannot be negative")
        @Digits(integer = 15, fraction = 4)
        BigDecimal sellingPrice,

        @NotNull(message = "Tax rate is required")
        @DecimalMin(value = "0.00", message = "Tax rate cannot be negative")
        @DecimalMax(value = "100.00", message = "Tax rate cannot exceed 100")
        @Digits(integer = 3, fraction = 4)
        BigDecimal taxRate,

        boolean taxInclusive,

        UUID categoryId

) {}
