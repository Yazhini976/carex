package com.carex.service;

import com.carex.entity.*;
import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.AppointmentStatus;
import com.carex.entity.enums.Role;
import com.carex.entity.enums.SlotStatus;
import com.carex.exception.BusinessRuleException;
import com.carex.exception.InvalidAppointmentStateException;
import com.carex.exception.SlotUnavailableException;
import com.carex.notification.NotificationDispatcher;
import com.carex.notification.NotificationTemplateService;
import com.carex.repository.AppointmentRepository;
import com.carex.repository.AppointmentStatusHistoryRepository;
import com.carex.repository.SlotRepository;
import com.carex.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentStatusHistoryRepository historyRepository;

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private PatientService patientService;

    @Mock
    private DoctorService doctorService;

    @Mock
    private SpecialtyService specialtyService;

    @Mock
    private DoctorSpecialtyService doctorSpecialtyService;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private NotificationTemplateService notificationTemplateService;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private User patientUser;
    private User doctorUserA;
    private User doctorUserB;
    private Patient patient;
    private Doctor doctorA;
    private Doctor doctorB;
    private Specialty dermatology;
    private Slot slotA_Online;
    private Slot slotA_Offline;
    private Slot slotB_Offline;

    @BeforeEach
    void setUp() {
        patientUser = new User();
        patientUser.setId(1L);
        patientUser.setName("John Patient");
        patientUser.setEmail("john@carex.com");
        patientUser.setRole(Role.PATIENT);
        patient = new Patient(patientUser);
        patient.setId(10L);

        doctorUserA = new User();
        doctorUserA.setId(2L);
        doctorUserA.setName("Dr. Alice");
        doctorUserA.setEmail("alice@carex.com");
        doctorUserA.setRole(Role.DOCTOR);
        doctorA = new Doctor(doctorUserA, "LIC-101");
        doctorA.setId(20L);
        doctorA.setIsActive(true);

        doctorUserB = new User();
        doctorUserB.setId(3L);
        doctorUserB.setName("Dr. Bob");
        doctorUserB.setEmail("bob@carex.com");
        doctorUserB.setRole(Role.DOCTOR);
        doctorB = new Doctor(doctorUserB, "LIC-102");
        doctorB.setId(30L);
        doctorB.setIsActive(true);

        dermatology = new Specialty("Dermatology");
        dermatology.setId(100L);
        dermatology.setIsActive(true);

        LocalDate date = LocalDate.of(2026, 9, 25);

        slotA_Online = new Slot(doctorA, date, LocalTime.of(10, 0), LocalTime.of(10, 30), AppointmentMode.ONLINE);
        slotA_Online.setId(201L);
        slotA_Online.setStatus(SlotStatus.AVAILABLE);

        slotA_Offline = new Slot(doctorA, date, LocalTime.of(11, 0), LocalTime.of(11, 30), AppointmentMode.OFFLINE);
        slotA_Offline.setId(202L);
        slotA_Offline.setStatus(SlotStatus.AVAILABLE);

        slotB_Offline = new Slot(doctorB, date, LocalTime.of(14, 0), LocalTime.of(14, 30), AppointmentMode.OFFLINE);
        slotB_Offline.setId(203L);
        slotB_Offline.setStatus(SlotStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Successful Booking: should create appointment, set slot to BOOKED, and record history")
    void testBookAppointment_Success() {
        when(patientService.getPatientById(10L)).thenReturn(patient);
        when(doctorService.getDoctorById(20L)).thenReturn(doctorA);
        when(specialtyService.getSpecialtyById(100L)).thenReturn(dermatology);
        when(slotRepository.findByIdWithLock(201L)).thenReturn(Optional.of(slotA_Online));
        when(doctorSpecialtyService.isAssigned(20L, 100L)).thenReturn(true);
        when(appointmentRepository.findActiveByPatientIdAndMode(10L, AppointmentMode.OFFLINE)).thenReturn(Collections.emptyList());

        Appointment savedAppointment = new Appointment(patient, doctorA, dermatology, slotA_Online, AppointmentMode.ONLINE);
        savedAppointment.setId(5001L);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        Appointment result = appointmentService.bookAppointment(10L, 20L, 100L, 201L, AppointmentMode.ONLINE, "Routine checkup");

        assertNotNull(result);
        assertEquals(5001L, result.getId());
        assertEquals(SlotStatus.BOOKED, slotA_Online.getStatus());
        verify(slotRepository, times(1)).save(slotA_Online);
        verify(historyRepository, times(1)).save(any(AppointmentStatusHistory.class));
    }

    @Test
    @DisplayName("Slot Unavailable: should throw SlotUnavailableException when slot is already BOOKED")
    void testBookAppointment_SlotUnavailable() {
        slotA_Online.setStatus(SlotStatus.BOOKED);

        when(patientService.getPatientById(10L)).thenReturn(patient);
        when(doctorService.getDoctorById(20L)).thenReturn(doctorA);
        when(specialtyService.getSpecialtyById(100L)).thenReturn(dermatology);
        when(slotRepository.findByIdWithLock(201L)).thenReturn(Optional.of(slotA_Online));

        assertThrows(SlotUnavailableException.class, () ->
                appointmentService.bookAppointment(10L, 20L, 100L, 201L, AppointmentMode.ONLINE, "Consultation")
        );

        verify(appointmentRepository, never()).save(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Invalid Doctor-Specialty: should reject booking when doctor is not assigned to requested specialty")
    void testBookAppointment_InvalidDoctorSpecialty() {
        when(patientService.getPatientById(10L)).thenReturn(patient);
        when(doctorService.getDoctorById(20L)).thenReturn(doctorA);
        when(specialtyService.getSpecialtyById(100L)).thenReturn(dermatology);
        when(slotRepository.findByIdWithLock(201L)).thenReturn(Optional.of(slotA_Online));
        when(doctorSpecialtyService.isAssigned(20L, 100L)).thenReturn(false);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                appointmentService.bookAppointment(10L, 20L, 100L, 201L, AppointmentMode.ONLINE, "Consultation")
        );

        assertTrue(ex.getMessage().contains("not assigned to specialty"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("HCL Online/Offline Rule: should REJECT booking if same doctor has active opposite mode appointment")
    void testBookAppointment_HclRule_RejectSameDoctorOppositeMode() {
        when(patientService.getPatientById(10L)).thenReturn(patient);
        when(doctorService.getDoctorById(20L)).thenReturn(doctorA);
        when(specialtyService.getSpecialtyById(100L)).thenReturn(dermatology);
        when(slotRepository.findByIdWithLock(202L)).thenReturn(Optional.of(slotA_Offline));
        when(doctorSpecialtyService.isAssigned(20L, 100L)).thenReturn(true);

        // Active ONLINE appointment already exists with Doctor A
        Appointment existingOnline = new Appointment(patient, doctorA, dermatology, slotA_Online, AppointmentMode.ONLINE);
        existingOnline.setStatus(AppointmentStatus.BOOKED);
        when(appointmentRepository.findActiveByPatientIdAndMode(10L, AppointmentMode.ONLINE))
                .thenReturn(List.of(existingOnline));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                appointmentService.bookAppointment(10L, 20L, 100L, 202L, AppointmentMode.OFFLINE, "Offline visit")
        );

        assertTrue(ex.getMessage().contains("require different doctors"));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("HCL Online/Offline Rule: should ALLOW booking if different doctor is used for opposite mode")
    void testBookAppointment_HclRule_AllowDifferentDoctorOppositeMode() {
        when(patientService.getPatientById(10L)).thenReturn(patient);
        when(doctorService.getDoctorById(30L)).thenReturn(doctorB);
        when(specialtyService.getSpecialtyById(100L)).thenReturn(dermatology);
        when(slotRepository.findByIdWithLock(203L)).thenReturn(Optional.of(slotB_Offline));
        when(doctorSpecialtyService.isAssigned(30L, 100L)).thenReturn(true);

        // Active ONLINE appointment exists with Doctor A, but booking OFFLINE with Doctor B
        Appointment existingOnlineWithDoctorA = new Appointment(patient, doctorA, dermatology, slotA_Online, AppointmentMode.ONLINE);
        when(appointmentRepository.findActiveByPatientIdAndMode(10L, AppointmentMode.ONLINE))
                .thenReturn(List.of(existingOnlineWithDoctorA));

        Appointment savedAppointment = new Appointment(patient, doctorB, dermatology, slotB_Offline, AppointmentMode.OFFLINE);
        savedAppointment.setId(5002L);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        Appointment result = appointmentService.bookAppointment(10L, 30L, 100L, 203L, AppointmentMode.OFFLINE, "Offline visit with Doctor B");

        assertNotNull(result);
        assertEquals(5002L, result.getId());
        assertEquals(SlotStatus.BOOKED, slotB_Offline.getStatus());
    }

    @Test
    @DisplayName("Status Transitions: BOOKED -> CONFIRMED -> COMPLETED lifecycle")
    void testStatusTransitions_BookedToConfirmedToCompleted() {
        Appointment apt = new Appointment(patient, doctorA, dermatology, slotA_Online, AppointmentMode.ONLINE);
        apt.setId(1001L);
        apt.setStatus(AppointmentStatus.BOOKED);

        when(appointmentRepository.findById(1001L)).thenReturn(Optional.of(apt));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        // 1. Confirm
        Appointment confirmed = appointmentService.confirmAppointment(1001L, 2L);
        assertEquals(AppointmentStatus.CONFIRMED, confirmed.getStatus());

        // 2. Complete
        Appointment completed = appointmentService.completeAppointment(1001L, 2L);
        assertEquals(AppointmentStatus.COMPLETED, completed.getStatus());
        assertNotNull(completed.getCompletedAt());
    }

    @Test
    @DisplayName("Status Transition: Cancel releases slot back to AVAILABLE")
    void testStatusTransition_CancelReleasesSlot() {
        slotA_Online.setStatus(SlotStatus.BOOKED);
        Appointment apt = new Appointment(patient, doctorA, dermatology, slotA_Online, AppointmentMode.ONLINE);
        apt.setId(1001L);
        apt.setStatus(AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findById(1001L)).thenReturn(Optional.of(apt));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment cancelled = appointmentService.cancelAppointment(1001L, 1L);

        assertEquals(AppointmentStatus.CANCELLED, cancelled.getStatus());
        assertNotNull(cancelled.getCancelledAt());
        assertEquals(SlotStatus.AVAILABLE, slotA_Online.getStatus());
        verify(slotRepository, times(1)).save(slotA_Online);
    }

    @Test
    @DisplayName("Invalid Status Transition: COMPLETED cannot transition to CANCELLED")
    void testInvalidStatusTransition_CompletedToCancelledThrows() {
        Appointment apt = new Appointment(patient, doctorA, dermatology, slotA_Online, AppointmentMode.ONLINE);
        apt.setId(1001L);
        apt.setStatus(AppointmentStatus.COMPLETED);

        when(appointmentRepository.findById(1001L)).thenReturn(Optional.of(apt));

        assertThrows(InvalidAppointmentStateException.class, () ->
                appointmentService.cancelAppointment(1001L, 1L)
        );
    }
}
