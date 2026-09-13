package com.carex.notification;

import com.carex.entity.Appointment;
import com.carex.entity.Doctor;
import com.carex.entity.Specialty;
import com.carex.entity.enums.AppointmentMode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Service for rendering structured, mode-specific notification subjects and templates.
 */
public interface NotificationTemplateService {

    NotificationEvent buildAppointmentBookedEvent(Appointment appointment);

    NotificationEvent buildAppointmentConfirmedEvent(Appointment appointment);

    NotificationEvent buildAppointmentCancelledEvent(Appointment appointment);

    NotificationEvent buildAppointmentCompletedEvent(Appointment appointment);

    NotificationEvent buildAppointmentNoShowEvent(Appointment appointment);

    NotificationEvent buildAppointmentReminderEvent(Appointment appointment);

    NotificationEvent buildWaitlistSlotAvailableEvent(Long userId, String userEmail, String userName,
                                                     Doctor doctor, Specialty specialty,
                                                     LocalDate date, LocalTime startTime, AppointmentMode mode);

    NotificationEvent buildDoctorUnavailableEvent(Appointment appointment, String doctorName);

    NotificationEvent buildSchedulingAlertEvent(Long userId, String userEmail, String userName,
                                               String subject, String message, NotificationPriority priority);
}
