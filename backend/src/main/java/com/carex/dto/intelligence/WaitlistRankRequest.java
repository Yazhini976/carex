package com.carex.dto.intelligence;

import com.carex.entity.enums.AppointmentMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class WaitlistRankRequest {

    @NotNull(message = "Specialty ID is required")
    private Long specialtyId;

    private Long doctorId;
    private AppointmentMode mode;
    private LocalDate date;

    public WaitlistRankRequest() {}

    public WaitlistRankRequest(Long specialtyId, Long doctorId, AppointmentMode mode, LocalDate date) {
        this.specialtyId = specialtyId;
        this.doctorId = doctorId;
        this.mode = mode;
        this.date = date;
    }

    public Long getSpecialtyId() { return specialtyId; }
    public void setSpecialtyId(Long specialtyId) { this.specialtyId = specialtyId; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public AppointmentMode getMode() { return mode; }
    public void setMode(AppointmentMode mode) { this.mode = mode; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
