package com.carex.notification;

import com.carex.entity.*;
import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.AppointmentStatus;
import com.carex.entity.enums.Role;
import com.carex.repository.AppointmentRepository;
import com.carex.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReminderSchedulerTest {

    private AppointmentRepository appointmentRepository;
    private NotificationRepository notificationRepository;
    private NotificationTemplateService templateService;
    private NotificationDispatcher dispatcher;
    private ReminderScheduler scheduler;

    private Appointment aptEligible;
    private Appointment aptTooFar;
    private Appointment aptCancelled;

    @BeforeEach
    void setUp() {
        appointmentRepository = mock(AppointmentRepository.class);
        notificationRepository = mock(NotificationRepository.class);
        templateService = mock(NotificationTemplateService.class);
        dispatcher = mock(NotificationDispatcher.class);

        scheduler = new ReminderScheduler(
                appointmentRepository,
                notificationRepository,
                templateService,
                dispatcher,
                30, // 30 minutes threshold
                true
        );

        User patientUser = new User();
        patientUser.setId(1L);
        patientUser.setName("Patient One");
        patientUser.setEmail("patient1@carex.com");
        patientUser.setRole(Role.PATIENT);

        User doctorUser = new User();
        doctorUser.setId(2L);
        doctorUser.setName("Dr. Smith");
        doctorUser.setEmail("smith@carex.com");
        doctorUser.setRole(Role.DOCTOR);

        Patient patient = new Patient(patientUser);
        patient.setId(10L);

        Doctor doctor = new Doctor(doctorUser, "DOC-1003");
        doctor.setId(20L);

        Specialty specialty = new Specialty("Cardiology");
        specialty.setId(1L);

        LocalDate testDate = LocalDate.of(2026, 9, 13);

        // Appointment 1: 10:25 AM (25 minutes away at 10:00 reference time -> eligible)
        Slot slot1 = new Slot(doctor, testDate, LocalTime.of(10, 25), LocalTime.of(10, 50), AppointmentMode.ONLINE);
        slot1.setId(101L);
        aptEligible = new Appointment(patient, doctor, specialty, slot1, AppointmentMode.ONLINE);
        aptEligible.setId(1001L);
        aptEligible.setStatus(AppointmentStatus.CONFIRMED);

        // Appointment 2: 10:45 AM (45 minutes away at 10:00 reference time -> not yet eligible)
        Slot slot2 = new Slot(doctor, testDate, LocalTime.of(10, 45), LocalTime.of(11, 10), AppointmentMode.ONLINE);
        slot2.setId(102L);
        aptTooFar = new Appointment(patient, doctor, specialty, slot2, AppointmentMode.ONLINE);
        aptTooFar.setId(1002L);
        aptTooFar.setStatus(AppointmentStatus.CONFIRMED);

        // Appointment 3: 10:20 AM (in window, but CANCELLED)
        Slot slot3 = new Slot(doctor, testDate, LocalTime.of(10, 20), LocalTime.of(10, 45), AppointmentMode.ONLINE);
        slot3.setId(103L);
        aptCancelled = new Appointment(patient, doctor, specialty, slot3, AppointmentMode.ONLINE);
        aptCancelled.setId(1003L);
        aptCancelled.setStatus(AppointmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should dispatch reminder for eligible upcoming appointment within 30-minute window")
    void testProcessUpcomingReminders_Eligible() {
        LocalDateTime referenceTime = LocalDateTime.of(2026, 9, 13, 10, 0);

        when(appointmentRepository.findBySlotDate(LocalDate.of(2026, 9, 13)))
                .thenReturn(List.of(aptEligible, aptTooFar, aptCancelled));
        when(notificationRepository.existsByAppointmentIdAndType(1001L, "APPOINTMENT_REMINDER"))
                .thenReturn(false);

        NotificationEvent mockEvent = NotificationEvent.builder()
                .recipientUserId(1L)
                .notificationType(NotificationType.APPOINTMENT_REMINDER)
                .build();
        when(templateService.buildAppointmentReminderEvent(aptEligible)).thenReturn(mockEvent);

        int dispatched = scheduler.processUpcomingReminders(referenceTime);

        assertEquals(1, dispatched);
        verify(templateService, times(1)).buildAppointmentReminderEvent(aptEligible);
        verify(dispatcher, times(1)).dispatch(mockEvent);
    }

    @Test
    @DisplayName("Should prevent duplicate reminders when APPOINTMENT_REMINDER is already recorded")
    void testProcessUpcomingReminders_PreventDuplicates() {
        LocalDateTime referenceTime = LocalDateTime.of(2026, 9, 13, 10, 0);

        when(appointmentRepository.findBySlotDate(LocalDate.of(2026, 9, 13)))
                .thenReturn(List.of(aptEligible));
        // Already reminded
        when(notificationRepository.existsByAppointmentIdAndType(1001L, "APPOINTMENT_REMINDER"))
                .thenReturn(true);

        int dispatched = scheduler.processUpcomingReminders(referenceTime);

        assertEquals(0, dispatched);
        verify(dispatcher, never()).dispatch(any());
    }

    @Test
    @DisplayName("Should skip appointments that are outside the lookahead threshold or cancelled")
    void testProcessUpcomingReminders_SkipOutsideThreshold() {
        LocalDateTime referenceTime = LocalDateTime.of(2026, 9, 13, 10, 0);

        when(appointmentRepository.findBySlotDate(LocalDate.of(2026, 9, 13)))
                .thenReturn(List.of(aptTooFar, aptCancelled));

        int dispatched = scheduler.processUpcomingReminders(referenceTime);

        assertEquals(0, dispatched);
        verify(dispatcher, never()).dispatch(any());
    }
}
