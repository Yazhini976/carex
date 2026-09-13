package com.carex.dto.intelligence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class RecoveryResponse {

    private Long unavailableDoctorId;
    private String unavailableDoctorName;
    private LocalDate date;
    private int affectedAppointmentsCount;
    private List<PatientRecoverySuggestion> patientSuggestions;
    private List<AlternativeDoctorSummary> alternativeDoctors;
    private String summaryRecommendation;
    private boolean readOnly;

    public static class PatientRecoverySuggestion {
        private Long appointmentId;
        private Long patientId;
        private String patientName;
        private LocalTime originalTime;
        private Long suggestedDoctorId;
        private String suggestedDoctorName;
        private LocalTime suggestedTime;
        private String specialtyName;
        private String mode;
        private String rationale;

        public PatientRecoverySuggestion() {}

        public PatientRecoverySuggestion(Long appointmentId, Long patientId, String patientName,
                                         LocalTime originalTime, Long suggestedDoctorId, String suggestedDoctorName,
                                         LocalTime suggestedTime, String specialtyName, String mode, String rationale) {
            this.appointmentId = appointmentId;
            this.patientId = patientId;
            this.patientName = patientName;
            this.originalTime = originalTime;
            this.suggestedDoctorId = suggestedDoctorId;
            this.suggestedDoctorName = suggestedDoctorName;
            this.suggestedTime = suggestedTime;
            this.specialtyName = specialtyName;
            this.mode = mode;
            this.rationale = rationale;
        }

        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
        public Long getPatientId() { return patientId; }
        public void setPatientId(Long patientId) { this.patientId = patientId; }
        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }
        public LocalTime getOriginalTime() { return originalTime; }
        public void setOriginalTime(LocalTime originalTime) { this.originalTime = originalTime; }
        public Long getSuggestedDoctorId() { return suggestedDoctorId; }
        public void setSuggestedDoctorId(Long suggestedDoctorId) { this.suggestedDoctorId = suggestedDoctorId; }
        public String getSuggestedDoctorName() { return suggestedDoctorName; }
        public void setSuggestedDoctorName(String suggestedDoctorName) { this.suggestedDoctorName = suggestedDoctorName; }
        public LocalTime getSuggestedTime() { return suggestedTime; }
        public void setSuggestedTime(LocalTime suggestedTime) { this.suggestedTime = suggestedTime; }
        public String getSpecialtyName() { return specialtyName; }
        public void setSpecialtyName(String specialtyName) { this.specialtyName = specialtyName; }
        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
        public String getRationale() { return rationale; }
        public void setRationale(String rationale) { this.rationale = rationale; }
    }

    public static class AlternativeDoctorSummary {
        private Long doctorId;
        private String doctorName;
        private String primarySpecialty;
        private int availableCapacity;

        public AlternativeDoctorSummary() {}

        public AlternativeDoctorSummary(Long doctorId, String doctorName, String primarySpecialty, int availableCapacity) {
            this.doctorId = doctorId;
            this.doctorName = doctorName;
            this.primarySpecialty = primarySpecialty;
            this.availableCapacity = availableCapacity;
        }

        public Long getDoctorId() { return doctorId; }
        public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
        public String getDoctorName() { return doctorName; }
        public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
        public String getPrimarySpecialty() { return primarySpecialty; }
        public void setPrimarySpecialty(String primarySpecialty) { this.primarySpecialty = primarySpecialty; }
        public int getAvailableCapacity() { return availableCapacity; }
        public void setAvailableCapacity(int availableCapacity) { this.availableCapacity = availableCapacity; }
    }

    public RecoveryResponse() {
        this.readOnly = true;
    }

    public RecoveryResponse(Long unavailableDoctorId, String unavailableDoctorName, LocalDate date,
                            int affectedAppointmentsCount, List<PatientRecoverySuggestion> patientSuggestions,
                            List<AlternativeDoctorSummary> alternativeDoctors, String summaryRecommendation) {
        this.unavailableDoctorId = unavailableDoctorId;
        this.unavailableDoctorName = unavailableDoctorName;
        this.date = date;
        this.affectedAppointmentsCount = affectedAppointmentsCount;
        this.patientSuggestions = patientSuggestions;
        this.alternativeDoctors = alternativeDoctors;
        this.summaryRecommendation = summaryRecommendation;
        this.readOnly = true;
    }

    public Long getUnavailableDoctorId() { return unavailableDoctorId; }
    public void setUnavailableDoctorId(Long unavailableDoctorId) { this.unavailableDoctorId = unavailableDoctorId; }
    public String getUnavailableDoctorName() { return unavailableDoctorName; }
    public void setUnavailableDoctorName(String unavailableDoctorName) { this.unavailableDoctorName = unavailableDoctorName; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public int getAffectedAppointmentsCount() { return affectedAppointmentsCount; }
    public void setAffectedAppointmentsCount(int affectedAppointmentsCount) { this.affectedAppointmentsCount = affectedAppointmentsCount; }
    public List<PatientRecoverySuggestion> getPatientSuggestions() { return patientSuggestions; }
    public void setPatientSuggestions(List<PatientRecoverySuggestion> patientSuggestions) { this.patientSuggestions = patientSuggestions; }
    public List<AlternativeDoctorSummary> getAlternativeDoctors() { return alternativeDoctors; }
    public void setAlternativeDoctors(List<AlternativeDoctorSummary> alternativeDoctors) { this.alternativeDoctors = alternativeDoctors; }
    public String getSummaryRecommendation() { return summaryRecommendation; }
    public void setSummaryRecommendation(String summaryRecommendation) { this.summaryRecommendation = summaryRecommendation; }
    public boolean isReadOnly() { return readOnly; }
    public void setReadOnly(boolean readOnly) { this.readOnly = readOnly; }
}
