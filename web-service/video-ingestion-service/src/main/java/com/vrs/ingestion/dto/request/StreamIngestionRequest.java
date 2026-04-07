package com.vrs.ingestion.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreamIngestionRequest {

    @NotBlank(message = "streamUrl is required")
    private String streamUrl;

    @Min(value = 1, message = "fps must be >= 1")
    @Max(value = 30, message = "fps must be <= 30")
    private int fps;

    @Min(value = 1, message = "durationSeconds must be >= 1")
    @Max(value = 3600, message = "durationSeconds must be <= 3600")
    private int durationSeconds;
}