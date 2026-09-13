package com.carex.security;

import com.carex.config.CorsConfig;
import com.carex.config.SecurityConfig;
import com.carex.controller.AnalyticsController;
import com.carex.controller.AppointmentController;
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
import com.carex.mapper.AppointmentMapper;
import com.carex.service.AppointmentService;
import com.carex.service.DoctorService;
import com.carex.service.PatientService;
import com.carex.service.WorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AppointmentController.class, AnalyticsController.class})
@Import({
        SecurityConfig.class,
        CorsConfig.class,
        JwtService.class,
        JwtAuthenticationFilter.class,
        RateLimitingFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        GlobalExceptionHandler.class,
        AppointmentMapper.class
})
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private PatientService patientService;

    @MockBean
    private WorkloadService workloadService;

    @MockBean
    private DoctorService doctorService;

    private User patientUser1;
    private User patientUser2;
    private User doctorUser1;
    private User doctorUser2;
    private User adminUser;

    private Appointment appointment1;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", "carexSuperSecureTestSecretKeyWithAtLeast256BitsLength1234567890!");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L);

        patientUser1 = new User("Patient One", "patient1@carex.com", "hash", Role.PATIENT);
        patientUser1.setId(101L);

        patientUser2 = new User("Patient Two", "patient2@carex.com", "hash", Role.PATIENT);
        patientUser2.setId(102L);

        doctorUser1 = new User("Doctor One", "doctor1@carex.com", "hash", Role.DOCTOR);
        doctorUser1.setId(201L);

        doctorUser2 = new User("Doctor Two", "doctor2@carex.com", "hash", Role.DOCTOR);
        doctorUser2.setId(202L);

        adminUser = new User("Admin", "admin@carex.com", "hash", Role.ADMIN);
        adminUser.setId(301L);

        Patient patient1 = new Patient(patientUser1);
        patient1.setId(1L);

        Doctor doctor1 = new Doctor(doctorUser1, "LIC-111");
        doctor1.setId(1L);
        doctor1.setConsultationFee(new BigDecimal("100.00"));

        Specialty specialty = new Specialty("Cardiology");
        specialty.setId(1L);

        Slot slot = new Slot(doctor1, LocalDate.of(2026, 9, 20), LocalTime.of(10, 0), LocalTime.of(10, 15), AppointmentMode.OFFLINE);
        slot.setId(1L);
        slot.setStatus(SlotStatus.BOOKED);

        appointment1 = new Appointment(patient1, doctor1, specialty, slot, AppointmentMode.OFFLINE);
        appointment1.setId(500L);
        appointment1.setStatus(AppointmentStatus.BOOKED);
        appointment1.setBookedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Scenario 1: No token on protected endpoint -> 401 Unauthorized")
    void testNoToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/appointments/500"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("Scenario 2: Invalid token on protected endpoint -> 401 Unauthorized")
    void testInvalidToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/appointments/500")
                        .header("Authorization", "Bearer invalid.malformed.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Scenario 3: Patient token on admin endpoint -> 403 Forbidden")
    void testPatientOnAdminEndpoint_Returns403() throws Exception {
        String patientToken = jwtService.generateToken(patientUser1);
        CustomUserPrincipal principal = new CustomUserPrincipal(patientUser1);
        when(customUserDetailsService.loadUserByUsername("patient1@carex.com")).thenReturn(principal);

        mockMvc.perform(get("/api/analytics/daily")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("Scenario 4: Patient token on own appointment -> 200 OK")
    void testPatientOnOwnAppointment_Returns200() throws Exception {
        String patientToken = jwtService.generateToken(patientUser1);
        CustomUserPrincipal principal = new CustomUserPrincipal(patientUser1);
        when(customUserDetailsService.loadUserByUsername("patient1@carex.com")).thenReturn(principal);
        when(appointmentService.getAppointmentById(500L)).thenReturn(appointment1);

        mockMvc.perform(get("/api/appointments/500")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value(500));
    }

    @Test
    @DisplayName("Scenario 5: Patient token on another patient's appointment -> 403 Forbidden (Object ownership)")
    void testPatientOnOtherPatientAppointment_Returns403() throws Exception {
        String otherPatientToken = jwtService.generateToken(patientUser2);
        CustomUserPrincipal principal = new CustomUserPrincipal(patientUser2);
        when(customUserDetailsService.loadUserByUsername("patient2@carex.com")).thenReturn(principal);
        when(appointmentService.getAppointmentById(500L)).thenReturn(appointment1);

        mockMvc.perform(get("/api/appointments/500")
                        .header("Authorization", "Bearer " + otherPatientToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("Scenario 6: Doctor token on own appointment -> 200 OK")
    void testDoctorOnOwnAppointment_Returns200() throws Exception {
        String doctorToken = jwtService.generateToken(doctorUser1);
        CustomUserPrincipal principal = new CustomUserPrincipal(doctorUser1);
        when(customUserDetailsService.loadUserByUsername("doctor1@carex.com")).thenReturn(principal);
        when(appointmentService.getAppointmentById(500L)).thenReturn(appointment1);

        mockMvc.perform(get("/api/appointments/500")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value(500));
    }

    @Test
    @DisplayName("Scenario 7: Doctor token on another doctor's appointment -> 403 Forbidden (Object ownership)")
    void testDoctorOnOtherDoctorAppointment_Returns403() throws Exception {
        String otherDoctorToken = jwtService.generateToken(doctorUser2);
        CustomUserPrincipal principal = new CustomUserPrincipal(doctorUser2);
        when(customUserDetailsService.loadUserByUsername("doctor2@carex.com")).thenReturn(principal);
        when(appointmentService.getAppointmentById(500L)).thenReturn(appointment1);

        mockMvc.perform(get("/api/appointments/500")
                        .header("Authorization", "Bearer " + otherDoctorToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }
}
