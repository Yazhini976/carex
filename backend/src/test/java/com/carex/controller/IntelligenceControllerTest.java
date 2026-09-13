package com.carex.controller;

import com.carex.config.SecurityConfig;
import com.carex.dto.intelligence.DoctorMatchRequest;
import com.carex.dto.intelligence.SimulationRequest;
import com.carex.dto.intelligence.SpecialtyRecommendationRequest;
import com.carex.entity.Doctor;
import com.carex.entity.Specialty;
import com.carex.entity.User;
import com.carex.entity.WaitTimePrediction;
import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.Role;
import com.carex.exception.GlobalExceptionHandler;
import com.carex.service.RecommendationService;
import com.carex.service.WaitTimePredictionService;
import com.carex.service.intelligence.DisruptionRecoveryService;
import com.carex.service.intelligence.DoctorMatchingService;
import com.carex.service.intelligence.DoctorMatchingService.DoctorMatchResult;
import com.carex.service.intelligence.NoShowPredictionService;
import com.carex.service.intelligence.SchedulingSimulationService;
import com.carex.service.intelligence.SchedulingSimulationService.ScenarioType;
import com.carex.service.intelligence.SchedulingSimulationService.SimulationResult;
import com.carex.service.intelligence.SmartWaitlistService;
import com.carex.service.intelligence.SpecialtyRecommendationService;
import com.carex.service.intelligence.SpecialtyRecommendationService.RecommendationResult;
import com.carex.service.intelligence.WaitTimeService;
import com.carex.service.intelligence.WorkloadIntelligenceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IntelligenceController.class)
@Import({
        com.carex.config.SecurityConfig.class,
        com.carex.config.CorsConfig.class,
        com.carex.security.JwtService.class,
        com.carex.security.JwtAuthenticationFilter.class,
        com.carex.security.RateLimitingFilter.class,
        com.carex.security.RestAuthenticationEntryPoint.class,
        com.carex.security.RestAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class IntelligenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SpecialtyRecommendationService specialtyRecommendationService;

    @MockBean
    private RecommendationService recommendationService;

    @MockBean
    private DoctorMatchingService doctorMatchingService;

    @MockBean
    private WaitTimeService waitTimeService;

    @MockBean
    private WaitTimePredictionService waitTimePredictionService;

    @MockBean
    private WorkloadIntelligenceService workloadIntelligenceService;

    @MockBean
    private SchedulingSimulationService schedulingSimulationService;

    @MockBean
    private NoShowPredictionService noShowPredictionService;

    @MockBean
    private DisruptionRecoveryService disruptionRecoveryService;

    @MockBean
    private SmartWaitlistService smartWaitlistService;

    @MockBean
    private com.carex.security.CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("POST /api/intelligence/specialty-recommendation - Success 200 OK")
    void testRecommendSpecialty_Success() throws Exception {
        Specialty specialty = new Specialty("Cardiology");
        specialty.setId(1L);
        specialty.setDescription("Heart");

        RecommendationResult mockResult = new RecommendationResult(
                Optional.of(specialty),
                "Keywords match cardiology symptoms",
                0.95
        );

        when(specialtyRecommendationService.recommend(eq(10L), any())).thenReturn(mockResult);

        SpecialtyRecommendationRequest request = new SpecialtyRecommendationRequest(10L, "I have chest pain and shortness of breath");

        mockMvc.perform(post("/api/intelligence/specialty-recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendedSpecialtyId").value(1))
                .andExpect(jsonPath("$.recommendedSpecialty").value("Cardiology"))
                .andExpect(jsonPath("$.disclaimer").exists());
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "PATIENT")
    @DisplayName("POST /api/intelligence/doctor-match - Success 200 OK")
    void testDoctorMatch_Success() throws Exception {
        User user = new User("Dr. Adams", "adams@carex.com", "hash", Role.DOCTOR);
        Doctor doctor = new Doctor(user, "LIC-112233");
        doctor.setId(7L);
        doctor.setConsultationFee(new BigDecimal("120.00"));
        Specialty specialty = new Specialty("General Medicine");
        specialty.setId(2L);
        specialty.setDescription("General");

        DoctorMatchResult matchResult = new DoctorMatchResult(
                doctor, specialty, true, true, true, 15, 92.5, "Exact match with slot available"
        );

        when(doctorMatchingService.rankDoctors(eq(2L), eq(AppointmentMode.ONLINE)))
                .thenReturn(List.of(matchResult));

        DoctorMatchRequest request = new DoctorMatchRequest(2L, AppointmentMode.ONLINE);

        mockMvc.perform(post("/api/intelligence/doctor-match")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].doctorId").value(7))
                .andExpect(jsonPath("$[0].doctorName").value("Dr. Adams"))
                .andExpect(jsonPath("$[0].score").value(92.5));
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/intelligence/simulation - Success 200 OK Read-Only")
    void testSimulation_Success() throws Exception {
        SimulationResult simResult = new SimulationResult(
                5L,
                LocalDate.of(2026, 9, 25),
                ScenarioType.DOCTOR_UNAVAILABLE,
                3,
                Collections.emptyList(),
                Collections.emptyList(),
                "3 appointments affected. 0 alternative doctors available.",
                "Consider redistributing to next business day.",
                true
        );

        when(schedulingSimulationService.simulate(eq(5L), eq(LocalDate.of(2026, 9, 25)), eq(ScenarioType.DOCTOR_UNAVAILABLE)))
                .thenReturn(simResult);

        SimulationRequest request = new SimulationRequest(5L, LocalDate.of(2026, 9, 25), ScenarioType.DOCTOR_UNAVAILABLE);

        mockMvc.perform(post("/api/intelligence/simulation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value(5))
                .andExpect(jsonPath("$.affectedAppointmentCount").value(3))
                .andExpect(jsonPath("$.readOnly").value(true));
    }
}
