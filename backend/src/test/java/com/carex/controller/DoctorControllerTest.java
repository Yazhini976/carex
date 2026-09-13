package com.carex.controller;

import com.carex.config.SecurityConfig;
import com.carex.dto.doctor.DoctorRequest;
import com.carex.entity.Doctor;
import com.carex.entity.Specialty;
import com.carex.entity.User;
import com.carex.entity.enums.Role;
import com.carex.exception.GlobalExceptionHandler;
import com.carex.mapper.DoctorMapper;
import com.carex.mapper.SpecialtyMapper;
import com.carex.service.DoctorService;
import com.carex.service.DoctorSpecialtyService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DoctorController.class)
@Import({
        com.carex.config.SecurityConfig.class,
        com.carex.config.CorsConfig.class,
        com.carex.security.JwtService.class,
        com.carex.security.JwtAuthenticationFilter.class,
        com.carex.security.RateLimitingFilter.class,
        com.carex.security.RestAuthenticationEntryPoint.class,
        com.carex.security.RestAccessDeniedHandler.class,
        GlobalExceptionHandler.class,
        DoctorMapper.class,
        SpecialtyMapper.class
})
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private DoctorSpecialtyService doctorSpecialtyService;

    @MockBean
    private com.carex.security.CustomUserDetailsService customUserDetailsService;

    private Doctor sampleDoctor;
    private Specialty sampleSpecialty;

    @BeforeEach
    void setUp() {
        User user = new User("Dr. Gregory House", "house@carex.com", "hash", Role.DOCTOR);
        user.setId(50L);
        user.setPhone("555-0199");

        sampleDoctor = new Doctor(user, "LIC-998877");
        sampleDoctor.setId(5L);
        sampleDoctor.setQualification("MD Diagnostics");
        sampleDoctor.setExperienceYears(15);
        sampleDoctor.setConsultationFee(new BigDecimal("250.00"));

        sampleSpecialty = new Specialty("Diagnostics");
        sampleSpecialty.setId(1L);
        sampleSpecialty.setDescription("Complex cases");
    }

    @Test
    @DisplayName("GET /api/doctors - Success 200 OK")
    void testGetAllDoctors_Success() throws Exception {
        when(doctorService.getAllDoctors()).thenReturn(List.of(sampleDoctor));
        when(doctorSpecialtyService.getSpecialtiesForDoctor(5L)).thenReturn(List.of(sampleSpecialty));

        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].name").value("Dr. Gregory House"))
                .andExpect(jsonPath("$[0].specialties[0].name").value("Diagnostics"));
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/doctors - Success 201 CREATED")
    void testCreateDoctor_Success() throws Exception {
        DoctorRequest request = new DoctorRequest(
                50L, "LIC-998877", "MD Diagnostics", 15, new BigDecimal("250.00")
        );

        when(doctorService.createDoctor(eq(50L), eq("LIC-998877"), eq("MD Diagnostics"), eq(15), any()))
                .thenReturn(sampleDoctor);

        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.licenseNumber").value("LIC-998877"));
    }
}
