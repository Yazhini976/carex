package com.carex.dto.intelligence;

public class SpecialtyRecommendationResponse {

    private Long recommendedSpecialtyId;
    private String recommendedSpecialty;
    private String reason;
    private double confidence;
    private String disclaimer;

    public SpecialtyRecommendationResponse() {
        this.disclaimer = "This recommendation is an appointment-navigation aid only and does NOT constitute medical diagnosis or clinical advice.";
    }

    public SpecialtyRecommendationResponse(Long recommendedSpecialtyId, String recommendedSpecialty, String reason, double confidence) {
        this.recommendedSpecialtyId = recommendedSpecialtyId;
        this.recommendedSpecialty = recommendedSpecialty;
        this.reason = reason;
        this.confidence = confidence;
        this.disclaimer = "This recommendation is an appointment-navigation aid only and does NOT constitute medical diagnosis or clinical advice.";
    }

    public Long getRecommendedSpecialtyId() {
        return recommendedSpecialtyId;
    }

    public void setRecommendedSpecialtyId(Long recommendedSpecialtyId) {
        this.recommendedSpecialtyId = recommendedSpecialtyId;
    }

    public String getRecommendedSpecialty() {
        return recommendedSpecialty;
    }

    public void setRecommendedSpecialty(String recommendedSpecialty) {
        this.recommendedSpecialty = recommendedSpecialty;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
