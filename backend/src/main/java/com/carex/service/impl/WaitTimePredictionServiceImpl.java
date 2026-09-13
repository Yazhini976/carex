package com.carex.service.impl;

import com.carex.entity.Appointment;
import com.carex.entity.WaitTimePrediction;
import com.carex.entity.enums.AppointmentStatus;
import com.carex.exception.ResourceNotFoundException;
import com.carex.repository.AppointmentRepository;
import com.carex.repository.WaitTimePredictionRepository;
import com.carex.service.AppointmentService;
import com.carex.service.WaitTimePredictionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Deterministic baseline wait-time prediction service.
 *
 * <p>Algorithm (replaceable by ML model later):<br>
 * 1. Count appointments for the same doctor on the same date that are scheduled before the target slot.<br>
 * 2. Multiply by AVG_CONSULT_MINUTES (15).<br>
 * 3. Add DELAY_BUFFER_MINUTES (5) for every 3 appointments.<br>
 * Result is capped at MAX_WAIT_MINUTES (120).</p>
 *
 * <p><strong>This service does NOT claim clinical accuracy.</strong></p>
 */
@Service
@Transactional(readOnly = true)
public class WaitTimePredictionServiceImpl implements WaitTimePredictionService {

    private static final Logger log = LoggerFactory.getLogger(WaitTimePredictionServiceImpl.class);

    /** Average consultation time in minutes (configurable in future). */
    private static final int AVG_CONSULT_MINUTES = 15;
    /** Cumulative delay buffer added per every 3 appointments. */
    private static final int DELAY_BUFFER_MINUTES = 5;
    /** Maximum predicted wait cap. */
    private static final int MAX_WAIT_MINUTES = 120;

    private final WaitTimePredictionRepository predictionRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    public WaitTimePredictionServiceImpl(WaitTimePredictionRepository predictionRepository,
                                          AppointmentRepository appointmentRepository,
                                          @Lazy AppointmentService appointmentService) {
        this.predictionRepository = predictionRepository;
        this.appointmentRepository = appointmentRepository;
        this.appointmentService = appointmentService;
    }

    @Override
    @Transactional
    public WaitTimePrediction predictAndSave(Long appointmentId) {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        int predicted = calculateWait(appointment);

        WaitTimePrediction prediction = new WaitTimePrediction(appointment, predicted);
        WaitTimePrediction saved = predictionRepository.save(prediction);
        log.info("Predicted wait for appointment {} = {} minutes", appointmentId, predicted);
        return saved;
    }

    @Override
    public WaitTimePrediction getPredictionForAppointment(Long appointmentId) {
        return predictionRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "WaitTimePrediction not found for appointment: " + appointmentId));
    }

    @Override
    @Transactional
    public WaitTimePrediction recordActualWaitTime(Long appointmentId, Integer actualMinutes) {
        WaitTimePrediction prediction = getPredictionForAppointment(appointmentId);
        prediction.setActualMinutes(actualMinutes);
        return predictionRepository.save(prediction);
    }

    // -------------------------------------------------------------------------
    // Deterministic baseline algorithm
    // -------------------------------------------------------------------------

    private int calculateWait(Appointment target) {
        Long doctorId = target.getDoctor().getId();
        LocalDate date = target.getSlot().getSlotDate();

        // All active appointments for this doctor on this date
        List<Appointment> dayAppointments = appointmentRepository.findActiveByDoctorIdAndDate(doctorId, date);

        // Count appointments whose slot starts before the target appointment's slot
        long appointmentsBefore = dayAppointments.stream()
                .filter(a -> !a.getId().equals(target.getId()))
                .filter(a -> a.getSlot().getStartTime().isBefore(target.getSlot().getStartTime()))
                .count();

        int baseWait = (int) (appointmentsBefore * AVG_CONSULT_MINUTES);
        int buffer = (int) (appointmentsBefore / 3) * DELAY_BUFFER_MINUTES;
        int total = Math.min(baseWait + buffer, MAX_WAIT_MINUTES);

        return Math.max(total, 0);
    }
}
