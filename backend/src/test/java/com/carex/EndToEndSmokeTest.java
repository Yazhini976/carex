package com.carex;

import com.carex.dto.appointment.AppointmentBookingRequest;
import com.carex.dto.auth.LoginRequest;
import com.carex.dto.auth.RegisterRequest;
import com.carex.dto.intelligence.DoctorMatchRequest;
import com.carex.dto.intelligence.SimulationRequest;
import com.carex.dto.intelligence.SpecialtyRecommendationRequest;
import com.carex.entity.*;
import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.AppointmentStatus;
import com.carex.entity.enums.Role;
import com.carex.entity.enums.SlotStatus;
import com.carex.repository.*;
import com.carex.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EndToEndSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private DoctorSpecialtyRepository doctorSpecialtyRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private Specialty cardiology;
    private Doctor doctor;
    private Slot slot;
    private User adminUser;

    @BeforeEach
    void setupTestData() {
        // 1. Specialty
        cardiology = specialtyRepository.findByName("Cardiology")
                .orElseGet(() -> specialtyRepository.save(new Specialty("Cardiology")));

        // 2. Doctor User & Entity
        User docUser = new User();
        docUser.setEmail("dr.smoke@carex.com");
        docUser.setPasswordHash(passwordEncoder.encode("Password123!"));
        docUser.setName("Dr. Smoke Test");
        docUser.setRole(Role.DOCTOR);
        docUser.setIsActive(true);
        User savedDocUser = userRepository.save(docUser);

        doctor = new Doctor(savedDocUser, "SMOKE-LIC-99");
        doctor.setExperienceYears(10);
        doctor = doctorRepository.save(doctor);

        // Assign Doctor to Specialty
        DoctorSpecialty ds = new DoctorSpecialty(doctor, cardiology);
        doctorSpecialtyRepository.save(ds);

        // 3. Available Slot
        slot = new Slot(doctor, LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30), AppointmentMode.ONLINE);
        slot.setStatus(SlotStatus.AVAILABLE);
        slot = slotRepository.save(slot);

        // 4. Admin User
        adminUser = new User();
        adminUser.setEmail("admin.smoke@carex.com");
        adminUser.setPasswordHash(passwordEncoder.encode("Password123!"));
        adminUser.setName("Admin Smoke");
        adminUser.setRole(Role.ADMIN);
        adminUser.setIsActive(true);
        adminUser = userRepository.save(adminUser);
    }

    @Test
    @DisplayName("End-to-End Happy Path Smoke Test: Register -> Login -> Specialty Rec -> Doctor Match -> Book -> Notify -> Simulate")
    void testEndToEndLifecycleFlow() throws Exception {
        // Step 1: Register new patient
        String uniqueEmail = "patient.smoke." + System.currentTimeMillis() + "@carex.com";
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setName("Smoke Patient");
        registerReq.setEmail(uniqueEmail);
        registerReq.setPassword("SecurePass123!");
        registerReq.setPhone("+15559876");
        registerReq.setRole(Role.PATIENT);

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode regNode = objectMapper.readTree(regResult.getResponse().getContentAsString());
        Long patientUserId = regNode.get("id").asLong();
        assertNotNull(patientUserId);

        // Link Patient profile to registered User
        Patient patient = patientRepository.findByUserId(patientUserId)
                .orElseGet(() -> {
                    User user = userRepository.findById(patientUserId).orElseThrow();
                    return patientRepository.save(new Patient(user));
                });

        // Step 2: Login and obtain JWT
        LoginRequest loginReq = new LoginRequest(uniqueEmail, "SecurePass123!");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginNode.get("token").asText();
        assertNotNull(token);
        String authHeader = "Bearer " + token;

        // Step 3: AI Smart Specialty Recommendation
        SpecialtyRecommendationRequest recReq = new SpecialtyRecommendationRequest();
        recReq.setPatientId(patient.getId());
        recReq.setInputText("I have chest pain, heart palpitations and shortness of breath.");

        mockMvc.perform(post("/api/intelligence/specialty-recommendation")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendedSpecialty").value("Cardiology"));

        // Step 4: Explainable Doctor Matching
        DoctorMatchRequest matchReq = new DoctorMatchRequest();
        matchReq.setSpecialtyId(cardiology.getId());
        matchReq.setMode(AppointmentMode.ONLINE);

        mockMvc.perform(post("/api/intelligence/doctor-match")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(matchReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].doctorId").value(org.hamcrest.Matchers.hasItem(doctor.getId().intValue())))
                .andExpect(jsonPath("$[0].score").isNumber())
                .andExpect(jsonPath("$[0].reasoning").isString());

        // Step 5: Book Appointment
        AppointmentBookingRequest bookingReq = new AppointmentBookingRequest();
        bookingReq.setPatientId(patient.getId());
        bookingReq.setDoctorId(doctor.getId());
        bookingReq.setSpecialtyId(cardiology.getId());
        bookingReq.setSlotId(slot.getId());
        bookingReq.setMode(AppointmentMode.ONLINE);
        bookingReq.setNotes("Smoke test appointment booking");

        MvcResult bookingResult = mockMvc.perform(post("/api/appointments")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("BOOKED"))
                .andReturn();

        JsonNode bookingNode = objectMapper.readTree(bookingResult.getResponse().getContentAsString());
        Long appointmentId = bookingNode.get("appointmentId").asLong();

        // Step 6: Verify In-App Notification was generated
        mockMvc.perform(get("/api/notifications/user/" + patientUserId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("APPOINTMENT_BOOKED"));

        // Step 7: Wait-Time Prediction API
        mockMvc.perform(get("/api/intelligence/wait-time/" + appointmentId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value(appointmentId))
                .andExpect(jsonPath("$.predictedMinutes").isNumber());

        // Step 8: Admin simulation (Read-only guarantee)
        String adminToken = "Bearer " + jwtService.generateToken(adminUser);
        long aptCountBefore = appointmentRepository.count();

        SimulationRequest simReq = new SimulationRequest();
        simReq.setDoctorId(doctor.getId());
        simReq.setDate(LocalDate.now().plusDays(1));
        simReq.setScenario(com.carex.service.intelligence.SchedulingSimulationService.ScenarioType.DOCTOR_UNAVAILABLE);

        mockMvc.perform(post("/api/intelligence/simulation")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(simReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scenario").value("DOCTOR_UNAVAILABLE"))
                .andExpect(jsonPath("$.readOnly").value(true));

        long aptCountAfter = appointmentRepository.count();
        assertEquals(aptCountBefore, aptCountAfter, "Simulation MUST NOT modify database records!");
    }
}
