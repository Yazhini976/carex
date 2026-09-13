package com.carex.service;

import com.carex.entity.DoctorAvailability;
import com.carex.entity.enums.AppointmentMode;

import java.time.LocalTime;
import java.util.List;

public interface DoctorAvailabilityService {
    DoctorAvailability createAvailability(Long doctorId, Integer dayOfWeek,
                                          LocalTime startTime, LocalTime endTime, AppointmentMode mode);
    DoctorAvailability getAvailabilityById(Long id);
    List<DoctorAvailability> getAvailabilityForDoctor(Long doctorId);
    List<DoctorAvailability> getActiveAvailabilityForDoctor(Long doctorId);
    DoctorAvailability updateAvailability(Long id, Integer dayOfWeek,
                                          LocalTime startTime, LocalTime endTime, AppointmentMode mode);
    DoctorAvailability deactivateAvailability(Long id);
    void deleteAvailability(Long id);
}
