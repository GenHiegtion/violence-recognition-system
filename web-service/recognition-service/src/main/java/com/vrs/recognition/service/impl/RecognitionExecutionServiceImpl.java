package com.vrs.recognition.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vrs.recognition.dto.request.RecognitionExecuteRequest;
import com.vrs.recognition.dto.response.RecognitionResponse;
import com.vrs.recognition.dto.response.RecognitionUploadResponse;
import com.vrs.recognition.model.RecognitionEntity;
import com.vrs.recognition.repository.RecognitionRepository;
import com.vrs.recognition.service.RecognitionExecutionService;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
public class RecognitionExecutionServiceImpl implements RecognitionExecutionService {

    private static final int DEFAULT_FRAMES_COUNT = 32;
    private static final String DEFAULT_PATTERN_CODE = "violence";

    private final RecognitionRepository recognitionRepository;
    private final RestClient aiClient;
    private final RestClient modelClient;
    private final String defaultPatternCode;
    private final Path uploadDirectory;

    public RecognitionExecutionServiceImpl(
            RecognitionRepository recognitionRepository,
            RestClient.Builder restClientBuilder,
            @Value("${app.ai-service-url:http://localhost:8000}") String aiServiceUrl,
            @Value("${app.model-service-url:http://localhost:8084}") String modelServiceUrl,
            @Value("${app.default-pattern-code:violence}") String defaultPatternCode,
            @Value("${app.upload-dir:${user.dir}/../../uploads/recognition}") String uploadDirectory
    ) {
        this.recognitionRepository = recognitionRepository;
        this.aiClient = restClientBuilder.baseUrl(Objects.requireNonNull(aiServiceUrl)).build();
        this.modelClient = restClientBuilder.baseUrl(Objects.requireNonNull(modelServiceUrl)).build();
        this.defaultPatternCode = hasText(defaultPatternCode) ? defaultPatternCode.trim() : DEFAULT_PATTERN_CODE;
        this.uploadDirectory = Paths.get(uploadDirectory).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void initializeUploadDirectory() {
        ensureUploadDirectory();
    }

    @Override
    public RecognitionUploadResponse uploadVideo(MultipartFile file, int fps, Integer durationSeconds) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is empty");
        }

        String uploadId = UUID.randomUUID().toString();
        int inferredDuration = durationSeconds != null
                ? durationSeconds
                : inferDurationFromSize(file.getSize());

