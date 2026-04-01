package com.vrs.ingestion.controller;

import com.vrs.ingestion.model.IngestionResponse;
import com.vrs.ingestion.model.Model;
import com.vrs.ingestion.model.RecognitionResponse;
import com.vrs.ingestion.model.StreamIngestionRequest;
import com.vrs.ingestion.service.IngestionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
@RequestMapping("/api/ingestion")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public IngestionResponse upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(name = "fps", defaultValue = "5") @Min(1) @Max(30) int fps,
            @RequestParam(name = "durationSeconds", required = false) @Min(1) @Max(3600) Integer durationSeconds
    ) {
        return ingestionService.ingestUpload(file, fps, durationSeconds);
    }

    @PostMapping("/stream")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public IngestionResponse stream(@Valid @RequestBody StreamIngestionRequest request) {
        return ingestionService.ingestStream(request);
    }

    @GetMapping("/jobs")
    public List<IngestionResponse> jobs() {
        return ingestionService.recentJobs();
    }

    @GetMapping("/recognitions")
    public List<RecognitionResponse> recognitions() {
        return ingestionService.recognitions();
    }

    @GetMapping("/models")
    public List<Model> models() {
        return ingestionService.models();
    }
}
