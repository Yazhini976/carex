package com.carex.service;

import com.carex.entity.Appointment;
import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.AppointmentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AppointmentService {
    Appointment bookAppointment(Long patientId, Long doctorId, Long specialtyId, Long slotId,
                                AppointmentMode mode, String notes);
    Appointment getAppointmentById(Long id);
    List<Appointment> getAppointmentsByPatient(Long patientId);
    List<Appointment> getAppointmentsByDoctor(Long doctorId);
    List<Appointment> getAppointmentsByStatus(AppointmentStatus status);
    List<Appointment> getAppointmentsByDoctorAndDate(Long doctorId, LocalDate date);
    Appointment confirmAppointment(Long appointmentId, Long changedByUserId);
    Appointment completeAppointment(Long appointmentId, Long changedByUserId);
    Appointment cancelAppointment(Long appointmentId, Long changedByUserId);
    Appointment markNoShow(Long appointmentId, Long changedByUserId);
    Map<String, Object> getDailySummary(LocalDate date);
}
