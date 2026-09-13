package com.carex.service;

import com.carex.entity.WaitTimePrediction;

import java.time.LocalDate;

public interface WaitTimePredictionService {
    /**
     * Calculates and persists a wait-time prediction for an appointment.
     *
     * <p>The initial implementation uses a deterministic baseline algorithm.
     * It can be replaced later with a machine-learning model without changing this contract.</p>
     *
     * @param appointmentId the appointment to predict for
     * @return the persisted prediction
     */
    WaitTimePrediction predictAndSave(Long appointmentId);

    WaitTimePrediction getPredictionForAppointment(Long appointmentId);

    /**
     * Records the actual wait time after the appointment is completed,
     * enabling future model accuracy analysis.
     */
    WaitTimePrediction recordActualWaitTime(Long appointmentId, Integer actualMinutes);
}
