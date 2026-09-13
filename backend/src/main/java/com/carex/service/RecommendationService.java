package com.carex.service;

import com.carex.entity.RecommendationLog;

import java.util.List;

/**
 * Service abstraction for CAREX specialty navigation/recommendation.
 *
 * <p>This service assists patients in finding the right specialty based on their
 * self-described symptoms or concerns. It is explicitly NOT a medical diagnosis engine.</p>
 */
public interface RecommendationService {
    RecommendationLog recordRecommendation(Long patientId, String inputText,
                                           Long recommendedSpecialtyId, String reason);
    List<RecommendationLog> getRecommendationsForPatient(Long patientId);
    List<RecommendationLog> getRecommendationsForSpecialty(Long specialtyId);
}
