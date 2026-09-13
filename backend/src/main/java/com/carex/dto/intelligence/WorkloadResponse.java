package com.carex.dto.intelligence;

public class WorkloadResponse {

    private Long doctorId;
    private String doctorName;
    private double currentScore;
    private String loadLevel;
    private String recommendation;
    private int appointmentCount;
    private int completedCount;
    private int cancelledCount;
    private int noShowCount;

    public WorkloadResponse() {}

    public WorkloadResponse(Long doctorId, String doctorName, double currentScore,
                            String loadLevel, String recommendation, int appointmentCount,
                            int completedCount, int cancelledCount, int noShowCount) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.currentScore = currentScore;
        this.loadLevel = loadLevel;
        this.recommendation = recommendation;
        this.appointmentCount = appointmentCount;
        this.completedCount = completedCount;
        this.cancelledCount = cancelledCount;
        this.noShowCount = noShowCount;
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

    public double getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(double currentScore) {
        this.currentScore = currentScore;
    }

    public String getLoadLevel() {
        return loadLevel;
    }

    public void setLoadLevel(String loadLevel) {
        this.loadLevel = loadLevel;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public int getAppointmentCount() {
        return appointmentCount;
    }

    public void setAppointmentCount(int appointmentCount) {
        this.appointmentCount = appointmentCount;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }

    public int getCancelledCount() {
        return cancelledCount;
    }

    public void setCancelledCount(int cancelledCount) {
        this.cancelledCount = cancelledCount;
    }

    public int getNoShowCount() {
        return noShowCount;
    }

    public void setNoShowCount(int noShowCount) {
        this.noShowCount = noShowCount;
    }
}
