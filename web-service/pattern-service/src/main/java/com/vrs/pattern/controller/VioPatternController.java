package com.vrs.pattern.controller;

import com.vrs.pattern.dto.request.VioPatternRequest;
import com.vrs.pattern.dto.response.VioPatternResponse;
import com.vrs.pattern.service.VioPatternService;
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
public class VioPatternController {

    private final VioPatternService vioPatternService;

    public VioPatternController(VioPatternService vioPatternService) {
        this.vioPatternService = vioPatternService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VioPatternResponse create(@Valid @RequestBody VioPatternRequest request) {
        return vioPatternService.create(request);
    }

    @GetMapping
    public List<VioPatternResponse> list() {
        return vioPatternService.list();
    }

    @GetMapping("/thresholds")
    public Map<String, Double> activeThresholds() {
        return vioPatternService.activeThresholds();
    }

    @GetMapping("/{id}")
    public VioPatternResponse findById(@PathVariable long id) {
        return vioPatternService.findById(id);
    }

    @PutMapping("/{id}")
    public VioPatternResponse update(@PathVariable long id, @Valid @RequestBody VioPatternRequest request) {
        return vioPatternService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        vioPatternService.delete(id);
        return ResponseEntity.noContent().build();
    }
}