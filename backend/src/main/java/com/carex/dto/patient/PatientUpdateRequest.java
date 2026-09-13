package com.carex.dto.patient;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class PatientUpdateRequest {

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Size(max = 10, message = "Gender cannot exceed 10 characters")
    private String gender;

    @Size(max = 20, message = "Emergency contact cannot exceed 20 characters")
    private String emergencyContact;

    public PatientUpdateRequest() {}

    public PatientUpdateRequest(LocalDate dateOfBirth, String gender, String emergencyContact) {
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.emergencyContact = emergencyContact;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }
}
