package com.carex.dto.slot;

import com.carex.entity.enums.AppointmentMode;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class SlotCreateRequest {

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Slot date is required")
    @FutureOrPresent(message = "Slot date cannot be in the past")
    private LocalDate slotDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Mode is required")
    private AppointmentMode mode;

    public SlotCreateRequest() {}

    public SlotCreateRequest(Long doctorId, LocalDate slotDate, LocalTime startTime, LocalTime endTime, AppointmentMode mode) {
        this.doctorId = doctorId;
        this.slotDate = slotDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.mode = mode;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getSlotDate() {
        return slotDate;
    }

    public void setSlotDate(LocalDate slotDate) {
        this.slotDate = slotDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public AppointmentMode getMode() {
        return mode;
    }

    public void setMode(AppointmentMode mode) {
        this.mode = mode;
    }
}
