package com.carex.dto.intelligence;

import java.time.LocalDate;
import java.util.List;

public class WaitlistRankResponse {

    private Long waitlistId;
    private Long patientId;
    private String patientName;
    private Long specialtyId;
    private String specialtyName;
    private Long preferredDoctorId;
    private String preferredDoctorName;
    private LocalDate preferredDate;
    private String preferredMode;
    private double score;
    private List<String> reasons;
    private int rank;

    public WaitlistRankResponse() {}

    public WaitlistRankResponse(Long waitlistId, Long patientId, String patientName, Long specialtyId,
                                String specialtyName, Long preferredDoctorId, String preferredDoctorName,
                                LocalDate preferredDate, String preferredMode, double score,
                                List<String> reasons, int rank) {
        this.waitlistId = waitlistId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.specialtyId = specialtyId;
        this.specialtyName = specialtyName;
        this.preferredDoctorId = preferredDoctorId;
        this.preferredDoctorName = preferredDoctorName;
        this.preferredDate = preferredDate;
        this.preferredMode = preferredMode;
        this.score = score;
        this.reasons = reasons;
        this.rank = rank;
    }

    public Long getWaitlistId() { return waitlistId; }
    public void setWaitlistId(Long waitlistId) { this.waitlistId = waitlistId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public Long getSpecialtyId() { return specialtyId; }
    public void setSpecialtyId(Long specialtyId) { this.specialtyId = specialtyId; }
    public String getSpecialtyName() { return specialtyName; }
    public void setSpecialtyName(String specialtyName) { this.specialtyName = specialtyName; }
    public Long getPreferredDoctorId() { return preferredDoctorId; }
    public void setPreferredDoctorId(Long preferredDoctorId) { this.preferredDoctorId = preferredDoctorId; }
    public String getPreferredDoctorName() { return preferredDoctorName; }
    public void setPreferredDoctorName(String preferredDoctorName) { this.preferredDoctorName = preferredDoctorName; }
    public LocalDate getPreferredDate() { return preferredDate; }
    public void setPreferredDate(LocalDate preferredDate) { this.preferredDate = preferredDate; }
    public String getPreferredMode() { return preferredMode; }
    public void setPreferredMode(String preferredMode) { this.preferredMode = preferredMode; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public List<String> getReasons() { return reasons; }
    public void setReasons(List<String> reasons) { this.reasons = reasons; }
    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
}
