package com.carex.service.intelligence;

import com.carex.entity.Appointment;
import com.carex.entity.Doctor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Disruption recovery service for doctor unavailability.
 *
 * <p><strong>CRITICAL: This service is strictly read-only.
 * Recommendations are prepared for administrator review and approval.
 * No appointments or slot statuses are modified automatically.</strong></p>
 */
public interface DisruptionRecoveryService {

    record PatientRecoveryPlan(
            Appointment appointment,
            Doctor suggestedDoctor,
            LocalTime suggestedTime,
            String specialtyName,
            String mode,
            String rationale
    ) {}

    record AlternativeCapacity(
            Doctor doctor,
            String primarySpecialty,
            int availableCapacity
    ) {}

    record DisruptionRecoveryResult(
            Long unavailableDoctorId,
            String unavailableDoctorName,
            LocalDate date,
            int affectedAppointmentCount,
            List<PatientRecoveryPlan> patientPlans,
            List<AlternativeCapacity> alternativeDoctors,
            String summaryRecommendation,
            boolean readOnly
    ) {}

    /**
     * Evaluates disruption impact when a doctor is unavailable and generates alternative recommendations.
     *
     * @param doctorId unavailable doctor ID
     * @param date     date of unavailability
     * @return recovery plan recommendations (read-only)
     */
    DisruptionRecoveryResult evaluateRecovery(Long doctorId, LocalDate date);
}
