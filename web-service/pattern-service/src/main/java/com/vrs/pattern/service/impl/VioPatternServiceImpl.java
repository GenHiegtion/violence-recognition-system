package com.vrs.pattern.service.impl;

import com.vrs.pattern.dto.request.VioPatternRequest;
import com.vrs.pattern.dto.response.VioPatternResponse;
import com.vrs.pattern.model.VioPattern;
import com.vrs.pattern.repository.VioPatternRepository;
import com.vrs.pattern.service.VioPatternService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VioPatternServiceImpl implements VioPatternService {

    private final VioPatternRepository repository;
    private final Path uploadRoot;

    public VioPatternServiceImpl(
            VioPatternRepository repository,
            @Value("${app.upload-dir:${user.dir}/../../uploads/patterns}") String uploadDir
    ) {
        this.repository = repository;
        this.uploadRoot = Paths.get(uploadDir).normalize().toAbsolutePath();
    }

    @Override
    @Transactional
    public VioPatternResponse create(VioPatternRequest request) {
        String normalizedName = request.getName().trim();
        if (repository.existsByNameIgnoreCase(normalizedName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pattern name already exists");
        }

        VioPattern pattern = new VioPattern();
        pattern.setName(normalizedName);
        pattern.setSevLevel(request.getSevLevel());
        pattern.setThreshold(request.getThreshold());
        pattern.setFile(request.getFile());

        return toResponse(repository.save(pattern));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VioPatternResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VioPatternResponse findById(long id) {
        return toResponse(findEntityById(id));
    }

    @Override
    @Transactional
    public VioPatternResponse update(long id, VioPatternRequest request) {
        VioPattern pattern = findEntityById(id);
        String normalizedName = request.getName().trim();

        if (!pattern.getName().equalsIgnoreCase(normalizedName)
                && repository.existsByNameIgnoreCase(normalizedName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pattern name already exists");
        }

        pattern.setName(normalizedName);
        pattern.setSevLevel(request.getSevLevel());
        pattern.setThreshold(request.getThreshold());
        pattern.setFile(request.getFile());

        return toResponse(repository.save(pattern));
    }

    @Override
    @Transactional
    public void delete(long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pattern not found");
        }
        repository.deleteById(id);
    }

    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name");
        }

        String safeName = Paths.get(originalName).getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_");
        String storedName = UUID.randomUUID() + "_" + safeName;

        try {
            Files.createDirectories(uploadRoot);
            Path target = uploadRoot.resolve(storedName).normalize();
            if (!target.startsWith(uploadRoot)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "uploads/patterns/" + storedName;
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Double> activeThresholds() {
        Map<String, Double> thresholds = new LinkedHashMap<>();
        for (VioPattern pattern : repository.findAll()) {
            thresholds.put(pattern.getName().toLowerCase(), pattern.getThreshold());
        }
        return thresholds;
    }

    private VioPattern findEntityById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pattern not found"));
    }

    private VioPatternResponse toResponse(VioPattern pattern) {
        return VioPatternResponse.builder()
                .id(pattern.getId())
                .name(pattern.getName())
                .sevLevel(pattern.getSevLevel())
                .threshold(pattern.getThreshold())
                .file(pattern.getFile())
                .build();
    }
}