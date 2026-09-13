package com.carex.mapper;

import com.carex.dto.patient.PatientResponse;
import com.carex.entity.Patient;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class PatientMapper {

    public PatientResponse toResponse(Patient patient) {
        if (patient == null) {
            return null;
        }

        Long userId = patient.getUser() != null ? patient.getUser().getId() : null;
        String name = patient.getUser() != null ? patient.getUser().getName() : null;
        String email = patient.getUser() != null ? patient.getUser().getEmail() : null;
        String phone = patient.getUser() != null ? patient.getUser().getPhone() : null;

        return new PatientResponse(
                patient.getId(),
                userId,
                name,
                email,
                phone,
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getEmergencyContact(),
                patient.getCreatedAt()
        );
    }

    public List<PatientResponse> toResponseList(List<Patient> patients) {
        if (patients == null) {
            return Collections.emptyList();
        }
        return patients.stream().map(this::toResponse).toList();
    }
}
