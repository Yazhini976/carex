package com.carex.service.impl;

import com.carex.entity.Doctor;
import com.carex.entity.DoctorAvailability;
import com.carex.entity.enums.AppointmentMode;
import com.carex.exception.BusinessRuleException;
import com.carex.exception.ResourceNotFoundException;
import com.carex.repository.DoctorAvailabilityRepository;
import com.carex.service.DoctorAvailabilityService;
import com.carex.service.DoctorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DoctorAvailabilityServiceImpl implements DoctorAvailabilityService {

    private static final Logger log = LoggerFactory.getLogger(DoctorAvailabilityServiceImpl.class);

    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorService doctorService;

    public DoctorAvailabilityServiceImpl(DoctorAvailabilityRepository availabilityRepository,
                                          DoctorService doctorService) {
        this.availabilityRepository = availabilityRepository;
        this.doctorService = doctorService;
    }

    @Override
    @Transactional
    public DoctorAvailability createAvailability(Long doctorId, Integer dayOfWeek,
                                                  LocalTime startTime, LocalTime endTime, AppointmentMode mode) {
        validateDayOfWeek(dayOfWeek);
        validateTimeRange(startTime, endTime);
        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (!Boolean.TRUE.equals(doctor.getIsActive())) {
            throw new BusinessRuleException("Doctor " + doctorId + " is not active and cannot have availability set");
        }
        DoctorAvailability availability = new DoctorAvailability(doctor, dayOfWeek, startTime, endTime, mode);
        DoctorAvailability saved = availabilityRepository.save(availability);
        log.info("Created availability id={} for doctor={} day={} mode={}", saved.getId(), doctorId, dayOfWeek, mode);
        return saved;
    }

    @Override
    public DoctorAvailability getAvailabilityById(Long id) {
        return availabilityRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("DoctorAvailability", id));
    }

    @Override
    public List<DoctorAvailability> getAvailabilityForDoctor(Long doctorId) {
        return availabilityRepository.findByDoctorId(doctorId);
    }

    @Override
    public List<DoctorAvailability> getActiveAvailabilityForDoctor(Long doctorId) {
        return availabilityRepository.findByDoctorIdAndIsActive(doctorId, true);
    }

    @Override
    @Transactional
    public DoctorAvailability updateAvailability(Long id, Integer dayOfWeek,
                                                  LocalTime startTime, LocalTime endTime, AppointmentMode mode) {
        DoctorAvailability availability = getAvailabilityById(id);
        if (dayOfWeek != null) {
            validateDayOfWeek(dayOfWeek);
            availability.setDayOfWeek(dayOfWeek);
        }
        LocalTime effectiveStart = startTime != null ? startTime : availability.getStartTime();
        LocalTime effectiveEnd = endTime != null ? endTime : availability.getEndTime();
        validateTimeRange(effectiveStart, effectiveEnd);
        if (startTime != null) availability.setStartTime(startTime);
        if (endTime != null) availability.setEndTime(endTime);
        if (mode != null) availability.setMode(mode);
        return availabilityRepository.save(availability);
    }

    @Override
    @Transactional
    public DoctorAvailability deactivateAvailability(Long id) {
        DoctorAvailability availability = getAvailabilityById(id);
        availability.setIsActive(false);
        return availabilityRepository.save(availability);
    }

    @Override
    @Transactional
    public void deleteAvailability(Long id) {
        getAvailabilityById(id);
        availabilityRepository.deleteById(id);
        log.info("Deleted availability id={}", id);
    }

    // -------------------------------------------------------------------------
    // Private validation helpers
    // -------------------------------------------------------------------------

    private void validateDayOfWeek(Integer dayOfWeek) {
        if (dayOfWeek < 0 || dayOfWeek > 6) {
            throw new BusinessRuleException(
                    "dayOfWeek must be 0–6 (0=Sunday, 6=Saturday), got: " + dayOfWeek);
        }
    }

    private void validateTimeRange(LocalTime start, LocalTime end) {
        if (!start.isBefore(end)) {
            throw new BusinessRuleException(
                    "startTime (" + start + ") must be before endTime (" + end + ")");
        }
    }
}
