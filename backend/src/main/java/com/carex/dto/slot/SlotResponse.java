package com.carex.dto.slot;

import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.SlotStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public class SlotResponse {

    private Long id;
    private Long doctorId;
    private String doctorName;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private AppointmentMode mode;
    private SlotStatus status;

    public SlotResponse() {}

    public SlotResponse(Long id, Long doctorId, String doctorName, LocalDate slotDate,
                        LocalTime startTime, LocalTime endTime,
                        AppointmentMode mode, SlotStatus status) {
        this.id = id;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.slotDate = slotDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.mode = mode;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
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

    public SlotStatus getStatus() {
        return status;
    }

    public void setStatus(SlotStatus status) {
        this.status = status;
    }
}
