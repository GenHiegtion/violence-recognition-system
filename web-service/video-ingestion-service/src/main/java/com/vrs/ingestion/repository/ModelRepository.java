package com.vrs.ingestion.repository;

import com.vrs.ingestion.model.Model;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelRepository extends JpaRepository<Model, Long> {

    Optional<Model> findByNameIgnoreCase(String name);
}
