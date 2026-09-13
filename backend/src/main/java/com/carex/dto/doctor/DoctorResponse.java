package com.carex.dto.doctor;

import com.carex.dto.specialty.SpecialtyResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DoctorResponse {

    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String licenseNumber;
    private String qualification;
    private Integer experienceYears;
    private BigDecimal consultationFee;
    private boolean active;
    private LocalDateTime createdAt;
    private List<SpecialtyResponse> specialties = new ArrayList<>();

    public DoctorResponse() {}

    public DoctorResponse(Long id, Long userId, String name, String email, String phone,
                          String licenseNumber, String qualification, Integer experienceYears,
                          BigDecimal consultationFee, boolean active, LocalDateTime createdAt,
                          List<SpecialtyResponse> specialties) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.licenseNumber = licenseNumber;
        this.qualification = qualification;
        this.experienceYears = experienceYears;
        this.consultationFee = consultationFee;
        this.active = active;
        this.createdAt = createdAt;
        if (specialties != null) {
            this.specialties = specialties;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<SpecialtyResponse> getSpecialties() {
        return specialties;
    }

    public void setSpecialties(List<SpecialtyResponse> specialties) {
        this.specialties = specialties;
    }
}
