package com.vrs.recognition.repository;

import com.vrs.recognition.model.RecognitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecognitionRepository extends JpaRepository<RecognitionEntity, Long> {
}
