package com.carex.service.intelligence;

import java.util.List;

/**
 * Predicts the operational risk probability of patient non-attendance (no-show) for an appointment.
 *
 * <p><strong>CRITICAL: This is strictly for operational reminder prioritization.
 * It is NOT a medical risk assessment and does NOT use any protected or sensitive personal attributes.</strong></p>
 */
public interface NoShowPredictionService {

    record NoShowPrediction(
            Long appointmentId,
            double noShowProbability,
            String riskLevel, // LOW / MEDIUM / HIGH
            String confidence,
            String method,
            List<String> basis
    ) {}

    /**
     * Analyzes appointment attendance factors and returns the predicted no-show risk.
     *
     * @param appointmentId appointment ID to analyze
     * @return explainable no-show prediction
     */
    NoShowPrediction predict(Long appointmentId);
}
