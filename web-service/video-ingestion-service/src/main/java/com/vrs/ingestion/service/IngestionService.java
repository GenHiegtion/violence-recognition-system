package com.vrs.ingestion.service;

import com.vrs.ingestion.dto.request.StreamIngestionRequest;
import com.vrs.ingestion.dto.response.IngestionResponse;
import com.vrs.ingestion.dto.response.RecognitionResponse;
import com.vrs.ingestion.model.Model;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface IngestionService {

    IngestionResponse ingestUpload(MultipartFile file, int fps, Integer durationSeconds);

    IngestionResponse ingestStream(StreamIngestionRequest request);

    List<IngestionResponse> recentJobs();

    List<RecognitionResponse> recognitions();

    List<Model> models();
}