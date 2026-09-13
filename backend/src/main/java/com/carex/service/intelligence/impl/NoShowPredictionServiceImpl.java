package com.carex.service.intelligence.impl;

import com.carex.entity.Appointment;
import com.carex.entity.enums.AppointmentStatus;
import com.carex.repository.AppointmentRepository;
import com.carex.service.intelligence.NoShowPredictionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Deterministic appointment attendance and no-show risk analyzer.
 *
 * <p><strong>Operational Policy:</strong><br>
 * Analyzes operational attendance history only (past completions, cancellations, no-shows).
 * Never uses protected or sensitive personal attributes.</p>
 */
@Service
@Transactional(readOnly = true)
public class NoShowPredictionServiceImpl implements NoShowPredictionService {

    private static final String CONFIDENCE = "DETERMINISTIC_OPERATIONAL_BASELINE";
    private static final String METHOD = "CAREX Attendance History Risk Model v1";

    private final AppointmentRepository appointmentRepository;

    public NoShowPredictionServiceImpl(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public NoShowPrediction predict(Long appointmentId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);

        if (appointmentOpt.isEmpty()) {
            return new NoShowPrediction(
                    appointmentId,
                    0.10,
                    "LOW",
                    CONFIDENCE,
                    METHOD,
                    List.of("Default operational baseline (appointment record not found)")
            );
        }

        Appointment appointment = appointmentOpt.get();
        Long patientId = appointment.getPatient() != null ? appointment.getPatient().getId() : null;

        if (patientId == null) {
            return new NoShowPrediction(
                    appointmentId,
                    0.10,
                    "LOW",
                    CONFIDENCE,
                    METHOD,
                    List.of("Baseline operational probability (new patient)")
            );
        }

        // Fetch patient's past appointment history
        List<Appointment> history = appointmentRepository.findByPatientId(patientId);

        long completedCount = history.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
        long noShowCount = history.stream().filter(a -> a.getStatus() == AppointmentStatus.NO_SHOW).count();
        long cancelledCount = history.stream().filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count();
        long totalPast = completedCount + noShowCount + cancelledCount;

        double baseProbability = 0.10; // 10% baseline
        List<String> basis = new ArrayList<>();

        if (totalPast == 0) {
            basis.add("First-time consultation profile (10% standard baseline)");
        } else {
            // Factor 1: Prior No-Shows
            if (noShowCount > 0) {
                double noShowPenalty = Math.min(noShowCount * 0.25, 0.50);
                baseProbability += noShowPenalty;
                basis.add(String.format("Patient has %d prior no-show(s) recorded (+%.0f%% risk)", noShowCount, noShowPenalty * 100));
            }

            // Factor 2: High Cancellation Rate
            if (cancelledCount > 2) {
                baseProbability += 0.15;
                basis.add(String.format("Frequent cancellations (%d past cancellations) (+15%% risk)", cancelledCount));
            }

            // Factor 3: High Completion Track Record
            if (completedCount >= 3 && noShowCount == 0) {
                baseProbability = Math.max(0.05, baseProbability - 0.05);
                basis.add(String.format("Strong past attendance history (%d completed appointments) (-5%% risk)", completedCount));
            }
        }

        // Factor 4: Confirmation state
        if (appointment.getStatus() == AppointmentStatus.BOOKED) {
            baseProbability += 0.05;
            basis.add("Appointment pending confirmation (+5% risk)");
        } else if (appointment.getStatus() == AppointmentStatus.CONFIRMED) {
            basis.add("Appointment confirmed by clinic");
        }

        double finalProbability = Math.min(Math.max(baseProbability, 0.02), 0.95);
        String riskLevel = finalProbability >= 0.50 ? "HIGH" : finalProbability >= 0.25 ? "MEDIUM" : "LOW";

        return new NoShowPrediction(
                appointmentId,
                Math.round(finalProbability * 100.0) / 100.0,
                riskLevel,
                CONFIDENCE,
                METHOD,
                basis
        );
    }
}
