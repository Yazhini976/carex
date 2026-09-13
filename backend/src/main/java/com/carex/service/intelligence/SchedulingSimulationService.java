package com.carex.service.intelligence;

import com.carex.entity.Appointment;
import com.carex.entity.Doctor;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduling simulation service for "what-if" scenarios.
 *
 * <p><strong>IMPORTANT: This service is strictly read-only.
 * Simulations NEVER modify production appointment data.
 * An explicit admin-approved "apply simulation" workflow is required for any production change.</strong></p>
 */
public interface SchedulingSimulationService {

    enum ScenarioType {
        DOCTOR_UNAVAILABLE,
        CAPACITY_REDUCTION,
        EMERGENCY_SURGE
    }

    record SimulationResult(
            Long doctorId,
            LocalDate date,
            ScenarioType scenario,
            int affectedAppointmentCount,
            List<Appointment> affectedAppointments,
            List<Doctor> candidateAlternativeDoctors,
            String workloadImpactSummary,
            String redistributionSuggestion,
            boolean readOnly   // always true — simulations never modify production data
    ) {}

    /**
     * Simulates the impact of a doctor being unavailable on a specific date.
     * Returns affected appointments and candidate alternative doctors.
     * Does NOT modify any production data.
     */
    SimulationResult simulateDoctorUnavailable(Long doctorId, LocalDate date);

    /**
     * Simulates the impact of a general scenario.
     * Does NOT modify any production data.
     */
    SimulationResult simulate(Long doctorId, LocalDate date, ScenarioType scenario);
}
