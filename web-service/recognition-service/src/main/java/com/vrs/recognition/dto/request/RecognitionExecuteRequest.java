package com.vrs.recognition.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecognitionExecuteRequest {

    @NotBlank(message = "source is required")
    private String source;

    private String sourceId;

    @NotNull(message = "modelId is required")
    private Long modelId;

    private Long userId;

    private Long vioPatternId;

    private String patternCode;

    @Min(value = 1, message = "framesCount must be >= 1")
    @Max(value = 10000, message = "framesCount must be <= 10000")
    private Integer framesCount;
}
