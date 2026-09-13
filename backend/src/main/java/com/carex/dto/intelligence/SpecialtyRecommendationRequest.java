package com.carex.dto.intelligence;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SpecialtyRecommendationRequest {

    private Long patientId;

    @NotBlank(message = "Input text is required for specialty recommendation")
    @Size(min = 3, max = 2000, message = "Input text must be between 3 and 2000 characters")
    private String inputText;

    public SpecialtyRecommendationRequest() {}

    public SpecialtyRecommendationRequest(Long patientId, String inputText) {
        this.patientId = patientId;
        this.inputText = inputText;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }
}
