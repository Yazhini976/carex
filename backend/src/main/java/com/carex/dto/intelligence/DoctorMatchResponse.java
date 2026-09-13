package com.carex.dto.intelligence;

import java.math.BigDecimal;

public class DoctorMatchResponse {

    private Long doctorId;
    private String doctorName;
    private String qualification;
    private Integer experienceYears;
    private BigDecimal consultationFee;
    private Long specialtyId;
    private String specialtyName;
    private boolean specialtyMatch;
    private boolean modeMatch;
    private boolean hasAvailableSlot;
    private Integer estimatedWaitMinutes;
    private double score;
    private String reasoning;

    public DoctorMatchResponse() {}

    public DoctorMatchResponse(Long doctorId, String doctorName, String qualification,
                               Integer experienceYears, BigDecimal consultationFee,
                               Long specialtyId, String specialtyName,
                               boolean specialtyMatch, boolean modeMatch, boolean hasAvailableSlot,
                               Integer estimatedWaitMinutes, double score, String reasoning) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.qualification = qualification;
        this.experienceYears = experienceYears;
        this.consultationFee = consultationFee;
        this.specialtyId = specialtyId;
        this.specialtyName = specialtyName;
        this.specialtyMatch = specialtyMatch;
        this.modeMatch = modeMatch;
        this.hasAvailableSlot = hasAvailableSlot;
        this.estimatedWaitMinutes = estimatedWaitMinutes;
        this.score = score;
        this.reasoning = reasoning;
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

    public boolean isSpecialtyMatch() {
        return specialtyMatch;
    }

    public void setSpecialtyMatch(boolean specialtyMatch) {
        this.specialtyMatch = specialtyMatch;
    }

    public boolean isModeMatch() {
        return modeMatch;
    }

    public void setModeMatch(boolean modeMatch) {
        this.modeMatch = modeMatch;
    }

    public boolean isHasAvailableSlot() {
        return hasAvailableSlot;
    }

    public void setHasAvailableSlot(boolean hasAvailableSlot) {
        this.hasAvailableSlot = hasAvailableSlot;
    }

    public Integer getEstimatedWaitMinutes() {
        return estimatedWaitMinutes;
    }

    public void setEstimatedWaitMinutes(Integer estimatedWaitMinutes) {
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }
}
