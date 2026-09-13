package com.carex.repository;

import com.carex.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {
    Optional<Specialty> findByName(String name);
    boolean existsByName(String name);
    List<Specialty> findByIsActive(Boolean isActive);
}
