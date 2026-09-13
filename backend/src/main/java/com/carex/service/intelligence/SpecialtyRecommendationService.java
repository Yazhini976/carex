package com.carex.service.intelligence;

import com.carex.entity.Specialty;

import java.util.List;
import java.util.Optional;

/**
 * Recommends a medical specialty based on a patient's self-described input.
 *
 * <p><strong>This service provides appointment navigation guidance only.
 * It is NOT a medical diagnosis engine.</strong></p>
 */
public interface SpecialtyRecommendationService {

    record RecommendationResult(
            Optional<Specialty> recommendedSpecialty,
            String reason,
            double confidence
    ) {}

    /**
     * Analyzes the patient's input and recommends an appropriate specialty.
     *
     * @param patientId   the patient's ID, or null for anonymous sessions
     * @param inputText   patient's self-described symptoms/concerns
     * @return recommendation result with specialty, reason, and confidence
     */
    RecommendationResult recommend(Long patientId, String inputText);

    /**
     * Returns keywords matched to specialties.
     * Used internally and for explainability.
     */
    List<String> getSupportedKeywords();
}
