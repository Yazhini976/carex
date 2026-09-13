package com.carex.service.intelligence;

import com.carex.entity.WorkloadSnapshot;

import java.util.List;

/**
 * Analyzes doctor workload patterns and provides operational insights.
 */
public interface WorkloadIntelligenceService {

    record WorkloadAnalysis(
            Long doctorId,
            double currentScore,
            String loadLevel,    // LOW / MODERATE / HIGH / CRITICAL
            String recommendation,
            int appointmentCount,
            int completedCount,
            int cancelledCount,
            int noShowCount
    ) {}

    WorkloadAnalysis analyzeDoctor(Long doctorId);

    List<WorkloadAnalysis> analyzeAllDoctors();

    /**
     * Identifies doctors at risk of overload (score > 80).
     */
    List<WorkloadAnalysis> findOverloadedDoctors();
}
