package com.vrs.model.service;

import com.vrs.model.dto.ModelResponse;
import java.util.List;

public interface ModelCatalogService {

    List<ModelResponse> findModels(String nameKeyword);

    ModelResponse findModelById(Long modelId);
}
