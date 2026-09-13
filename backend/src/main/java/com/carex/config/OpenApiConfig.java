package com.carex.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI carexOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CAREX — Intelligent Doctor Appointment & Patient Flow Management System API")
                        .description("REST API specifications for the CAREX healthcare backend. "
                                + "Provides endpoints for JWT authentication, user management, patient registration, "
                                + "doctor profiles, availability scheduling, slot generation, appointment workflows, "
                                + "dynamic waitlist management, notification delivery, AI-assisted specialty navigation, "
                                + "doctor matching, workload intelligence, and read-only schedule simulations.\n\n"
                                + "### Authentication\n"
                                + "1. Obtain a JWT token via `/api/auth/login` or `/api/auth/register`.\n"
                                + "2. Click **Authorize** button at top right.\n"
                                + "3. Enter the token in format: `Bearer <your_token>` or simply `<your_token>`.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CAREX Engineering Team")
                                .email("support@carex.health"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://carex.health")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token to authorize protected API requests.")))
                .tags(List.of(
                        new Tag().name("Authentication").description("Registration and login operations"),
                        new Tag().name("Users").description("User profile and account operations"),
                        new Tag().name("Patients").description("Patient registration and profile management"),
                        new Tag().name("Doctors").description("Doctor profile, qualifications, and search"),
                        new Tag().name("Specialties").description("Medical specialties management"),
                        new Tag().name("Doctor Specialties").description("Doctor-to-specialty relationship management"),
                        new Tag().name("Availability").description("Doctor working schedule patterns"),
                        new Tag().name("Slots").description("Time slot management and generation"),
                        new Tag().name("Appointments").description("Appointment booking, status transitions, and queries"),
                        new Tag().name("Waitlist").description("Smart patient waitlist queue management"),
                        new Tag().name("Notifications").description("User and system notification queue"),
                        new Tag().name("Intelligence").description("AI-assisted navigation, matching, wait-time prediction, and simulation"),
                        new Tag().name("Analytics").description("Operational analytics, daily summaries, and workload metrics")
                ));
    }
}
