package com.carex.service;

import com.carex.entity.Waitlist;
import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.WaitlistStatus;

import java.time.LocalDate;
import java.util.List;

public interface WaitlistService {
    Waitlist joinWaitlist(Long patientId, Long specialtyId, Long preferredDoctorId,
                          LocalDate preferredDate, AppointmentMode preferredMode);
    Waitlist getWaitlistEntryById(Long id);
    List<Waitlist> getPatientWaitlist(Long patientId);
    List<Waitlist> findMatchingWaiting(Long specialtyId, Long doctorId,
                                       AppointmentMode mode, LocalDate date);
    Waitlist cancelEntry(Long id);
    Waitlist markOffered(Long id);
    Waitlist markFulfilled(Long id);
    Waitlist expireEntry(Long id);
    void triggerSmartWaitlist(Long specialtyId, Long doctorId, AppointmentMode mode, LocalDate date);
}
