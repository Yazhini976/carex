package com.carex.mapper;

import com.carex.dto.doctor.DoctorResponse;
import com.carex.dto.specialty.SpecialtyResponse;
import com.carex.entity.Doctor;
import com.carex.entity.Specialty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class DoctorMapper {

    private final SpecialtyMapper specialtyMapper;

    public DoctorMapper(SpecialtyMapper specialtyMapper) {
        this.specialtyMapper = specialtyMapper;
    }

    public DoctorResponse toResponse(Doctor doctor) {
        return toResponse(doctor, Collections.emptyList());
    }

    public DoctorResponse toResponse(Doctor doctor, List<Specialty> specialties) {
        if (doctor == null) {
            return null;
        }

        Long userId = doctor.getUser() != null ? doctor.getUser().getId() : null;
        String name = doctor.getUser() != null ? doctor.getUser().getName() : null;
        String email = doctor.getUser() != null ? doctor.getUser().getEmail() : null;
        String phone = doctor.getUser() != null ? doctor.getUser().getPhone() : null;

        List<SpecialtyResponse> specialtyResponses = specialties != null
                ? specialtyMapper.toResponseList(specialties)
                : new ArrayList<>();

        return new DoctorResponse(
                doctor.getId(),
                userId,
                name,
                email,
                phone,
                doctor.getLicenseNumber(),
                doctor.getQualification(),
                doctor.getExperienceYears(),
                doctor.getConsultationFee(),
                Boolean.TRUE.equals(doctor.getIsActive()),
                doctor.getCreatedAt(),
                specialtyResponses
        );
    }

    public List<DoctorResponse> toResponseList(List<Doctor> doctors) {
        if (doctors == null) {
            return Collections.emptyList();
        }
        return doctors.stream().map(this::toResponse).toList();
    }
}
