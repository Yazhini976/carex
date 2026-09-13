package com.carex.dto.doctor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class AdminDoctorRequest {

    // ---------- User account fields ----------
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String phone;

    // ---------- Doctor profile fields ----------
    @PositiveOrZero
    private Integer experienceYears;

    private BigDecimal consultationFee;

    private String qualification;

    private Long specialtyId;

    private Boolean allowsOnline = true;
    private Boolean allowsOffline = true;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public Long getSpecialtyId() { return specialtyId; }
    public void setSpecialtyId(Long specialtyId) { this.specialtyId = specialtyId; }

    public Boolean getAllowsOnline() { return allowsOnline; }
    public void setAllowsOnline(Boolean allowsOnline) { this.allowsOnline = allowsOnline; }

    public Boolean getAllowsOffline() { return allowsOffline; }
    public void setAllowsOffline(Boolean allowsOffline) { this.allowsOffline = allowsOffline; }
}
