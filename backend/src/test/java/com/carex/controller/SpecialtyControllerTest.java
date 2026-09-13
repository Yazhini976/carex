package com.carex.controller;

import com.carex.config.SecurityConfig;
import com.carex.dto.specialty.SpecialtyRequest;
import com.carex.entity.Specialty;
import com.carex.exception.BusinessRuleException;
import com.carex.exception.DuplicateResourceException;
import com.carex.exception.GlobalExceptionHandler;
import com.carex.exception.ResourceNotFoundException;
import com.carex.mapper.SpecialtyMapper;
import com.carex.service.SpecialtyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SpecialtyController.class)
@Import({
        com.carex.config.SecurityConfig.class,
        com.carex.config.CorsConfig.class,
        com.carex.security.JwtService.class,
        com.carex.security.JwtAuthenticationFilter.class,
        com.carex.security.RateLimitingFilter.class,
        com.carex.security.RestAuthenticationEntryPoint.class,
        com.carex.security.RestAccessDeniedHandler.class,
        GlobalExceptionHandler.class,
        SpecialtyMapper.class
})
class SpecialtyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SpecialtyService specialtyService;

    @MockBean
    private com.carex.security.CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("GET /api/specialties - Success 200 OK")
    void testGetAllSpecialties() throws Exception {
        Specialty s1 = new Specialty("Pediatrics");
        s1.setId(1L);
        s1.setDescription("Child care");

        when(specialtyService.getAllSpecialties()).thenReturn(List.of(s1));

        mockMvc.perform(get("/api/specialties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Pediatrics"));
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/specialties - Duplicate Resource 409 CONFLICT")
    void testCreateSpecialty_Duplicate() throws Exception {
        SpecialtyRequest request = new SpecialtyRequest("Pediatrics", "Child care");

        when(specialtyService.createSpecialty(any(), any()))
                .thenThrow(new DuplicateResourceException("Specialty with name 'Pediatrics' already exists"));

        mockMvc.perform(post("/api/specialties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Duplicate Resource"));
    }

    @Test
    @DisplayName("GET /api/specialties/{id} - Not Found 404 NOT_FOUND")
    void testGetSpecialtyById_NotFound() throws Exception {
        when(specialtyService.getSpecialtyById(888L))
                .thenThrow(ResourceNotFoundException.of("Specialty", 888L));

        mockMvc.perform(get("/api/specialties/888"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"));
    }
}
