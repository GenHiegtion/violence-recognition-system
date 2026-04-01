package com.vrs.ingestion.model;

import java.time.LocalDateTime;

public record IngestionResponse(
        String ingestionId,
        Long recognitionId,
        String sourceType,
        String source,
        int fps,
        long estimatedFrames,
        String status,
        LocalDateTime createdAt
) {
}
