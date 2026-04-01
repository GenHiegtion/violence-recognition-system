package com.vrs.pattern.service;

import com.vrs.pattern.model.PatternRequest;
import com.vrs.pattern.model.PatternResponse;
import com.vrs.pattern.model.VioPattern;
import com.vrs.pattern.repository.VioPatternRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PatternManagementService {

    private final VioPatternRepository repository;

    public PatternManagementService(VioPatternRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PatternResponse create(PatternRequest request) {
        if (repository.existsByNameIgnoreCase(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pattern name already exists");
        }

        VioPattern pattern = new VioPattern();
        pattern.setName(request.name().trim());
        pattern.setSevLevel(request.sevLevel());
        pattern.setThreshold(request.threshold());
        pattern.setFile(request.file());

        return toResponse(repository.save(pattern));
    }

    @Transactional(readOnly = true)
    public List<PatternResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PatternResponse findById(long id) {
        VioPattern pattern = findEntityById(id);
        return toResponse(pattern);
    }

    @Transactional
    public PatternResponse update(long id, PatternRequest request) {
        VioPattern pattern = findEntityById(id);

        if (!pattern.getName().equalsIgnoreCase(request.name())
                && repository.existsByNameIgnoreCase(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pattern name already exists");
        }

        pattern.setName(request.name().trim());
        pattern.setSevLevel(request.sevLevel());
        pattern.setThreshold(request.threshold());
        pattern.setFile(request.file());

        return toResponse(repository.save(pattern));
    }

    @Transactional
    public void delete(long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pattern not found");
        }
        repository.deleteById(id);
    }

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

    private PatternResponse toResponse(VioPattern pattern) {
        return new PatternResponse(
                pattern.getId(),
                pattern.getName(),
                pattern.getSevLevel(),
                pattern.getThreshold(),
                pattern.getFile()
        );
    }
}
