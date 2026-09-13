package com.carex.mapper;

import com.carex.dto.appointment.AppointmentResponse;
import com.carex.entity.Appointment;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AppointmentMapper {

    public AppointmentResponse toResponse(Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        AppointmentResponse response = new AppointmentResponse();
        response.setAppointmentId(appointment.getId());

        // Patient details
        if (appointment.getPatient() != null) {
            response.setPatientId(appointment.getPatient().getId());
            if (appointment.getPatient().getUser() != null) {
                response.setPatientName(appointment.getPatient().getUser().getName());
                response.setPatientEmail(appointment.getPatient().getUser().getEmail());
                response.setPatientPhone(appointment.getPatient().getUser().getPhone());
            }
        }

        // Doctor details
        if (appointment.getDoctor() != null) {
            response.setDoctorId(appointment.getDoctor().getId());
            response.setDoctorLicenseNumber(appointment.getDoctor().getLicenseNumber());
            response.setDoctorQualification(appointment.getDoctor().getQualification());
            response.setDoctorConsultationFee(appointment.getDoctor().getConsultationFee());
            if (appointment.getDoctor().getUser() != null) {
                response.setDoctorName(appointment.getDoctor().getUser().getName());
            }
        }

        // Specialty details
        if (appointment.getSpecialty() != null) {
            response.setSpecialtyId(appointment.getSpecialty().getId());
            response.setSpecialtyName(appointment.getSpecialty().getName());
        }

        // Slot details
        if (appointment.getSlot() != null) {
            response.setSlotId(appointment.getSlot().getId());
            response.setSlotDate(appointment.getSlot().getSlotDate());
            response.setSlotStartTime(appointment.getSlot().getStartTime());
            response.setSlotEndTime(appointment.getSlot().getEndTime());
        }

        response.setMode(appointment.getMode());
        response.setStatus(appointment.getStatus());
        response.setBookedAt(appointment.getBookedAt());
        response.setCancelledAt(appointment.getCancelledAt());
        response.setCompletedAt(appointment.getCompletedAt());
        response.setNotes(appointment.getNotes());

        return response;
    }

    public List<AppointmentResponse> toResponseList(List<Appointment> appointments) {
        if (appointments == null) {
            return Collections.emptyList();
        }
        return appointments.stream().map(this::toResponse).toList();
    }
}
