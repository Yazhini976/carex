package com.carex.dto.intelligence;

import com.carex.service.intelligence.SchedulingSimulationService.ScenarioType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class SimulationRequest {

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private ScenarioType scenario;

    public SimulationRequest() {}

    public SimulationRequest(Long doctorId, LocalDate date, ScenarioType scenario) {
        this.doctorId = doctorId;
        this.date = date;
        this.scenario = scenario;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
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
}
