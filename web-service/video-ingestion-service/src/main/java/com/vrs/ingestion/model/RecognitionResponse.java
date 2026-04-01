package com.vrs.ingestion.model;

import java.time.LocalDateTime;

public record RecognitionResponse(
        Long id,
        String result,
        String file,
        LocalDateTime date,
        Float confidenceScore,
        Long userId,
        Long modelId,
        String modelName,
        Long vioPatternId
) {
}
