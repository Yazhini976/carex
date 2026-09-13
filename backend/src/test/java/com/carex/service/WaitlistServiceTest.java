package com.carex.service;

import com.carex.entity.*;
import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.Role;
import com.carex.entity.enums.WaitlistStatus;
import com.carex.exception.BusinessRuleException;
import com.carex.repository.WaitlistRepository;
import com.carex.service.impl.WaitlistServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaitlistServiceTest {

    @Mock
    private WaitlistRepository waitlistRepository;

    @Mock
    private PatientService patientService;

    @Mock
    private SpecialtyService specialtyService;

    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private WaitlistServiceImpl waitlistService;

    private Patient patient;
    private Doctor doctor;
    private Specialty specialty;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setName("Alice Patient");
        user.setRole(Role.PATIENT);
        patient = new Patient(user);
        patient.setId(10L);

        User docUser = new User();
        docUser.setId(2L);
        docUser.setName("Dr. Priya");
        docUser.setRole(Role.DOCTOR);
        doctor = new Doctor(docUser, "DOC-99");
        doctor.setId(20L);

        specialty = new Specialty("Cardiology");
        specialty.setId(100L);
    }

    @Test
    @DisplayName("Should successfully join waitlist with preferred doctor and mode")
    void testJoinWaitlist_Success() {
        LocalDate preferredDate = LocalDate.of(2026, 9, 30);
        when(patientService.getPatientById(10L)).thenReturn(patient);
        when(specialtyService.getSpecialtyById(100L)).thenReturn(specialty);
        when(doctorService.getDoctorById(20L)).thenReturn(doctor);

        Waitlist entry = new Waitlist(patient, specialty);
        entry.setId(1L);
        entry.setPreferredDoctor(doctor);
        entry.setPreferredDate(preferredDate);
        entry.setPreferredMode(AppointmentMode.ONLINE);

        when(waitlistRepository.save(any(Waitlist.class))).thenReturn(entry);

        Waitlist result = waitlistService.joinWaitlist(10L, 100L, 20L, preferredDate, AppointmentMode.ONLINE);

        assertNotNull(result);
        assertEquals(WaitlistStatus.WAITING, result.getStatus());
        assertEquals(doctor, result.getPreferredDoctor());
        assertEquals(AppointmentMode.ONLINE, result.getPreferredMode());
        verify(waitlistRepository, times(1)).save(any(Waitlist.class));
    }

    @Test
    @DisplayName("Should transition status through WAITING -> OFFERED -> FULFILLED")
    void testWaitlistLifecycleTransitions() {
        Waitlist entry = new Waitlist(patient, specialty);
        entry.setId(1L);
        entry.setStatus(WaitlistStatus.WAITING);

        when(waitlistRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(waitlistRepository.save(any(Waitlist.class))).thenAnswer(inv -> inv.getArgument(0));

        // 1. Mark Offered
        Waitlist offered = waitlistService.markOffered(1L);
        assertEquals(WaitlistStatus.OFFERED, offered.getStatus());

        // 2. Mark Fulfilled
        Waitlist fulfilled = waitlistService.markFulfilled(1L);
        assertEquals(WaitlistStatus.FULFILLED, fulfilled.getStatus());
        assertNotNull(fulfilled.getFulfilledAt());
    }

    @Test
    @DisplayName("Should cancel active waitlist entry")
    void testCancelWaitlistEntry() {
        Waitlist entry = new Waitlist(patient, specialty);
        entry.setId(1L);
        entry.setStatus(WaitlistStatus.WAITING);

        when(waitlistRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(waitlistRepository.save(any(Waitlist.class))).thenAnswer(inv -> inv.getArgument(0));

        Waitlist cancelled = waitlistService.cancelEntry(1L);
        assertEquals(WaitlistStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    @DisplayName("Should reject invalid state transition for fulfilled entry")
    void testInvalidTransition_FulfilledCannotBeCancelled() {
        Waitlist entry = new Waitlist(patient, specialty);
        entry.setId(1L);
        entry.setStatus(WaitlistStatus.FULFILLED);

        when(waitlistRepository.findById(1L)).thenReturn(Optional.of(entry));

        assertThrows(BusinessRuleException.class, () ->
                waitlistService.cancelEntry(1L)
        );
    }
}
