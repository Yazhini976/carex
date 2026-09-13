package com.carex.service.intelligence.impl;

import com.carex.entity.Waitlist;
import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.WaitlistStatus;
import com.carex.repository.WaitlistRepository;
import com.carex.service.intelligence.SmartWaitlistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of SmartWaitlistService.
 *
 * <p>Scoring Breakdown (Total: 0–100):<br>
 * - Specialty Match: +40 pts<br>
 * - Preferred Doctor Match: +25 pts (+15 pts if open to any doctor)<br>
 * - Preferred Mode Match: +20 pts<br>
 * - Preferred Date Match: +10 pts (scales down if different date)<br>
 * - Waiting Duration Priority: +1 pt per day waiting (max +5 pts)</p>
 */
@Service
@Transactional(readOnly = true)
public class SmartWaitlistServiceImpl implements SmartWaitlistService {

    private final WaitlistRepository waitlistRepository;

    public SmartWaitlistServiceImpl(WaitlistRepository waitlistRepository) {
        this.waitlistRepository = waitlistRepository;
    }

    @Override
    public List<WaitlistRankResult> rankWaitingPatients(Long specialtyId, Long doctorId,
                                                        AppointmentMode mode, LocalDate date) {
        // Fetch all currently waiting entries for this specialty (or all WAITING)
        List<Waitlist> waitingEntries = waitlistRepository.findBySpecialtyIdAndStatus(specialtyId, WaitlistStatus.WAITING);
        if (waitingEntries.isEmpty()) {
            // Fallback: search all WAITING entries
            waitingEntries = waitlistRepository.findByStatus(WaitlistStatus.WAITING).stream()
                    .filter(w -> w.getSpecialty() != null && w.getSpecialty().getId().equals(specialtyId))
                    .toList();
        }

        List<ScoredEntry> scoredList = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Waitlist entry : waitingEntries) {
            double score = 0.0;
            List<String> factors = new ArrayList<>();

            // 1. Specialty match: 40 pts
            if (entry.getSpecialty() != null && entry.getSpecialty().getId().equals(specialtyId)) {
                score += 40.0;
                factors.add("Specialty match: +40 pts");
            }

            // 2. Preferred doctor match: 25 pts (or 15 for flexible)
            if (entry.getPreferredDoctor() != null) {
                if (doctorId != null && entry.getPreferredDoctor().getId().equals(doctorId)) {
                    score += 25.0;
                    factors.add("Specific preferred doctor match: +25 pts");
                }
            } else {
                score += 15.0;
                factors.add("Flexible doctor preference: +15 pts");
            }

            // 3. Preferred mode match: 20 pts
            if (entry.getPreferredMode() == null || mode == null || entry.getPreferredMode() == mode) {
                score += 20.0;
                factors.add("Consultation mode match (" + (mode != null ? mode : "ANY") + "): +20 pts");
            }

            // 4. Preferred date match: 10 pts
            if (entry.getPreferredDate() != null && date != null) {
                if (entry.getPreferredDate().isEqual(date)) {
                    score += 10.0;
                    factors.add("Exact preferred date match: +10 pts");
                } else if (Math.abs(ChronoUnit.DAYS.between(entry.getPreferredDate(), date)) <= 3) {
                    score += 5.0;
                    factors.add("Near-date preference (+/- 3 days): +5 pts");
                }
            } else {
                score += 5.0;
                factors.add("Open date preference: +5 pts");
            }

            // 5. Waiting duration priority: up to 5 pts
            if (entry.getCreatedAt() != null) {
                long daysWaiting = ChronoUnit.DAYS.between(entry.getCreatedAt().toLocalDate(), today);
                double waitBonus = Math.min(Math.max(daysWaiting, 0), 5.0);
                score += waitBonus;
                factors.add(String.format("Wait time priority (%d days): +%.1f pts", daysWaiting, waitBonus));
            } else {
                score += 1.0;
                factors.add("Queue priority baseline: +1 pt");
            }

            scoredList.add(new ScoredEntry(entry, Math.min(score, 100.0), factors));
        }

        // Sort descending by score
        scoredList.sort(Comparator.comparingDouble(ScoredEntry::score).reversed());

        List<WaitlistRankResult> results = new ArrayList<>();
        int rank = 1;
        for (ScoredEntry se : scoredList) {
            results.add(new WaitlistRankResult(se.entry, se.score, se.factors, rank++));
        }

        return results;
    }

    private record ScoredEntry(Waitlist entry, double score, List<String> factors) {}
}
