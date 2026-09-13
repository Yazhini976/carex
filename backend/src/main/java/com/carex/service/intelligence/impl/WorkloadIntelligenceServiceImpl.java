package com.carex.service.intelligence.impl;

import com.carex.entity.Appointment;
import com.carex.entity.WorkloadSnapshot;
import com.carex.entity.enums.AppointmentStatus;
import com.carex.repository.AppointmentRepository;
import com.carex.service.DoctorService;
import com.carex.service.WorkloadService;
import com.carex.service.intelligence.WorkloadIntelligenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Rule-based workload intelligence analysis.
 *
 * <p>Scoring Formula (0–100):<br>
 * - Base Daily Volume: up to 40 pts<br>
 * - Active Queue Pressure (Booked/Confirmed): up to 35 pts<br>
 * - Peak Period Concentration (4 PM - 8 PM): up to 25 pts</p>
 *
 * <p>Load Levels:<br>
 * - LOW: score ≤ 30<br>
 * - MODERATE / MEDIUM: 30 < score ≤ 60<br>
 * - HIGH: 60 < score ≤ 80<br>
 * - CRITICAL: score > 80</p>
 */
@Service
@Transactional(readOnly = true)
public class WorkloadIntelligenceServiceImpl implements WorkloadIntelligenceService {

    private final WorkloadService workloadService;
    private final DoctorService doctorService;
    private final AppointmentRepository appointmentRepository;

    public WorkloadIntelligenceServiceImpl(WorkloadService workloadService,
                                            DoctorService doctorService,
                                            AppointmentRepository appointmentRepository) {
        this.workloadService = workloadService;
        this.doctorService = doctorService;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    @Transactional
    public WorkloadAnalysis analyzeDoctor(Long doctorId) {
        LocalDate today = LocalDate.now();
        List<Appointment> todayAppointments = appointmentRepository.findByDoctorIdAndSlotDate(doctorId, today);

        int totalCount = todayAppointments.size();
        int completedCount = (int) todayAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
        int cancelledCount = (int) todayAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count();
        int noShowCount = (int) todayAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.NO_SHOW).count();
        int activeQueue = (int) todayAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.BOOKED || a.getStatus() == AppointmentStatus.CONFIRMED).count();

        // Peak period count (4 PM - 8 PM)
        int peakCount = (int) todayAppointments.stream()
                .filter(a -> a.getSlot() != null && a.getSlot().getStartTime() != null)
                .filter(a -> {
                    LocalTime t = a.getSlot().getStartTime();
                    return t.isAfter(LocalTime.of(15, 59)) && t.isBefore(LocalTime.of(20, 1));
                })
                .count();

        // Compute normalized score 0-100
        double volumeScore = Math.min((totalCount / 15.0) * 40.0, 40.0);
        double queueScore = Math.min((activeQueue / 10.0) * 35.0, 35.0);
        double peakScore = totalCount > 0 ? ((double) peakCount / totalCount) * 25.0 : 0.0;

        double totalScore = Math.min(Math.round(volumeScore + queueScore + peakScore), 100.0);
        String loadLevel = classifyLoad(totalScore);

        String recommendation = buildDetailedRecommendation(loadLevel, totalCount, activeQueue, peakCount);

        // Also capture snapshot in database
        try {
            workloadService.captureSnapshot(doctorId);
        } catch (Exception ignored) {}

        return new WorkloadAnalysis(
                doctorId, totalScore, loadLevel, recommendation,
                totalCount, completedCount, cancelledCount, noShowCount
        );
    }

    @Override
    @Transactional
    public List<WorkloadAnalysis> analyzeAllDoctors() {
        return doctorService.getActiveDoctors().stream()
                .map(d -> analyzeDoctor(d.getId()))
                .toList();
    }

    @Override
    @Transactional
    public List<WorkloadAnalysis> findOverloadedDoctors() {
        return analyzeAllDoctors().stream()
                .filter(a -> "HIGH".equals(a.loadLevel()) || "CRITICAL".equals(a.loadLevel()))
                .toList();
    }

    private String classifyLoad(double score) {
        if (score <= 30) return "LOW";
        if (score <= 60) return "MEDIUM";
        if (score <= 80) return "HIGH";
        return "CRITICAL";
    }

    private String buildDetailedRecommendation(String loadLevel, int total, int activeQueue, int peakCount) {
        return switch (loadLevel) {
            case "LOW" -> "Provider has light operational load. Open capacity available for new or redistributed patient bookings.";
            case "MEDIUM" -> "Provider operating at balanced capacity (" + total + " total appointments, " + activeQueue + " in queue).";
            case "HIGH" -> "High workload detected (" + activeQueue + " pending in queue). Consider redistributing " +
                           Math.max(1, peakCount / 2) + " future slot(s) during peak afternoon hours.";
            case "CRITICAL" -> "CRITICAL capacity alert: " + total + " appointments scheduled with " + activeQueue +
                               " in active queue. Immediate patient flow rebalancing or secondary provider assistance recommended.";
            default -> "Operational status normal.";
        };
    }
}
