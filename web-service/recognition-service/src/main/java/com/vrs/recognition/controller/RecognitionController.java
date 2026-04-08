package com.vrs.recognition.controller;

import com.vrs.recognition.dto.request.RecognitionExecuteRequest;
import com.vrs.recognition.dto.response.RecognitionResponse;
import com.vrs.recognition.dto.response.RecognitionUploadResponse;
import com.vrs.recognition.service.RecognitionExecutionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping("/api/recognitions")
public class RecognitionController {

    private final RecognitionExecutionService recognitionExecutionService;

    public RecognitionController(RecognitionExecutionService recognitionExecutionService) {
        this.recognitionExecutionService = recognitionExecutionService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RecognitionUploadResponse upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(name = "fps", defaultValue = "5") @Min(1) @Max(30) int fps,
            @RequestParam(name = "durationSeconds", required = false) @Min(1) @Max(3600) Integer durationSeconds
    ) {
        return recognitionExecutionService.uploadVideo(file, fps, durationSeconds);
    }

    @PostMapping("/execute")
    public RecognitionResponse execute(@Valid @RequestBody RecognitionExecuteRequest request) {
        return recognitionExecutionService.executeRecognition(request);
    }

    @GetMapping
    public List<RecognitionResponse> list() {
        return recognitionExecutionService.findAll();
    }

    @GetMapping("/{id}")
    public RecognitionResponse findById(@PathVariable("id") Long recognitionId) {
        return recognitionExecutionService.findById(recognitionId);
    }
}
