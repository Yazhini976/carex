package com.carex.dto.availability;

import com.carex.entity.enums.AppointmentMode;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class DoctorAvailabilityResponse {

    private Long id;
    private Long doctorId;
    private String doctorName;
    private Integer dayOfWeek;
    private String dayOfWeekName;
    private LocalTime startTime;
    private LocalTime endTime;
    private AppointmentMode mode;
    private boolean active;

    public DoctorAvailabilityResponse() {}

    public DoctorAvailabilityResponse(Long id, Long doctorId, String doctorName, Integer dayOfWeek,
                                      LocalTime startTime, LocalTime endTime,
                                      AppointmentMode mode, boolean active) {
        this.id = id;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.dayOfWeek = dayOfWeek;
        this.dayOfWeekName = computeDayName(dayOfWeek);
        this.startTime = startTime;
        this.endTime = endTime;
        this.mode = mode;
        this.active = active;
    }

    private static String computeDayName(Integer dayOfWeek) {
        if (dayOfWeek == null) return null;
        return switch (dayOfWeek) {
            case 0 -> "Sunday";
            case 1 -> "Monday";
            case 2 -> "Tuesday";
            case 3 -> "Wednesday";
            case 4 -> "Thursday";
            case 5 -> "Friday";
            case 6 -> "Saturday";
            default -> "Unknown";
        };
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

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
        this.dayOfWeekName = computeDayName(dayOfWeek);
    }

    public String getDayOfWeekName() {
        return dayOfWeekName;
    }

    public void setDayOfWeekName(String dayOfWeekName) {
        this.dayOfWeekName = dayOfWeekName;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
