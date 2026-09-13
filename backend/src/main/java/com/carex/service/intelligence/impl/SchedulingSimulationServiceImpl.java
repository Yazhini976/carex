package com.carex.service.intelligence.impl;

import com.carex.entity.Appointment;
import com.carex.entity.Doctor;
import com.carex.entity.Specialty;
import com.carex.repository.AppointmentRepository;
import com.carex.service.DoctorService;
import com.carex.service.DoctorSpecialtyService;
import com.carex.service.intelligence.SchedulingSimulationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Read-only scheduling simulation service.
 *
 * <p><strong>CRITICAL: This service strictly NEVER modifies production data.
 * All results are read-only forecasts for clinic planning and capacity balancing.</strong></p>
 */
@Service
@Transactional(readOnly = true)
public class SchedulingSimulationServiceImpl implements SchedulingSimulationService {

    private static final Logger log = LoggerFactory.getLogger(SchedulingSimulationServiceImpl.class);

    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;
    private final DoctorSpecialtyService doctorSpecialtyService;

    public SchedulingSimulationServiceImpl(AppointmentRepository appointmentRepository,
                                            DoctorService doctorService,
                                            DoctorSpecialtyService doctorSpecialtyService) {
        this.appointmentRepository = appointmentRepository;
        this.doctorService = doctorService;
        this.doctorSpecialtyService = doctorSpecialtyService;
    }

    @Override
    public SimulationResult simulateDoctorUnavailable(Long doctorId, LocalDate date) {
        return simulate(doctorId, date, ScenarioType.DOCTOR_UNAVAILABLE);
    }

    @Override
    public SimulationResult simulate(Long doctorId, LocalDate date, ScenarioType scenario) {
        log.info("[SIMULATION][READ-ONLY] scenario={} doctorId={} date={}", scenario, doctorId, date);

        // Find active booked/confirmed appointments for target doctor on this date
        List<Appointment> allActive = appointmentRepository.findActiveByDoctorIdAndDate(doctorId, date);

        // Calculate affected appointments based on scenario
        List<Appointment> affected;
        if (scenario == ScenarioType.CAPACITY_REDUCTION) {
            // Half the appointments affected
            int affectedSize = (int) Math.ceil(allActive.size() / 2.0);
            affected = allActive.stream().limit(affectedSize).toList();
        } else {
            affected = allActive;
        }

        int affectedCount = scenario == ScenarioType.EMERGENCY_SURGE
                ? allActive.size() + (int) Math.ceil(Math.max(allActive.size(), 4) * 0.5)
                : affected.size();

        // Find alternative doctors with matching specialties
        List<Specialty> specialties = doctorSpecialtyService.getSpecialtiesForDoctor(doctorId);
        List<Long> specialtyIds = specialties.stream().map(Specialty::getId).toList();

        List<Doctor> alternatives = doctorService.getActiveDoctors()
                .stream()
                .filter(d -> !d.getId().equals(doctorId))
                .filter(d -> specialtyIds.isEmpty() || specialtyIds.stream().anyMatch(sId -> doctorSpecialtyService.isAssigned(d.getId(), sId)))
                .toList();

        String workloadImpact = buildWorkloadImpact(affectedCount, scenario);
        String redistribution = buildRedistribution(affectedCount, alternatives, scenario);

        return new SimulationResult(
                doctorId, date, scenario,
                affectedCount, affected,
                alternatives, workloadImpact, redistribution,
                true // always read-only
        );
    }

    private String buildWorkloadImpact(int affectedCount, ScenarioType scenario) {
        if (affectedCount == 0) return "No active appointments affected. Workload impact: NEGLIGIBLE.";
        if (affectedCount > 10) return "HIGH impact: " + affectedCount + " appointments affected under " + scenario + " scenario.";
        if (affectedCount > 4) return "MODERATE impact: " + affectedCount + " appointments affected under " + scenario + " scenario.";
        return "LOW impact: " + affectedCount + " appointments affected under " + scenario + " scenario.";
    }

    private String buildRedistribution(int affectedCount, List<Doctor> alternatives, ScenarioType scenario) {
        if (affectedCount == 0) return "No patient redistribution required.";
        if (alternatives.isEmpty()) {
            return "No matching alternative doctors available in the same department on this date. Date rescheduling required.";
        }

        int perDoctor = (int) Math.ceil((double) affectedCount / alternatives.size());
        StringBuilder sb = new StringBuilder();
        sb.append("Recommended Redistribution Strategy: Reassign ").append(affectedCount).append(" patient(s) across ")
          .append(alternatives.size()).append(" alternative provider(s) (~").append(perDoctor).append(" per doctor). ");

        for (int i = 0; i < Math.min(alternatives.size(), 3); i++) {
            Doctor doc = alternatives.get(i);
            String name = doc.getUser() != null ? doc.getUser().getName() : "Dr. #" + doc.getId();
            int count = (i == alternatives.size() - 1)
                    ? affectedCount - (perDoctor * (alternatives.size() - 1))
                    : perDoctor;
            if (count > 0) {
                sb.append("→ ").append(name).append(": ").append(count).append(" ");
            }
        }

        sb.append("[SIMULATION FORECAST ONLY — No changes have been applied]");
        return sb.toString();
    }
}
