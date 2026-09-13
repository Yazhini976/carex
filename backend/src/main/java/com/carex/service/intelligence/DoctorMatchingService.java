package com.carex.service.intelligence;

import com.carex.entity.Doctor;
import com.carex.entity.Specialty;
import com.carex.entity.enums.AppointmentMode;

import java.util.List;

/**
 * Matches and ranks doctors based on specialty, mode, availability, and workload.
 * Produces an explainable recommendation score per doctor.
 */
public interface DoctorMatchingService {

    /**
     * Result model for a single doctor match.
     */
    record DoctorMatchResult(
            Doctor doctor,
            Specialty specialty,
            boolean specialtyMatch,
            boolean modeMatch,
            boolean hasAvailableSlot,
            Integer estimatedWaitMinutes,
            double score,
            String reasoning
    ) {}

    /**
     * Ranks available doctors for a given specialty and mode.
     *
     * <p>Scoring formula (documented):<br>
     * - specialtyMatch: +40 pts<br>
     * - modeMatch: +20 pts<br>
     * - hasAvailableSlot: +20 pts<br>
     * - experience bonus (capped): up to +10 pts<br>
     * - low estimated wait bonus: up to +10 pts<br>
     * Total: 0–100</p>
     */
    List<DoctorMatchResult> rankDoctors(Long specialtyId, AppointmentMode mode);
}
