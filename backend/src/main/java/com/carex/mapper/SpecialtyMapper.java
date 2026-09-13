package com.carex.mapper;

import com.carex.dto.specialty.SpecialtyResponse;
import com.carex.entity.Specialty;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SpecialtyMapper {

    public SpecialtyResponse toResponse(Specialty specialty) {
        if (specialty == null) {
            return null;
        }
        return new SpecialtyResponse(
                specialty.getId(),
                specialty.getName(),
                specialty.getDescription(),
                Boolean.TRUE.equals(specialty.getIsActive())
        );
    }

    public List<SpecialtyResponse> toResponseList(List<Specialty> specialties) {
        if (specialties == null) {
            return Collections.emptyList();
        }
        return specialties.stream().map(this::toResponse).toList();
    }
}
