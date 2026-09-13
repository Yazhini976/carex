package com.carex.dto.intelligence;

import com.carex.entity.enums.AppointmentMode;
import jakarta.validation.constraints.NotNull;

public class DoctorMatchRequest {

    @NotNull(message = "Specialty ID is required")
    private Long specialtyId;

    @NotNull(message = "Appointment mode is required")
    private AppointmentMode mode;

    public DoctorMatchRequest() {}

    public DoctorMatchRequest(Long specialtyId, AppointmentMode mode) {
        this.specialtyId = specialtyId;
        this.mode = mode;
    }

    public Long getSpecialtyId() {
        return specialtyId;
    }

    public void setSpecialtyId(Long specialtyId) {
        this.specialtyId = specialtyId;
    }

    public AppointmentMode getMode() {
        return mode;
    }

    public void setMode(AppointmentMode mode) {
        this.mode = mode;
    }
}
