package com.vrs.pattern.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VioPatternRequest {

    @NotBlank(message = "Pattern name is required")
    private String name;

    @NotNull(message = "Severity level is required")
    @PositiveOrZero(message = "Severity level must be >= 0")
    private Integer sevLevel;

    @NotNull(message = "Threshold is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Threshold must be >= 0")
    @DecimalMax(value = "1.0", inclusive = true, message = "Threshold must be <= 1")
    private Double threshold;

    private String file;
}