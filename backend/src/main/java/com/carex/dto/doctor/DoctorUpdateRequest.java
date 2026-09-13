package com.carex.dto.doctor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class DoctorUpdateRequest {

    @Size(max = 200, message = "Qualification cannot exceed 200 characters")
    private String qualification;

    @PositiveOrZero(message = "Experience years must be zero or positive")
    private Integer experienceYears;

    @DecimalMin(value = "0.0", inclusive = true, message = "Consultation fee must be positive or zero")
    private BigDecimal consultationFee;

    private Boolean active;

    public DoctorUpdateRequest() {}

    public DoctorUpdateRequest(String qualification, Integer experienceYears, BigDecimal consultationFee, Boolean active) {
        this.qualification = qualification;
        this.experienceYears = experienceYears;
        this.consultationFee = consultationFee;
        this.active = active;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
