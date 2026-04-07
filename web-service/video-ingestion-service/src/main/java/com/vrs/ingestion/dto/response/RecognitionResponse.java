package com.vrs.ingestion.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecognitionResponse {

    private Long id;
    private String result;
    private String file;
    private LocalDateTime date;
    private Float confidenceScore;
    private Long userId;
    private Long modelId;
    private String modelName;
    private Long vioPatternId;
}