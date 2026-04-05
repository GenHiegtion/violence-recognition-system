package com.vrs.pattern.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VioPatternResponse {

    private Long id;
    private String name;
    private Integer sevLevel;
    private Double threshold;
    private String file;
}