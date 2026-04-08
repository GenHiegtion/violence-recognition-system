package com.vrs.model.service.impl;

import com.vrs.model.dto.ModelResponse;
import com.vrs.model.model.ModelEntity;
import com.vrs.model.repository.ModelRepository;
import com.vrs.model.service.ModelCatalogService;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ModelCatalogServiceImpl implements ModelCatalogService {

    private static final Pattern MOVINET_VARIANT_PATTERN = Pattern.compile("(a[0-5])", Pattern.CASE_INSENSITIVE);

    private final ModelRepository modelRepository;
    private final Path aiWeightsDirectory;

    public ModelCatalogServiceImpl(
            ModelRepository modelRepository,
            @Value("${app.ai-weights-dir:../../ai-service/weights}") String aiWeightsDirectory
    ) {
        this.modelRepository = modelRepository;
        this.aiWeightsDirectory = Paths.get(aiWeightsDirectory).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void initializeCatalog() {
        syncFromWeightsDirectory();
        ensureFallbackModelExists();
    }

    @Override
    public List<ModelResponse> findModels(String nameKeyword) {
        List<ModelEntity> models = hasText(nameKeyword)
                ? modelRepository.findByNameContainingIgnoreCase(nameKeyword.trim())
                : modelRepository.findAll();

        return models.stream()
                .filter(this::isWeightAvailable)
                .sorted(Comparator.comparing(ModelEntity::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ModelResponse findModelById(Long modelId) {
        ModelEntity model = modelRepository.findById(modelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Model not found: " + modelId));

        if (!isWeightAvailable(model)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Model weight not found for model: " + model.getName()
            );
        }

        return toResponse(model);
    }

    private void syncFromWeightsDirectory() {
        if (!Files.isDirectory(aiWeightsDirectory)) {
            return;
        }

        try (Stream<Path> files = Files.list(aiWeightsDirectory)) {
            files.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(fileName -> fileName.toLowerCase().endsWith(".pt"))
                    .sorted(String::compareToIgnoreCase)
                    .forEach(this::upsertModelFromWeight);
        } catch (IOException ignored) {
            // Keep service functional even if auto discovery fails.
        }
    }

    private void upsertModelFromWeight(String fileName) {
        String modelName = deriveModelName(fileName);

        modelRepository.findByNameIgnoreCase(modelName)
                .ifPresentOrElse(existing -> {
                    if (!fileName.equals(existing.getPt())) {
                        existing.setPt(fileName);
                        modelRepository.save(existing);
                    }
                }, () -> modelRepository.save(ModelEntity.builder()
                        .name(modelName)
                        .pt(fileName)
                        .build()));
    }

    private void ensureFallbackModelExists() {
        if (modelRepository.count() > 0) {
            return;
        }

        modelRepository.save(ModelEntity.builder()
                .name("MoViNet-A0")
                .pt("movinet_a0_violence.pt")
                .build());
    }

    private String deriveModelName(String fileName) {
        String baseName = fileName.replaceFirst("\\.[^.]+$", "");
        Matcher matcher = MOVINET_VARIANT_PATTERN.matcher(baseName);
        if (matcher.find()) {
            return "MoViNet-" + matcher.group(1).toUpperCase();
        }
        return baseName.replace('_', '-');
    }

    private boolean isWeightAvailable(ModelEntity model) {
        if (model == null || !hasText(model.getPt())) {
            return false;
        }

        Path ptPath = Paths.get(model.getPt()).normalize();
        if (ptPath.isAbsolute()) {
            return Files.isRegularFile(ptPath);
        }

        return Files.isRegularFile(aiWeightsDirectory.resolve(ptPath).normalize());
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private ModelResponse toResponse(ModelEntity model) {
        return ModelResponse.builder()
                .id(model.getId())
                .name(model.getName())
                .pt(model.getPt())
                .build();
    }
}
