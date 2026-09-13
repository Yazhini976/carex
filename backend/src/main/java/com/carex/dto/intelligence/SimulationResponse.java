package com.carex.dto.intelligence;

import com.carex.service.intelligence.SchedulingSimulationService.ScenarioType;

import java.time.LocalDate;
import java.util.List;

public class SimulationResponse {

    private Long doctorId;
    private String doctorName;
    private LocalDate date;
    private ScenarioType scenario;
    private int affectedAppointmentCount;
    private List<Long> affectedAppointmentIds;
    private List<Long> candidateAlternativeDoctorIds;
    private List<String> candidateAlternativeDoctorNames;
    private String workloadImpactSummary;
    private String redistributionSuggestion;
    private boolean readOnly;

    public SimulationResponse() {
        this.readOnly = true;
    }

    public SimulationResponse(Long doctorId, String doctorName, LocalDate date, ScenarioType scenario,
                              int affectedAppointmentCount, List<Long> affectedAppointmentIds,
                              List<Long> candidateAlternativeDoctorIds, List<String> candidateAlternativeDoctorNames,
                              String workloadImpactSummary, String redistributionSuggestion, boolean readOnly) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.date = date;
        this.scenario = scenario;
        this.affectedAppointmentCount = affectedAppointmentCount;
        this.affectedAppointmentIds = affectedAppointmentIds;
        this.candidateAlternativeDoctorIds = candidateAlternativeDoctorIds;
        this.candidateAlternativeDoctorNames = candidateAlternativeDoctorNames;
        this.workloadImpactSummary = workloadImpactSummary;
        this.redistributionSuggestion = redistributionSuggestion;
        this.readOnly = readOnly;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public ScenarioType getScenario() {
        return scenario;
    }

    public void setScenario(ScenarioType scenario) {
        this.scenario = scenario;
    }

    public int getAffectedAppointmentCount() {
        return affectedAppointmentCount;
    }

    public void setAffectedAppointmentCount(int affectedAppointmentCount) {
        this.affectedAppointmentCount = affectedAppointmentCount;
    }

    public List<Long> getAffectedAppointmentIds() {
        return affectedAppointmentIds;
    }

    public void setAffectedAppointmentIds(List<Long> affectedAppointmentIds) {
        this.affectedAppointmentIds = affectedAppointmentIds;
    }

    public List<Long> getCandidateAlternativeDoctorIds() {
        return candidateAlternativeDoctorIds;
    }

    public void setCandidateAlternativeDoctorIds(List<Long> candidateAlternativeDoctorIds) {
        this.candidateAlternativeDoctorIds = candidateAlternativeDoctorIds;
    }

    public List<String> getCandidateAlternativeDoctorNames() {
        return candidateAlternativeDoctorNames;
    }

    public void setCandidateAlternativeDoctorNames(List<String> candidateAlternativeDoctorNames) {
        this.candidateAlternativeDoctorNames = candidateAlternativeDoctorNames;
    }

    public String getWorkloadImpactSummary() {
        return workloadImpactSummary;
    }

    public void setWorkloadImpactSummary(String workloadImpactSummary) {
        this.workloadImpactSummary = workloadImpactSummary;
    }

    public String getRedistributionSuggestion() {
        return redistributionSuggestion;
    }

    public void setRedistributionSuggestion(String redistributionSuggestion) {
        this.redistributionSuggestion = redistributionSuggestion;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
