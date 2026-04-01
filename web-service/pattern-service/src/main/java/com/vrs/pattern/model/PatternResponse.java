package com.vrs.pattern.model;

public record PatternResponse(
        Long id,
        String name,
        Integer sevLevel,
        Double threshold,
        String file
) {
}
