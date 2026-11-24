package com.possaas.possaasbackend.subscription.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdatePlanRequest(

        @NotBlank(message = "Plan name is required")
        @Size(max = 100, message = "Plan name cannot exceed 100 characters")
        String name,

        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description,

        @NotNull(message = "Monthly price is required")
        @DecimalMin(value = "0.00", message = "Monthly price cannot be negative")
        BigDecimal monthlyPrice,

        @NotNull(message = "Yearly price is required")
        @DecimalMin(value = "0.00", message = "Yearly price cannot be negative")
        BigDecimal yearlyPrice,

        @NotBlank(message = "Currency is required")
        @Pattern(
                regexp = "^[A-Za-z]{3}$",
                message = "Currency must be a valid three-letter code"
        )
        String currency,

        @Positive(message = "Maximum branches must be greater than zero")
        Integer maxBranches,

        @Positive(message = "Maximum users must be greater than zero")
        Integer maxUsers

) {}
