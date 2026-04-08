package com.vrs.model.repository;

import com.vrs.model.model.ModelEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelRepository extends JpaRepository<ModelEntity, Long> {

    Optional<ModelEntity> findByNameIgnoreCase(String name);

    List<ModelEntity> findByNameContainingIgnoreCase(String name);
}
