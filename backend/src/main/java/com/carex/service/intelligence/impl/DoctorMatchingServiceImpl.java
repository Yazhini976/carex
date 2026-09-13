package com.carex.service.intelligence.impl;

import com.carex.entity.Doctor;
import com.carex.entity.Slot;
import com.carex.entity.Specialty;
import com.carex.entity.enums.AppointmentMode;
import com.carex.service.DoctorService;
import com.carex.service.DoctorSpecialtyService;
import com.carex.service.SlotService;
import com.carex.service.intelligence.DoctorMatchingService;
import com.carex.service.intelligence.WaitTimeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Rule-based explainable doctor matching service producing CAREX Match Scores.
 *
 * <p>Scoring formula:<br>
 * - Specialty Match: +40 pts<br>
 * - Mode Match: +20 pts (slots matching requested mode)<br>
 * - Slot Availability: +20 pts (has open slots today)<br>
 * - Estimated Waiting Time: +10 pts (low wait <= 15m full points, scaling down)<br>
 * - Active Verified Doctor: +5 pts<br>
 * - Clinical Experience: +5 pts (min(experienceYears, 5))<br>
 * Total: 0–100</p>
 */
@Service
@Transactional(readOnly = true)
public class DoctorMatchingServiceImpl implements DoctorMatchingService {

    private final DoctorService doctorService;
    private final DoctorSpecialtyService doctorSpecialtyService;
    private final SlotService slotService;
    private final WaitTimeService waitTimeService;

    public DoctorMatchingServiceImpl(DoctorService doctorService,
                                      DoctorSpecialtyService doctorSpecialtyService,
                                      SlotService slotService,
                                      WaitTimeService waitTimeService) {
        this.doctorService = doctorService;
        this.doctorSpecialtyService = doctorSpecialtyService;
        this.slotService = slotService;
        this.waitTimeService = waitTimeService;
    }

    @Override
    public List<DoctorMatchResult> rankDoctors(Long specialtyId, AppointmentMode mode) {
        List<Doctor> activeDoctors = doctorService.getActiveDoctors();
        LocalDate today = LocalDate.now();
        List<DoctorMatchResult> results = new ArrayList<>();

        for (Doctor doctor : activeDoctors) {
            // 1. Specialty match check — FILTER OUT non-matching doctors when specialtyId is given
            boolean specialtyMatch = specialtyId == null || doctorSpecialtyService.isAssigned(doctor.getId(), specialtyId);
            if (specialtyId != null && !specialtyMatch) {
                continue; // skip doctors who don't belong to the requested specialty
            }

            Specialty specialty = specialtyId != null
                    ? doctorSpecialtyService.getSpecialtiesForDoctor(doctor.getId())
                        .stream()
                        .filter(s -> s.getId().equals(specialtyId))
                        .findFirst().orElse(null)
                    : null;

            // 2. Slot availability & mode match from slots
            List<Slot> availableToday = slotService.getAvailableSlotsForDoctorAndDate(doctor.getId(), today);
            boolean hasAvailableSlot = !availableToday.isEmpty();
            boolean modeMatch = mode == null || availableToday.stream().anyMatch(s -> s.getMode() == mode);

            // 3. Estimated wait time
            Integer estimatedWait = null;
            if (hasAvailableSlot) {
                try {
                    WaitTimeService.WaitTimeEstimate estimate = waitTimeService.estimate(doctor.getId(), today, null);
                    estimatedWait = estimate.predictedMinutes();
                } catch (Exception ignored) {}
            }

            int expYears = doctor.getExperienceYears() != null ? doctor.getExperienceYears() : 0;
            boolean isActive = doctor.getIsActive() != null && doctor.getIsActive();

            double score = computeScore(specialtyMatch, modeMatch, hasAvailableSlot,
                    isActive, expYears, estimatedWait);

            String reasoning = buildReasoning(specialtyMatch, modeMatch, hasAvailableSlot,
                    isActive, expYears, estimatedWait, score);

            results.add(new DoctorMatchResult(
                    doctor, specialty, specialtyMatch, modeMatch, hasAvailableSlot,
                    estimatedWait, score, reasoning
            ));
        }

        results.sort(Comparator.comparingDouble(DoctorMatchResult::score).reversed());
        return results;
    }

    private double computeScore(boolean specialtyMatch, boolean modeMatch, boolean hasSlot,
                                boolean isActive, int experienceYears, Integer estimatedWait) {
        double score = 0;

        // Specialty Match: 40%
        if (specialtyMatch) score += 40;

        // Requested Mode: 20%
        if (modeMatch) score += 20;

        // Availability: 20%
        if (hasSlot) score += 20;

        // Estimated Waiting Time: 10%
        if (estimatedWait != null) {
            double waitBonus = Math.max(0, 10 - (estimatedWait / 6.0));
            score += Math.min(waitBonus, 10);
        } else {
            score += 5; // default wait bonus
        }

        // Active Doctor Verification: 5%
        if (isActive) score += 5;

        // Experience: 5%
        score += Math.min(experienceYears, 5);

        return Math.min(score, 100);
    }

    private String buildReasoning(boolean specialtyMatch, boolean modeMatch, boolean hasSlot,
                                  boolean isActive, int experienceYears, Integer estimatedWait, double score) {
        List<String> reasons = new ArrayList<>();
        if (specialtyMatch) reasons.add("Specialty match (+40%)");
        if (modeMatch) reasons.add("Requested consultation mode available (+20%)");
        if (hasSlot) reasons.add("Available slots open today (+20%)");
        if (estimatedWait != null && estimatedWait <= 20) {
            reasons.add(String.format("Low estimated wait time (~%d min)", estimatedWait));
        }
        if (isActive) reasons.add("Active verified clinician (+5%)");
        if (experienceYears > 0) reasons.add(String.format("%d+ years clinical experience (+5%%)", experienceYears));

        return String.join(" • ", reasons) + String.format(" [Score: %.0f%%]", score);
    }
}
