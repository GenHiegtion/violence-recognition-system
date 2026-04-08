package com.vrs.model.controller;

import com.vrs.model.dto.ModelResponse;
import com.vrs.model.service.ModelCatalogService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    private final ModelCatalogService modelCatalogService;

    public ModelController(ModelCatalogService modelCatalogService) {
        this.modelCatalogService = modelCatalogService;
    }

    @GetMapping
    public List<ModelResponse> listModels(@RequestParam(name = "name", required = false) String name) {
        return modelCatalogService.findModels(name);
    }

    @GetMapping("/{id}")
    public ModelResponse getById(@PathVariable("id") Long modelId) {
        return modelCatalogService.findModelById(modelId);
    }
}
