package com.carex.controller;

import com.carex.config.SecurityConfig;
import com.carex.dto.appointment.AppointmentBookingRequest;
import com.carex.entity.Appointment;
import com.carex.entity.Doctor;
import com.carex.entity.Patient;
import com.carex.entity.Slot;
import com.carex.entity.Specialty;
import com.carex.entity.User;
import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.AppointmentStatus;
import com.carex.entity.enums.Role;
import com.carex.entity.enums.SlotStatus;
import com.carex.exception.GlobalExceptionHandler;
import com.carex.exception.InvalidAppointmentStateException;
import com.carex.exception.ResourceNotFoundException;
import com.carex.exception.SlotUnavailableException;
import com.carex.mapper.AppointmentMapper;
import com.carex.security.CustomUserDetailsService;
import com.carex.security.JwtService;
import com.carex.service.AppointmentService;
import com.carex.service.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
@Import({
        com.carex.config.SecurityConfig.class,
        com.carex.config.CorsConfig.class,
        com.carex.security.JwtService.class,
        com.carex.security.JwtAuthenticationFilter.class,
        com.carex.security.RateLimitingFilter.class,
        com.carex.security.RestAuthenticationEntryPoint.class,
        com.carex.security.RestAccessDeniedHandler.class,
        GlobalExceptionHandler.class,
        AppointmentMapper.class
})
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private PatientService patientService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private Appointment sampleAppointment;

    @BeforeEach
    void setUp() {
        User patientUser = new User("John Doe", "john@example.com", "hash", Role.PATIENT);
        patientUser.setId(10L);
        patientUser.setPhone("1234567890");

        Patient patient = new Patient(patientUser);
        patient.setId(1L);
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));

        User doctorUser = new User("Dr. Sarah Smith", "sarah@carex.com", "hash", Role.DOCTOR);
        doctorUser.setId(20L);

        Doctor doctor = new Doctor(doctorUser, "DOC-12345");
        doctor.setId(2L);
        doctor.setQualification("MD Cardiology");
        doctor.setConsultationFee(new BigDecimal("150.00"));

        Specialty specialty = new Specialty("Cardiology");
        specialty.setId(3L);
        specialty.setDescription("Heart care");

        Slot slot = new Slot(doctor, LocalDate.of(2026, 9, 20), LocalTime.of(10, 0), LocalTime.of(10, 30), AppointmentMode.OFFLINE);
        slot.setId(4L);
        slot.setStatus(SlotStatus.BOOKED);

        sampleAppointment = new Appointment(patient, doctor, specialty, slot, AppointmentMode.OFFLINE);
        sampleAppointment.setId(100L);
        sampleAppointment.setNotes("Routine heart checkup");
        sampleAppointment.setStatus(AppointmentStatus.BOOKED);
        sampleAppointment.setBookedAt(LocalDateTime.of(2026, 9, 11, 10, 0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/appointments - Success 201 CREATED")
    void testBookAppointment_Success() throws Exception {
        AppointmentBookingRequest request = new AppointmentBookingRequest(
                1L, 2L, 3L, 4L, AppointmentMode.OFFLINE, "Routine heart checkup"
        );

        when(appointmentService.bookAppointment(eq(1L), eq(2L), eq(3L), eq(4L), eq(AppointmentMode.OFFLINE), eq("Routine heart checkup")))
                .thenReturn(sampleAppointment);

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appointmentId").value(100))
                .andExpect(jsonPath("$.patientName").value("John Doe"))
                .andExpect(jsonPath("$.doctorName").value("Dr. Sarah Smith"))
                .andExpect(jsonPath("$.specialtyName").value("Cardiology"))
                .andExpect(jsonPath("$.status").value("BOOKED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/appointments - Slot Unavailable 409 CONFLICT")
    void testBookAppointment_SlotUnavailable() throws Exception {
        AppointmentBookingRequest request = new AppointmentBookingRequest(
                1L, 2L, 3L, 4L, AppointmentMode.OFFLINE, "Notes"
        );

        when(appointmentService.bookAppointment(any(), any(), any(), any(), any(), any()))
                .thenThrow(SlotUnavailableException.forSlot(4L));

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Slot Unavailable"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/appointments - Validation Failure 400 BAD_REQUEST")
    void testBookAppointment_ValidationError() throws Exception {
        AppointmentBookingRequest invalidRequest = new AppointmentBookingRequest();
        // missing mandatory fields

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/appointments/{id} - Success 200 OK")
    void testGetAppointmentById_Success() throws Exception {
        when(appointmentService.getAppointmentById(100L)).thenReturn(sampleAppointment);

        mockMvc.perform(get("/api/appointments/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value(100))
                .andExpect(jsonPath("$.patientEmail").value("john@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/appointments/{id} - Not Found 404 NOT_FOUND")
    void testGetAppointmentById_NotFound() throws Exception {
        when(appointmentService.getAppointmentById(999L))
                .thenThrow(ResourceNotFoundException.of("Appointment", 999L));

        mockMvc.perform(get("/api/appointments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/appointments/{id}/cancel - Success 200 OK")
    void testCancelAppointment_Success() throws Exception {
        sampleAppointment.setStatus(AppointmentStatus.CANCELLED);
        sampleAppointment.setCancelledAt(LocalDateTime.now());
        when(appointmentService.cancelAppointment(eq(100L), any())).thenReturn(sampleAppointment);

        mockMvc.perform(put("/api/appointments/100/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/appointments/{id}/confirm - Invalid State 409 CONFLICT")
    void testConfirmAppointment_InvalidState() throws Exception {
        when(appointmentService.getAppointmentById(100L)).thenReturn(sampleAppointment);
        when(appointmentService.confirmAppointment(eq(100L), any()))
                .thenThrow(new InvalidAppointmentStateException("Cannot confirm appointment from CANCELLED state"));

        mockMvc.perform(put("/api/appointments/100/confirm"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Invalid Appointment State"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/appointments/summary/daily - Success 200 OK")
    void testGetDailySummary_Success() throws Exception {
        Map<String, Object> summary = Map.of(
                "totalAppointments", 10L,
                "booked", 4L,
                "confirmed", 3L,
                "completed", 2L,
                "cancelled", 1L,
                "noShow", 0L
        );

        when(appointmentService.getDailySummary(any(LocalDate.class))).thenReturn(summary);

        mockMvc.perform(get("/api/appointments/summary/daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAppointments").value(10))
                .andExpect(jsonPath("$.booked").value(4))
                .andExpect(jsonPath("$.confirmed").value(3))
                .andExpect(jsonPath("$.completed").value(2));
    }
}