        String source = storeUploadedFile(file, uploadId);
        return RecognitionUploadResponse.builder()
                .uploadId(uploadId)
                .source(source)
                .fps(fps)
                .estimatedFrames((long) fps * inferredDuration)
                .status("UPLOADED")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    @SuppressWarnings("null")
    public RecognitionResponse executeRecognition(RecognitionExecuteRequest request) {
        long startedAt = System.currentTimeMillis();
        log.info(
            "[OUTBOUND] recognition-service preparing execution sourceId={} modelId={} source={}",
            request.getSourceId(),
            request.getModelId(),
            request.getSource()
        );

        ModelResponse model = fetchModel(request.getModelId());

        AiInferenceRequest aiRequest = new AiInferenceRequest(
                request.getSourceId(),
                resolvePatternCode(request.getPatternCode()),
                request.getFramesCount() != null ? request.getFramesCount() : DEFAULT_FRAMES_COUNT,
                null,
                request.getSource(),
                model.pt(),
                resolveModelVariant(model)
        );

        AiInferenceResponse aiResponse;
        try {
            log.info(
                "[OUTBOUND] recognition-service calling ai-service POST /api/ai/infer sourceId={} model={} pt={}",
                request.getSourceId(),
                model.name(),
                model.pt()
            );
            aiResponse = aiClient.post()
                    .uri("/api/ai/infer")
                    .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                    .body(aiRequest)
                    .retrieve()
                    .body(AiInferenceResponse.class);
        } catch (RestClientException ex) {
            log.error("[OUTBOUND] recognition-service failed calling ai-service", ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to call AI inference service", ex);
        }

        if (aiResponse == null || aiResponse.score() == null || aiResponse.label() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI inference service returned invalid response");
        }

        log.info(
            "[OUTBOUND] recognition-service ai-service response sourceId={} label={} score={}",
            request.getSourceId(),
            aiResponse.label(),
            aiResponse.score()
        );

        String resultLabel = "Violence".equalsIgnoreCase(aiResponse.label()) ? "Violence" : "Non-violence";
        float confidenceScore = "Violence".equals(resultLabel)
            ? aiResponse.score().floatValue()
            : (float) (1.0 - aiResponse.score());

        RecognitionEntity recognition = RecognitionEntity.builder()
            .result(resultLabel)
                .file(request.getSource())
                .date(LocalDateTime.now())
            .confidenceScore(confidenceScore)
                .userId(request.getUserId())
                .vioPatternId(request.getVioPatternId())
                .modelId(model.id())
                .modelName(model.name())
                .build();

        var saved = recognitionRepository.save(recognition);
        log.info(
            "[OUTBOUND] recognition-service execution persisted recognitionId={} durationMs={}",
            saved.getId(),
            System.currentTimeMillis() - startedAt
        );
        return toResponse(saved);
    }

    @Override
    public List<RecognitionResponse> findAll() {
        return recognitionRepository.findAll().stream()
                .sorted(Comparator.comparing(RecognitionEntity::getDate).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Override
    public RecognitionResponse findById(Long recognitionId) {
        RecognitionEntity recognition = recognitionRepository.findById(Objects.requireNonNull(recognitionId, "recognitionId is required"))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Recognition not found: " + recognitionId
                ));
        return toResponse(recognition);
    }

    private ModelResponse fetchModel(Long modelId) {
        try {
            log.info("[OUTBOUND] recognition-service calling model-service GET /api/models/{}", modelId);
            ModelResponse model = modelClient.get()
                    .uri("/api/models/{id}", modelId)
                    .retrieve()
                    .body(ModelResponse.class);

            if (model == null || model.id() == null || !hasText(model.pt())) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Model service returned invalid model data");
            }

            log.info(
                    "[OUTBOUND] recognition-service model-service response id={} name={} pt={}",
                    model.id(),
                    model.name(),
                    model.pt()
            );
            return model;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (RestClientException ex) {
            log.error("[OUTBOUND] recognition-service failed calling model-service", ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to call model service", ex);
        }
    }

    private String resolvePatternCode(String requestedPatternCode) {
        return hasText(requestedPatternCode) ? requestedPatternCode.trim() : defaultPatternCode;
    }

    private String resolveModelVariant(ModelResponse model) {
        String source = (model.name() == null ? "" : model.name())
                + " "
                + (model.pt() == null ? "" : model.pt());

        for (String variant : List.of("A0", "A1", "A2", "A3", "A4", "A5")) {
            if (source.toUpperCase().contains(variant)) {
                return variant;
            }
        }
        return "A0";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private int inferDurationFromSize(long fileSizeBytes) {
        long by200kb = fileSizeBytes / 200_000L;
        return (int) Math.max(1, Math.min(60, by200kb));
    }

    private void ensureUploadDirectory() {
        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot initialize upload directory: " + uploadDirectory, ex);
        }
    }

    private String storeUploadedFile(MultipartFile file, String uploadId) {
        String originalName = file.getOriginalFilename() == null ? "upload.bin" : file.getOriginalFilename();
        String safeName = Paths.get(originalName)
                .getFileName()
                .toString()
                .replaceAll("[^A-Za-z0-9._-]", "_");

        Path targetFile = uploadDirectory.resolve(uploadId + "_" + safeName);

        try {
            file.transferTo(Objects.requireNonNull(targetFile));
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store uploaded file", ex);
        }

        return targetFile.toString();
    }

    private RecognitionResponse toResponse(RecognitionEntity recognition) {
        return RecognitionResponse.builder()
                .id(recognition.getId())
                .result(recognition.getResult())
                .file(recognition.getFile())
                .date(recognition.getDate())
                .confidenceScore(recognition.getConfidenceScore())
                .userId(recognition.getUserId())
                .modelId(recognition.getModelId())
                .modelName(recognition.getModelName())
                .vioPatternId(recognition.getVioPatternId())
                .build();
    }

    private record AiInferenceRequest(
            @JsonProperty("source_id") String sourceId,
            @JsonProperty("pattern_code") String patternCode,
            @JsonProperty("frames_count") int framesCount,
            @JsonProperty("violence_score") Double violenceScore,
            @JsonProperty("video_path") String videoPath,
            @JsonProperty("model_pt") String modelPt,
            @JsonProperty("model_name") String modelName
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AiInferenceResponse(
            Double score,
            String label
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ModelResponse(
            Long id,
            String name,
            String pt
    ) {
    }
}
