package com.possaas.possaasbackend.productvariant.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductVariantRequest(

        @Size(max = 100, message = "SKU cannot exceed 100 characters")
        String sku,

        @Size(max = 100, message = "Barcode cannot exceed 100 characters")
        String barcode,

        @Size(max = 200, message = "Variant name cannot exceed 200 characters")
        String name,

        @DecimalMin(value = "0.0", message = "Purchase price cannot be negative")
        @Digits(integer = 11, fraction = 4, message = "Invalid purchase price")
        BigDecimal purchasePrice,

        @DecimalMin(value = "0.0", message = "Selling price cannot be negative")
        @Digits(integer = 11, fraction = 4, message = "Invalid selling price")

        BigDecimal sellingPrice

) {
}
