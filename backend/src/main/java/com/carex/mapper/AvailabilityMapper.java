package com.carex.mapper;

import com.carex.dto.availability.DoctorAvailabilityResponse;
import com.carex.entity.DoctorAvailability;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AvailabilityMapper {

    public DoctorAvailabilityResponse toResponse(DoctorAvailability availability) {
        if (availability == null) {
            return null;
        }

        Long doctorId = availability.getDoctor() != null ? availability.getDoctor().getId() : null;
        String doctorName = (availability.getDoctor() != null && availability.getDoctor().getUser() != null)
                ? availability.getDoctor().getUser().getName()
                : null;

        return new DoctorAvailabilityResponse(
                availability.getId(),
                doctorId,
                doctorName,
                availability.getDayOfWeek(),
                availability.getStartTime(),
                availability.getEndTime(),
                availability.getMode(),
                Boolean.TRUE.equals(availability.getIsActive())
        );
    }

    public List<DoctorAvailabilityResponse> toResponseList(List<DoctorAvailability> availabilities) {
        if (availabilities == null) {
            return Collections.emptyList();
        }
        return availabilities.stream().map(this::toResponse).toList();
    }
}
