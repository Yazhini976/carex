package com.carex.dto.waitlist;

import com.carex.entity.enums.AppointmentMode;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class WaitlistRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Specialty ID is required")
    private Long specialtyId;

    private Long preferredDoctorId;

    @FutureOrPresent(message = "Preferred date cannot be in the past")
    private LocalDate preferredDate;

    private AppointmentMode preferredMode;

    public WaitlistRequest() {}

    public WaitlistRequest(Long patientId, Long specialtyId, Long preferredDoctorId,
                           LocalDate preferredDate, AppointmentMode preferredMode) {
        this.patientId = patientId;
        this.specialtyId = specialtyId;
        this.preferredDoctorId = preferredDoctorId;
        this.preferredDate = preferredDate;
        this.preferredMode = preferredMode;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getSpecialtyId() {
        return specialtyId;
    }

    public void setSpecialtyId(Long specialtyId) {
        this.specialtyId = specialtyId;
    }

    public Long getPreferredDoctorId() {
        return preferredDoctorId;
    }

    public void setPreferredDoctorId(Long preferredDoctorId) {
        this.preferredDoctorId = preferredDoctorId;
    }

    public LocalDate getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(LocalDate preferredDate) {
        this.preferredDate = preferredDate;
    }

    public AppointmentMode getPreferredMode() {
        return preferredMode;
    }

    public void setPreferredMode(AppointmentMode preferredMode) {
        this.preferredMode = preferredMode;
    }
}
