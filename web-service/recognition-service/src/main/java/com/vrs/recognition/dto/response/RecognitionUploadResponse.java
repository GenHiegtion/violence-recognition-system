package com.vrs.recognition.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecognitionUploadResponse {

    private String uploadId;
    private String source;
    private int fps;
    private long estimatedFrames;
    private String status;
    private LocalDateTime createdAt;
}
