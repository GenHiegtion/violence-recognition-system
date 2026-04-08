package com.vrs.recognition.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "recognitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecognitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String result;

    @Column(nullable = false, length = 500)
    private String file;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private Float confidenceScore;

    @Column
    private Long userId;

    @Column
    private Long vioPatternId;

    @Column(nullable = false)
    private Long modelId;

    @Column(nullable = false, length = 150)
    private String modelName;
}
