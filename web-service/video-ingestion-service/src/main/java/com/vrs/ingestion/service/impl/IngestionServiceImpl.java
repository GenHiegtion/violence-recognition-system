package com.vrs.ingestion.service.impl;

import com.vrs.ingestion.dto.request.StreamIngestionRequest;
import com.vrs.ingestion.dto.response.IngestionResponse;
import com.vrs.ingestion.dto.response.RecognitionResponse;
import com.vrs.ingestion.model.Model;
import com.vrs.ingestion.model.Recognition;
import com.vrs.ingestion.repository.ModelRepository;
import com.vrs.ingestion.repository.RecognitionRepository;
import com.vrs.ingestion.service.IngestionService;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class IngestionServiceImpl implements IngestionService {

    private final Map<String, IngestionResponse> jobs = new ConcurrentHashMap<>();
    private final ModelRepository modelRepository;
    private final RecognitionRepository recognitionRepository;
    private final Path uploadDirectory;

    public IngestionServiceImpl(
            ModelRepository modelRepository,
            RecognitionRepository recognitionRepository,
            @Value("${app.upload-dir:../../uploads}") String uploadDirectory
    ) {
        this.modelRepository = modelRepository;
        this.recognitionRepository = recognitionRepository;
        this.uploadDirectory = Paths.get(uploadDirectory).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void initializeData() {
        ensureUploadDirectory();

        if (modelRepository.findByNameIgnoreCase("movinet").isPresent()) {
            return;
        }

        Model model = new Model();
        model.setName("movinet");
        model.setPt("movinet-a2.pt");
        modelRepository.save(model);
    }

    @Override
    public IngestionResponse ingestUpload(MultipartFile file, int fps, Integer durationSeconds) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is empty");
        }

        String id = UUID.randomUUID().toString();
        int inferredDuration = durationSeconds != null
                ? durationSeconds
                : inferDurationFromSize(file.getSize());

        String source = storeUploadedFile(file, id);
        Recognition recognition = createPendingRecognition(source);

        IngestionResponse response = IngestionResponse.builder()
                .ingestionId(id)
                .recognitionId(recognition.getId())
                .sourceType("FILE")
                .source(source)
                .fps(fps)
                .estimatedFrames((long) fps * inferredDuration)
                .status("QUEUED")
                .createdAt(LocalDateTime.now())
                .build();
        jobs.put(id, response);
        return response;
    }

    @Override
    public IngestionResponse ingestStream(StreamIngestionRequest request) {
        String id = UUID.randomUUID().toString();
        Recognition recognition = createPendingRecognition(request.getStreamUrl());

        IngestionResponse response = IngestionResponse.builder()
                .ingestionId(id)
                .recognitionId(recognition.getId())
                .sourceType("STREAM")
                .source(request.getStreamUrl())
                .fps(request.getFps())
                .estimatedFrames((long) request.getFps() * request.getDurationSeconds())
                .status("QUEUED")
                .createdAt(LocalDateTime.now())
                .build();
        jobs.put(id, response);
        return response;
    }

    @Override
    public List<IngestionResponse> recentJobs() {
        return jobs.values().stream()
                .sorted(Comparator.comparing(IngestionResponse::getCreatedAt).reversed())
                .toList();
    }

    @Override
    public List<RecognitionResponse> recognitions() {
        return recognitionRepository.findAll().stream()
                .sorted(Comparator.comparing(Recognition::getDate).reversed())
                .map(this::toRecognitionResponse)
                .toList();
    }

    @Override
    public List<Model> models() {
        return modelRepository.findAll();
    }

    private int inferDurationFromSize(long fileSizeBytes) {
        long by200kb = fileSizeBytes / 200_000L;
        return (int) Math.max(1, Math.min(60, by200kb));
    }

    private Recognition createPendingRecognition(String file) {
        Model model = modelRepository.findByNameIgnoreCase("movinet")
                .orElseGet(this::bootstrapAndGetModel);

        Recognition recognition = new Recognition();
        recognition.setResult("PENDING");
        recognition.setFile(file);
        recognition.setDate(LocalDateTime.now());
        recognition.setConfidenceScore(0.0f);
        recognition.setModel(model);
        recognition.setUserId(null);
        recognition.setVioPatternId(null);
        return recognitionRepository.save(recognition);
    }

    private void ensureUploadDirectory() {
        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot initialize upload directory: " + uploadDirectory, ex);
        }
    }

    private String storeUploadedFile(MultipartFile file, String ingestionId) {
        String originalName = file.getOriginalFilename() == null ? "upload.bin" : file.getOriginalFilename();
        String safeName = Paths.get(originalName)
                .getFileName()
                .toString()
                .replaceAll("[^A-Za-z0-9._-]", "_");

        Path targetFile = uploadDirectory.resolve(ingestionId + "_" + safeName);

        try {
            file.transferTo(targetFile);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store uploaded file", ex);
        }

        return targetFile.toString();
    }

    private Model bootstrapAndGetModel() {
        Model model = new Model();
        model.setName("movinet");
        model.setPt("movinet-a2.pt");
        return modelRepository.save(model);
    }

    private RecognitionResponse toRecognitionResponse(Recognition recognition) {
        return RecognitionResponse.builder()
                .id(recognition.getId())
                .result(recognition.getResult())
                .file(recognition.getFile())
                .date(recognition.getDate())
                .confidenceScore(recognition.getConfidenceScore())
                .userId(recognition.getUserId())
                .modelId(recognition.getModel().getId())
                .modelName(recognition.getModel().getName())
                .vioPatternId(recognition.getVioPatternId())
                .build();
    }
}