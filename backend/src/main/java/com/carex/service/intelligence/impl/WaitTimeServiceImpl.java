package com.carex.service.intelligence.impl;

import com.carex.entity.Appointment;
import com.carex.entity.enums.AppointmentStatus;
import com.carex.repository.AppointmentRepository;
import com.carex.service.intelligence.WaitTimeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Deterministic wait-time estimation service.
 *
 * <p>Algorithm:<br>
 * 1. Count active appointments for the doctor before the target time.<br>
 * 2. Multiply by AVG_CONSULT = 15 minutes.<br>
 * 3. Add DELAY_BUFFER (5 min per 3 appointments).<br>
 * Result capped at 120 minutes.</p>
 *
 * <p>This service does NOT claim clinical accuracy.</p>
 */
@Service
@Transactional(readOnly = true)
public class WaitTimeServiceImpl implements WaitTimeService {

    private static final int AVG_CONSULT = 15;
    private static final int DELAY_BUFFER = 5;
    private static final int MAX_WAIT = 120;
    private static final String METHOD = "DETERMINISTIC_BASELINE_v1";
    private static final String DISCLAIMER =
            "This is an estimated queue position calculation. It does not guarantee clinical timing accuracy.";

    private final AppointmentRepository appointmentRepository;

    public WaitTimeServiceImpl(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public WaitTimeEstimate estimate(Long doctorId, LocalDate date, Long appointmentId) {
        List<Appointment> active = appointmentRepository.findActiveByDoctorIdAndDate(doctorId, date);

        long count = active.stream()
                .filter(a -> appointmentId == null || !a.getId().equals(appointmentId))
                .count();

        int base = (int) (count * AVG_CONSULT);
        int buffer = (int) (count / 3) * DELAY_BUFFER;
        int total = Math.min(base + buffer, MAX_WAIT);

        return new WaitTimeEstimate(Math.max(total, 0), METHOD, DISCLAIMER);
    }
}
