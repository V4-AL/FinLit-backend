package com.example.demo.repository;

import com.example.demo.model.ModuleLife;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ModuleLifeRepository extends JpaRepository<ModuleLife, Long> {
    Optional<ModuleLife> findByUserIdAndModuleId(Long userId, Long moduleId);
}