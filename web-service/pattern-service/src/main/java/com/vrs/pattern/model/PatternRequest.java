package com.vrs.pattern.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record PatternRequest(
        @NotBlank(message = "Pattern name is required")
        String name,
        @NotNull(message = "Severity level is required")
        @PositiveOrZero(message = "Severity level must be >= 0")
        Integer sevLevel,
        @NotNull(message = "Threshold is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Threshold must be >= 0")
        @DecimalMax(value = "1.0", inclusive = true, message = "Threshold must be <= 1")
        Double threshold,
        String file
) {
}
