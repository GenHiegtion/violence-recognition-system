package com.vrs.ingestion.service;

import com.vrs.ingestion.model.IngestionResponse;
import com.vrs.ingestion.model.Model;
import com.vrs.ingestion.model.Recognition;
import com.vrs.ingestion.model.RecognitionResponse;
import com.vrs.ingestion.model.StreamIngestionRequest;
import com.vrs.ingestion.repository.ModelRepository;
import com.vrs.ingestion.repository.RecognitionRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IngestionService {

    private final Map<String, IngestionResponse> jobs = new ConcurrentHashMap<>();
    private final ModelRepository modelRepository;
    private final RecognitionRepository recognitionRepository;

    public IngestionService(ModelRepository modelRepository, RecognitionRepository recognitionRepository) {
        this.modelRepository = modelRepository;
        this.recognitionRepository = recognitionRepository;
    }

    @PostConstruct
    public void bootstrapModel() {
        if (modelRepository.findByNameIgnoreCase("movinet").isPresent()) {
            return;
        }

        Model model = new Model();
        model.setName("movinet");
        model.setPt("movinet-a2.pt");
        modelRepository.save(model);
    }

    public IngestionResponse ingestUpload(MultipartFile file, int fps, Integer durationSeconds) {
        String id = UUID.randomUUID().toString();
        int inferredDuration = durationSeconds != null
                ? durationSeconds
                : inferDurationFromSize(file.getSize());

        String source = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();
        Recognition recognition = createPendingRecognition(source);

        IngestionResponse response = new IngestionResponse(
                id,
                recognition.getId(),
                "FILE",
                source,
                fps,
                (long) fps * inferredDuration,
                "QUEUED",
                LocalDateTime.now()
        );
        jobs.put(id, response);
        return response;
    }

    public IngestionResponse ingestStream(StreamIngestionRequest request) {
        String id = UUID.randomUUID().toString();
        Recognition recognition = createPendingRecognition(request.streamUrl());

        IngestionResponse response = new IngestionResponse(
                id,
            recognition.getId(),
                "STREAM",
                request.streamUrl(),
                request.fps(),
                (long) request.fps() * request.durationSeconds(),
                "QUEUED",
                LocalDateTime.now()
        );
        jobs.put(id, response);
        return response;
    }

    public List<IngestionResponse> recentJobs() {
        return jobs.values().stream()
                .sorted(Comparator.comparing(IngestionResponse::createdAt).reversed())
                .toList();
    }

    public List<RecognitionResponse> recognitions() {
        return recognitionRepository.findAll().stream()
                .sorted(Comparator.comparing(Recognition::getDate).reversed())
                .map(this::toRecognitionResponse)
                .toList();
    }

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

    private Model bootstrapAndGetModel() {
        Model model = new Model();
        model.setName("movinet");
        model.setPt("movinet-a2.pt");
        return modelRepository.save(model);
    }

    private RecognitionResponse toRecognitionResponse(Recognition recognition) {
        return new RecognitionResponse(
                recognition.getId(),
                recognition.getResult(),
                recognition.getFile(),
                recognition.getDate(),
                recognition.getConfidenceScore(),
                recognition.getUserId(),
                recognition.getModel().getId(),
                recognition.getModel().getName(),
                recognition.getVioPatternId()
        );
    }
}
