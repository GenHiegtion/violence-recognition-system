package com.vrs.pattern.service.impl;

import com.vrs.pattern.dto.request.VioPatternRequest;
import com.vrs.pattern.dto.response.VioPatternResponse;
import com.vrs.pattern.model.VioPattern;
import com.vrs.pattern.repository.VioPatternRepository;
import com.vrs.pattern.service.VioPatternService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VioPatternServiceImpl implements VioPatternService {

    private final VioPatternRepository repository;

    public VioPatternServiceImpl(VioPatternRepository repository) {
        this.repository = repository;
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