package com.vrs.pattern.service;

import com.vrs.pattern.dto.request.VioPatternRequest;
import com.vrs.pattern.dto.response.VioPatternResponse;
import java.util.List;
import java.util.Map;

public interface VioPatternService {

    VioPatternResponse create(VioPatternRequest request);

    List<VioPatternResponse> list();

    Map<String, Double> activeThresholds();

    VioPatternResponse findById(long id);

    VioPatternResponse update(long id, VioPatternRequest request);

    void delete(long id);
}