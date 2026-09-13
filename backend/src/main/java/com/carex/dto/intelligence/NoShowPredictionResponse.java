package com.carex.dto.intelligence;

import java.util.ArrayList;
import java.util.List;

public class NoShowPredictionResponse {

    private Long appointmentId;
    private double noShowProbability;
    private String riskLevel; // LOW / MEDIUM / HIGH
    private String confidence;
    private String method;
    private List<String> basis = new ArrayList<>();

    public NoShowPredictionResponse() {}

    public NoShowPredictionResponse(Long appointmentId, double noShowProbability, String confidence, String method) {
        this.appointmentId = appointmentId;
        this.noShowProbability = noShowProbability;
        this.riskLevel = noShowProbability >= 0.6 ? "HIGH" : noShowProbability >= 0.25 ? "MEDIUM" : "LOW";
        this.confidence = confidence;
        this.method = method;
        this.basis = new ArrayList<>();
    }

    public NoShowPredictionResponse(Long appointmentId, double noShowProbability, String riskLevel,
                                    String confidence, String method, List<String> basis) {
        this.appointmentId = appointmentId;
        this.noShowProbability = noShowProbability;
        this.riskLevel = riskLevel;
        this.confidence = confidence;
        this.method = method;
        this.basis = basis != null ? basis : new ArrayList<>();
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public double getNoShowProbability() {
        return noShowProbability;
    }

    public void setNoShowProbability(double noShowProbability) {
        this.noShowProbability = noShowProbability;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<String> getBasis() {
        return basis;
    }

    public void setBasis(List<String> basis) {
        this.basis = basis;
    }
}
