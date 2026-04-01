package com.vrs.pattern.repository;

import com.vrs.pattern.model.VioPattern;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VioPatternRepository extends JpaRepository<VioPattern, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<VioPattern> findByNameIgnoreCase(String name);
}
