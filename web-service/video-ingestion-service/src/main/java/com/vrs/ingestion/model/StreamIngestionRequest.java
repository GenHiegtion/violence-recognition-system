package com.vrs.ingestion.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record StreamIngestionRequest(
        @NotBlank(message = "streamUrl is required")
        String streamUrl,
        @Min(value = 1, message = "fps must be >= 1")
        @Max(value = 30, message = "fps must be <= 30")
        int fps,
        @Min(value = 1, message = "durationSeconds must be >= 1")
        @Max(value = 3600, message = "durationSeconds must be <= 3600")
        int durationSeconds
) {
}
