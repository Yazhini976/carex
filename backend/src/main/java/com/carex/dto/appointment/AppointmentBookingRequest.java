package com.carex.dto.appointment;

import com.carex.entity.enums.AppointmentMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AppointmentBookingRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Specialty ID is required")
    private Long specialtyId;

    @NotNull(message = "Slot ID is required")
    private Long slotId;

    @NotNull(message = "Appointment mode is required")
    private AppointmentMode mode;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    public AppointmentBookingRequest() {}

    public AppointmentBookingRequest(Long patientId, Long doctorId, Long specialtyId,
                                     Long slotId, AppointmentMode mode, String notes) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.specialtyId = specialtyId;
        this.slotId = slotId;
        this.mode = mode;
        this.notes = notes;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Long getSpecialtyId() {
        return specialtyId;
    }

    public void setSpecialtyId(Long specialtyId) {
        this.specialtyId = specialtyId;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public AppointmentMode getMode() {
        return mode;
    }

    public void setMode(AppointmentMode mode) {
        this.mode = mode;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
