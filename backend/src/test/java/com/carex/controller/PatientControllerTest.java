package com.carex.controller;

import com.carex.config.SecurityConfig;
import com.carex.dto.patient.PatientRequest;
import com.carex.entity.Patient;
import com.carex.entity.User;
import com.carex.entity.enums.Role;
import com.carex.exception.GlobalExceptionHandler;
import com.carex.mapper.PatientMapper;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
@Import({
        com.carex.config.SecurityConfig.class,
        com.carex.config.CorsConfig.class,
        com.carex.security.JwtService.class,
        com.carex.security.JwtAuthenticationFilter.class,
        com.carex.security.RateLimitingFilter.class,
        com.carex.security.RestAuthenticationEntryPoint.class,
        com.carex.security.RestAccessDeniedHandler.class,
        GlobalExceptionHandler.class,
        PatientMapper.class
})
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PatientService patientService;

    @MockBean
    private com.carex.security.CustomUserDetailsService customUserDetailsService;

    private Patient samplePatient;

    @BeforeEach
    void setUp() {
        User user = new User("Jane Doe", "jane@carex.com", "hash", Role.PATIENT);
        user.setId(30L);
        user.setPhone("555-1234");

        samplePatient = new Patient(user);
        samplePatient.setId(12L);
        samplePatient.setDateOfBirth(LocalDate.of(1995, 5, 15));
        samplePatient.setGender("FEMALE");
        samplePatient.setEmergencyContact("555-9999");
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/patients - Success 200 OK")
    void testGetAllPatients_Success() throws Exception {
        when(patientService.getAllPatients()).thenReturn(List.of(samplePatient));

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(12))
                .andExpect(jsonPath("$[0].name").value("Jane Doe"))
                .andExpect(jsonPath("$[0].gender").value("FEMALE"));
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/patients - Success 201 CREATED")
    void testCreatePatient_Success() throws Exception {
        PatientRequest request = new PatientRequest(
                30L, LocalDate.of(1995, 5, 15), "FEMALE", "555-9999"
        );

        when(patientService.createPatient(eq(30L), any(), eq("FEMALE"), eq("555-9999")))
                .thenReturn(samplePatient);

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.emergencyContact").value("555-9999"));
    }
}
