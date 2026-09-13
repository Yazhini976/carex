package com.carex.dto.waitlist;

import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.WaitlistStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WaitlistResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private Long specialtyId;
    private String specialtyName;
    private Long preferredDoctorId;
    private String preferredDoctorName;
    private LocalDate preferredDate;
    private AppointmentMode preferredMode;
    private WaitlistStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime fulfilledAt;

    public WaitlistResponse() {}

    public WaitlistResponse(Long id, Long patientId, String patientName, Long specialtyId,
                            String specialtyName, Long preferredDoctorId, String preferredDoctorName,
                            LocalDate preferredDate, AppointmentMode preferredMode,
                            WaitlistStatus status, LocalDateTime createdAt, LocalDateTime fulfilledAt) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.specialtyId = specialtyId;
        this.specialtyName = specialtyName;
        this.preferredDoctorId = preferredDoctorId;
        this.preferredDoctorName = preferredDoctorName;
        this.preferredDate = preferredDate;
        this.preferredMode = preferredMode;
        this.status = status;
        this.createdAt = createdAt;
        this.fulfilledAt = fulfilledAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Long getSpecialtyId() {
        return specialtyId;
    }

    public void setSpecialtyId(Long specialtyId) {
        this.specialtyId = specialtyId;
    }

    public String getSpecialtyName() {
        return specialtyName;
    }

    public void setSpecialtyName(String specialtyName) {
        this.specialtyName = specialtyName;
    }

    public Long getPreferredDoctorId() {
        return preferredDoctorId;
    }

    public void setPreferredDoctorId(Long preferredDoctorId) {
        this.preferredDoctorId = preferredDoctorId;
    }

    public String getPreferredDoctorName() {
        return preferredDoctorName;
    }

    public void setPreferredDoctorName(String preferredDoctorName) {
        this.preferredDoctorName = preferredDoctorName;
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

    public WaitlistStatus getStatus() {
        return status;
    }

    public void setStatus(WaitlistStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getFulfilledAt() {
        return fulfilledAt;
    }

    public void setFulfilledAt(LocalDateTime fulfilledAt) {
        this.fulfilledAt = fulfilledAt;
    }
}
