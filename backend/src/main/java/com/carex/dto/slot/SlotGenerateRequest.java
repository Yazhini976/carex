package com.carex.dto.slot;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class SlotGenerateRequest {

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Availability ID is required")
    private Long availabilityId;

    @NotNull(message = "From date is required")
    private LocalDate fromDate;

    @NotNull(message = "To date is required")
    private LocalDate toDate;

    @Min(value = 5, message = "Slot duration must be at least 5 minutes")
    @Max(value = 120, message = "Slot duration cannot exceed 120 minutes")
    private int slotDurationMinutes = 15;

    public SlotGenerateRequest() {}

    public SlotGenerateRequest(Long doctorId, Long availabilityId, LocalDate fromDate, LocalDate toDate, int slotDurationMinutes) {
        this.doctorId = doctorId;
        this.availabilityId = availabilityId;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.slotDurationMinutes = slotDurationMinutes;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Long getAvailabilityId() {
        return availabilityId;
    }

    public void setAvailabilityId(Long availabilityId) {
        this.availabilityId = availabilityId;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public int getSlotDurationMinutes() {
        return slotDurationMinutes;
    }

    public void setSlotDurationMinutes(int slotDurationMinutes) {
        this.slotDurationMinutes = slotDurationMinutes;
    }
}
