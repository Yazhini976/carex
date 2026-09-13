package com.carex.dto.doctor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class DoctorRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "License number is required")
    @Size(max = 50, message = "License number cannot exceed 50 characters")
    private String licenseNumber;

    @Size(max = 200, message = "Qualification cannot exceed 200 characters")
    private String qualification;

    @PositiveOrZero(message = "Experience years must be zero or positive")
    private Integer experienceYears;

    @DecimalMin(value = "0.0", inclusive = true, message = "Consultation fee must be positive or zero")
    private BigDecimal consultationFee;

    public DoctorRequest() {}

    public DoctorRequest(Long userId, String licenseNumber, String qualification,
                         Integer experienceYears, BigDecimal consultationFee) {
        this.userId = userId;
        this.licenseNumber = licenseNumber;
        this.qualification = qualification;
        this.experienceYears = experienceYears;
        this.consultationFee = consultationFee;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }
}
