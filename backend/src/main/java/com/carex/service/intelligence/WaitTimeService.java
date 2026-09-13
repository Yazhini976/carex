package com.carex.service.intelligence;

import com.carex.entity.Doctor;

import java.time.LocalDate;

/**
 * Estimates wait time for a patient seeking an appointment with a specific doctor.
 *
 * <p>The initial implementation uses a deterministic baseline algorithm.
 * It is designed to be replaced by a machine-learning model without API changes.</p>
 *
 * <p><strong>This service does NOT claim clinical accuracy.</strong></p>
 */
public interface WaitTimeService {

    record WaitTimeEstimate(
            int predictedMinutes,
            String method,
            String disclaimer
    ) {}

    /**
     * Estimates the expected wait time for an appointment with a doctor on a given date.
     *
     * <p>Baseline algorithm:<br>
     * - Counts active (non-cancelled, non-completed) appointments before the target appointment<br>
     * - Multiplies by average consultation time (default 15 minutes)<br>
     * - Adds a buffer for delays (default +5 minutes per 3 appointments)</p>
     */
    WaitTimeEstimate estimate(Long doctorId, LocalDate date, Long appointmentId);
}
