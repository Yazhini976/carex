package com.carex.service.intelligence;

import com.carex.entity.Waitlist;
import com.carex.entity.enums.AppointmentMode;

import java.time.LocalDate;
import java.util.List;

/**
 * Smart waitlist ranking service.
 *
 * <p>When a slot opens up (e.g. following a cancellation), this service ranks
 * candidate waiting patients using an explainable multi-factor scoring formula.</p>
 */
public interface SmartWaitlistService {

    record WaitlistRankResult(
            Waitlist waitlistEntry,
            double matchScore,
            List<String> scoringFactors,
            int rank
    ) {}

    /**
     * Ranks all waiting patients for a specific specialty, optional doctor, mode, and date.
     *
     * <p>Scoring formula:<br>
     * - Specialty match: 40 pts<br>
     * - Preferred doctor match: 25 pts (15 pts if any doctor acceptable)<br>
     * - Preferred mode match: 20 pts<br>
     * - Preferred date match: 10 pts<br>
     * - Waiting duration priority: up to 5 pts<br>
     * Total: 0–100 pts</p>
     */
    List<WaitlistRankResult> rankWaitingPatients(Long specialtyId, Long doctorId,
                                                AppointmentMode mode, LocalDate date);
}
