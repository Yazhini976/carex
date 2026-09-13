package com.carex.repository;

import com.carex.entity.RecommendationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecommendationLogRepository extends JpaRepository<RecommendationLog, Long> {
    List<RecommendationLog> findByPatientId(Long patientId);
    List<RecommendationLog> findByRecommendedSpecialtyId(Long specialtyId);
}
