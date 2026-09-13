package com.carex.mapper;

import com.carex.dto.waitlist.WaitlistResponse;
import com.carex.entity.Waitlist;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class WaitlistMapper {

    public WaitlistResponse toResponse(Waitlist waitlist) {
        if (waitlist == null) {
            return null;
        }

        Long patientId = waitlist.getPatient() != null ? waitlist.getPatient().getId() : null;
        String patientName = (waitlist.getPatient() != null && waitlist.getPatient().getUser() != null)
                ? waitlist.getPatient().getUser().getName()
                : null;

        Long specialtyId = waitlist.getSpecialty() != null ? waitlist.getSpecialty().getId() : null;
        String specialtyName = waitlist.getSpecialty() != null ? waitlist.getSpecialty().getName() : null;

        Long doctorId = waitlist.getPreferredDoctor() != null ? waitlist.getPreferredDoctor().getId() : null;
        String doctorName = (waitlist.getPreferredDoctor() != null && waitlist.getPreferredDoctor().getUser() != null)
                ? waitlist.getPreferredDoctor().getUser().getName()
                : null;

        return new WaitlistResponse(
                waitlist.getId(),
                patientId,
                patientName,
                specialtyId,
                specialtyName,
                doctorId,
                doctorName,
                waitlist.getPreferredDate(),
                waitlist.getPreferredMode(),
                waitlist.getStatus(),
                waitlist.getCreatedAt(),
                waitlist.getFulfilledAt()
        );
    }

    public List<WaitlistResponse> toResponseList(List<Waitlist> waitlistEntries) {
        if (waitlistEntries == null) {
            return Collections.emptyList();
        }
        return waitlistEntries.stream().map(this::toResponse).toList();
    }
}
