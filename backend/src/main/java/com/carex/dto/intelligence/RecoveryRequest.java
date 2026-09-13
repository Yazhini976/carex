package com.carex.dto.intelligence;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class RecoveryRequest {

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    public RecoveryRequest() {}

    public RecoveryRequest(Long doctorId, LocalDate date) {
        this.doctorId = doctorId;
        this.date = date;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
