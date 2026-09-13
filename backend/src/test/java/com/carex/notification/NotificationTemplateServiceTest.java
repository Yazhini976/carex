package com.carex.notification;

import com.carex.entity.*;
import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.AppointmentStatus;
import com.carex.entity.enums.Role;
import com.carex.notification.impl.NotificationTemplateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTemplateServiceTest {

    private NotificationTemplateService templateService;
    private Appointment appointment;
    private User patientUser;
    private User doctorUser;
    private Patient patient;
    private Doctor doctor;
    private Specialty specialty;
    private Slot slot;

    @BeforeEach
    void setUp() {
        templateService = new NotificationTemplateServiceImpl();

        patientUser = new User();
        patientUser.setId(10L);
        patientUser.setName("Alice Patient");
        patientUser.setEmail("alice@carex.com");
        patientUser.setRole(Role.PATIENT);

        doctorUser = new User();
        doctorUser.setId(20L);
        doctorUser.setName("Priya Sharma");
        doctorUser.setEmail("priya@carex.com");
        doctorUser.setRole(Role.DOCTOR);

        patient = new Patient(patientUser);
        patient.setId(100L);

        doctor = new Doctor(doctorUser, "DOC-1002");
        doctor.setId(200L);

        specialty = new Specialty("Dermatology");
        specialty.setId(1L);

        slot = new Slot(doctor, LocalDate.of(2026, 9, 20), LocalTime.of(10, 30), LocalTime.of(11, 0), AppointmentMode.ONLINE);
        slot.setId(500L);

        appointment = new Appointment(patient, doctor, specialty, slot, AppointmentMode.ONLINE);
        appointment.setId(1001L);
    }

    @Test
    @DisplayName("Should generate accurate online booking confirmation template")
    void testBuildAppointmentBookedEvent_Online() {
        NotificationEvent event = templateService.buildAppointmentBookedEvent(appointment);

        assertNotNull(event);
        assertEquals(patientUser.getId(), event.getRecipientUserId());
        assertEquals("alice@carex.com", event.getRecipientEmail());
        assertEquals(NotificationType.APPOINTMENT_BOOKED, event.getNotificationType());
        assertTrue(event.getSubject().contains("#1001"));
        assertTrue(event.getMessage().contains("Dr. Priya Sharma"));
        assertTrue(event.getMessage().contains("Dermatology"));
        assertTrue(event.getMessage().contains("ONLINE"));
        assertTrue(event.getHtmlBody().contains("Appointment Booked"));
    }

    @Test
    @DisplayName("Should generate accurate offline instructions when mode is OFFLINE")
    void testBuildAppointmentConfirmedEvent_Offline() {
        slot.setMode(AppointmentMode.OFFLINE);
        appointment.setMode(AppointmentMode.OFFLINE);

        NotificationEvent event = templateService.buildAppointmentConfirmedEvent(appointment);

        assertNotNull(event);
        assertEquals(NotificationType.APPOINTMENT_CONFIRMED, event.getNotificationType());
        assertTrue(event.getMessage().contains("OFFLINE"));
        assertTrue(event.getMessage().contains("arrive at the clinic"));
    }

    @Test
    @DisplayName("Should generate cancellation template")
    void testBuildAppointmentCancelledEvent() {
        NotificationEvent event = templateService.buildAppointmentCancelledEvent(appointment);

        assertNotNull(event);
        assertEquals(NotificationType.APPOINTMENT_CANCELLED, event.getNotificationType());
        assertTrue(event.getSubject().contains("Cancelled"));
        assertTrue(event.getMessage().contains("CANCELLED"));
    }

    @Test
    @DisplayName("Should generate reminder template with high priority")
    void testBuildAppointmentReminderEvent() {
        NotificationEvent event = templateService.buildAppointmentReminderEvent(appointment);

        assertNotNull(event);
        assertEquals(NotificationType.APPOINTMENT_REMINDER, event.getNotificationType());
        assertEquals(NotificationPriority.HIGH, event.getPriority());
        assertTrue(event.getSubject().contains("Reminder"));
    }

    @Test
    @DisplayName("Should generate waitlist slot available template")
    void testBuildWaitlistSlotAvailableEvent() {
        NotificationEvent event = templateService.buildWaitlistSlotAvailableEvent(
                10L, "alice@carex.com", "Alice Patient",
                doctor, specialty, LocalDate.of(2026, 9, 21), LocalTime.of(14, 0), AppointmentMode.ONLINE
        );

        assertNotNull(event);
        assertEquals(NotificationType.WAITLIST_SLOT_AVAILABLE, event.getNotificationType());
        assertEquals(NotificationPriority.HIGH, event.getPriority());
        assertTrue(event.getMessage().contains("Dr. Priya Sharma"));
        assertTrue(event.getMessage().contains("Dermatology"));
    }

    @Test
    @DisplayName("Should generate doctor disruption critical alert template")
    void testBuildDoctorUnavailableEvent() {
        NotificationEvent event = templateService.buildDoctorUnavailableEvent(appointment, "Priya Sharma");

        assertNotNull(event);
        assertEquals(NotificationType.DOCTOR_UNAVAILABLE, event.getNotificationType());
        assertEquals(NotificationPriority.CRITICAL, event.getPriority());
        assertTrue(event.getMessage().contains("unavailable"));
    }
}
