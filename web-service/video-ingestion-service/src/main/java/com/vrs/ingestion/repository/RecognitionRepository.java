package com.vrs.ingestion.repository;

import com.vrs.ingestion.model.Recognition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecognitionRepository extends JpaRepository<Recognition, Long> {
}
