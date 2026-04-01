package com.vrs.pattern.controller;

import com.vrs.pattern.model.PatternRequest;
import com.vrs.pattern.model.PatternResponse;
import com.vrs.pattern.service.PatternManagementService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patterns")
public class PatternController {

    private final PatternManagementService service;

    public PatternController(PatternManagementService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatternResponse create(@Valid @RequestBody PatternRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<PatternResponse> list() {
        return service.list();
    }

    @GetMapping("/thresholds")
    public Map<String, Double> activeThresholds() {
        return service.activeThresholds();
    }

    @GetMapping("/{id}")
    public PatternResponse findById(@PathVariable long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public PatternResponse update(@PathVariable long id, @Valid @RequestBody PatternRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
