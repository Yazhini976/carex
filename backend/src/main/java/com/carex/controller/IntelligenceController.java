package com.carex.controller;

import com.carex.dto.intelligence.DoctorMatchRequest;
import com.carex.dto.intelligence.DoctorMatchResponse;
import com.carex.dto.intelligence.NoShowPredictionResponse;
import com.carex.dto.intelligence.RecoveryRequest;
import com.carex.dto.intelligence.RecoveryResponse;
import com.carex.dto.intelligence.SimulationRequest;
import com.carex.dto.intelligence.SimulationResponse;
import com.carex.dto.intelligence.SpecialtyRecommendationRequest;
import com.carex.dto.intelligence.SpecialtyRecommendationResponse;
import com.carex.dto.intelligence.WaitTimeResponse;
import com.carex.dto.intelligence.WaitlistRankRequest;
import com.carex.dto.intelligence.WaitlistRankResponse;
import com.carex.dto.intelligence.WorkloadResponse;
import com.carex.entity.Appointment;
import com.carex.entity.Doctor;
import com.carex.entity.Specialty;
import com.carex.service.RecommendationService;
import com.carex.service.WaitTimePredictionService;
import com.carex.service.intelligence.DisruptionRecoveryService;
import com.carex.service.intelligence.DoctorMatchingService;
import com.carex.service.intelligence.NoShowPredictionService;
import com.carex.service.intelligence.SchedulingSimulationService;
import com.carex.service.intelligence.SchedulingSimulationService.ScenarioType;
import com.carex.service.intelligence.SmartWaitlistService;
import com.carex.service.intelligence.SpecialtyRecommendationService;
import com.carex.service.intelligence.WaitTimeService;
import com.carex.service.intelligence.WorkloadIntelligenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/intelligence")
@Tag(name = "Intelligence", description = "AI navigation, doctor matching, wait-time prediction, workload, simulation, recovery, and waitlist ranking endpoints")
public class IntelligenceController {

    private final SpecialtyRecommendationService specialtyRecommendationService;
    private final RecommendationService recommendationService;
    private final DoctorMatchingService doctorMatchingService;
    private final WaitTimeService waitTimeService;
    private final WaitTimePredictionService waitTimePredictionService;
    private final WorkloadIntelligenceService workloadIntelligenceService;
    private final SchedulingSimulationService schedulingSimulationService;
    private final NoShowPredictionService noShowPredictionService;
    private final DisruptionRecoveryService disruptionRecoveryService;
    private final SmartWaitlistService smartWaitlistService;

    public IntelligenceController(SpecialtyRecommendationService specialtyRecommendationService,
                                  RecommendationService recommendationService,
                                  DoctorMatchingService doctorMatchingService,
                                  WaitTimeService waitTimeService,
                                  WaitTimePredictionService waitTimePredictionService,
                                  WorkloadIntelligenceService workloadIntelligenceService,
                                  SchedulingSimulationService schedulingSimulationService,
                                  NoShowPredictionService noShowPredictionService,
                                  DisruptionRecoveryService disruptionRecoveryService,
                                  SmartWaitlistService smartWaitlistService) {
        this.specialtyRecommendationService = specialtyRecommendationService;
        this.recommendationService = recommendationService;
        this.doctorMatchingService = doctorMatchingService;
        this.waitTimeService = waitTimeService;
        this.waitTimePredictionService = waitTimePredictionService;
        this.workloadIntelligenceService = workloadIntelligenceService;
        this.schedulingSimulationService = schedulingSimulationService;
        this.noShowPredictionService = noShowPredictionService;
        this.disruptionRecoveryService = disruptionRecoveryService;
        this.smartWaitlistService = smartWaitlistService;
    }

    @PostMapping("/specialty-recommendation")
    @Operation(summary = "Specialty recommendation", description = "Assists patients in finding the most appropriate specialty based on symptoms. Appointment navigation only, NOT medical diagnosis.")
    public ResponseEntity<SpecialtyRecommendationResponse> recommendSpecialty(
            @Valid @RequestBody SpecialtyRecommendationRequest request) {
        SpecialtyRecommendationService.RecommendationResult result =
                specialtyRecommendationService.recommend(request.getPatientId(), request.getInputText());

        Long specialtyId = result.recommendedSpecialty().map(Specialty::getId).orElse(null);
        String specialtyName = result.recommendedSpecialty().map(Specialty::getName).orElse("General Medicine");

        SpecialtyRecommendationResponse response = new SpecialtyRecommendationResponse(
                specialtyId,
                specialtyName,
                result.reason(),
                result.confidence()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/doctor-match")
    @Operation(summary = "Doctor matching and ranking", description = "Ranks doctors by specialty, mode, slot availability, experience, and wait time with an explainable score")
    public ResponseEntity<List<DoctorMatchResponse>> matchDoctors(
            @Valid @RequestBody DoctorMatchRequest request) {
        List<DoctorMatchingService.DoctorMatchResult> results =
                doctorMatchingService.rankDoctors(request.getSpecialtyId(), request.getMode());

        List<DoctorMatchResponse> responses = results.stream().map(r -> new DoctorMatchResponse(
                r.doctor().getId(),
                r.doctor().getUser() != null ? r.doctor().getUser().getName() : null,
                r.doctor().getQualification(),
                r.doctor().getExperienceYears(),
                r.doctor().getConsultationFee(),
                r.specialty() != null ? r.specialty().getId() : null,
                r.specialty() != null ? r.specialty().getName() : null,
                r.specialtyMatch(),
                r.modeMatch(),
                r.hasAvailableSlot(),
                r.estimatedWaitMinutes(),
                r.score(),
                r.reasoning()
        )).toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/wait-time/{appointmentId}")
    @Operation(summary = "Wait-time estimate", description = "Estimates the expected wait time for an appointment using deterministic queue analysis. Baseline, non-clinical.")
    public ResponseEntity<WaitTimeResponse> estimateWaitTime(@PathVariable Long appointmentId) {
        var prediction = waitTimePredictionService.predictAndSave(appointmentId);

        WaitTimeResponse response = new WaitTimeResponse(
                appointmentId,
                prediction.getPredictedMinutes(),
                "Deterministic baseline queue model",
                "Estimated wait time is calculated for scheduling guidance and does NOT guarantee exact consultation start time."
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/workload/doctor/{doctorId}")
    @Operation(summary = "Doctor workload analysis", description = "Calculates workload score, load level (LOW/MEDIUM/HIGH/CRITICAL), and operational recommendations")
    public ResponseEntity<WorkloadResponse> analyzeDoctorWorkload(@PathVariable Long doctorId) {
        WorkloadIntelligenceService.WorkloadAnalysis analysis = workloadIntelligenceService.analyzeDoctor(doctorId);

        WorkloadResponse response = new WorkloadResponse(
                analysis.doctorId(),
                "Doctor #" + analysis.doctorId(),
                analysis.currentScore(),
                analysis.loadLevel(),
                analysis.recommendation(),
                analysis.appointmentCount(),
                analysis.completedCount(),
                analysis.cancelledCount(),
                analysis.noShowCount()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/workload/all")
    @Operation(summary = "All doctors workload analysis", description = "Retrieves workload scores and overload alerts across all doctors for clinic operations")
    public ResponseEntity<List<WorkloadResponse>> analyzeAllWorkload() {
        List<WorkloadIntelligenceService.WorkloadAnalysis> analyses = workloadIntelligenceService.analyzeAllDoctors();

        List<WorkloadResponse> responses = analyses.stream().map(a -> new WorkloadResponse(
                a.doctorId(),
                "Doctor #" + a.doctorId(),
                a.currentScore(),
                a.loadLevel(),
                a.recommendation(),
                a.appointmentCount(),
                a.completedCount(),
                a.cancelledCount(),
                a.noShowCount()
        )).toList();

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/simulation")
    @Operation(summary = "Schedule simulation (what-if analysis)", description = "Simulates doctor unavailability or capacity shifts. Read-only: strictly NEVER modifies production appointments.")
    public ResponseEntity<SimulationResponse> runSimulation(@Valid @RequestBody SimulationRequest request) {
        ScenarioType scenario = request.getScenario() != null ? request.getScenario() : ScenarioType.DOCTOR_UNAVAILABLE;
        SchedulingSimulationService.SimulationResult result =
                schedulingSimulationService.simulate(request.getDoctorId(), request.getDate(), scenario);

        List<Long> affectedIds = result.affectedAppointments().stream()
                .map(Appointment::getId)
                .toList();

        List<Long> altDocIds = result.candidateAlternativeDoctors().stream()
                .map(Doctor::getId)
                .toList();

        List<String> altDocNames = result.candidateAlternativeDoctors().stream()
                .map(d -> d.getUser() != null ? d.getUser().getName() : "Dr. #" + d.getId())
                .toList();

        SimulationResponse response = new SimulationResponse(
                result.doctorId(),
                "Doctor #" + result.doctorId(),
                result.date(),
                result.scenario(),
                result.affectedAppointmentCount(),
                affectedIds,
                altDocIds,
                altDocNames,
                result.workloadImpactSummary(),
                result.redistributionSuggestion(),
                true
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/no-show/{appointmentId}")
    @Operation(summary = "No-show prediction", description = "Predicts baseline probability of patient non-attendance for an appointment based on past attendance history")
    public ResponseEntity<NoShowPredictionResponse> predictNoShow(@PathVariable Long appointmentId) {
        NoShowPredictionService.NoShowPrediction prediction = noShowPredictionService.predict(appointmentId);

        NoShowPredictionResponse response = new NoShowPredictionResponse(
                prediction.appointmentId(),
                prediction.noShowProbability(),
                prediction.riskLevel(),
                prediction.confidence(),
                prediction.method(),
                prediction.basis()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/recovery")
    @Operation(summary = "Disruption recovery analysis", description = "Evaluates affected patients and proposes candidate reassignments when a doctor is unavailable. Strictly read-only.")
    public ResponseEntity<RecoveryResponse> evaluateRecovery(@Valid @RequestBody RecoveryRequest request) {
        DisruptionRecoveryService.DisruptionRecoveryResult result =
                disruptionRecoveryService.evaluateRecovery(request.getDoctorId(), request.getDate());

        List<RecoveryResponse.PatientRecoverySuggestion> suggestions = result.patientPlans().stream().map(p ->
                new RecoveryResponse.PatientRecoverySuggestion(
                        p.appointment().getId(),
                        p.appointment().getPatient() != null ? p.appointment().getPatient().getId() : null,
                        p.appointment().getPatient() != null && p.appointment().getPatient().getUser() != null
                                ? p.appointment().getPatient().getUser().getName()
                                : "Patient #" + (p.appointment().getPatient() != null ? p.appointment().getPatient().getId() : "N/A"),
                        p.appointment().getSlot() != null ? p.appointment().getSlot().getStartTime() : null,
                        p.suggestedDoctor() != null ? p.suggestedDoctor().getId() : null,
                        p.suggestedDoctor() != null && p.suggestedDoctor().getUser() != null
                                ? p.suggestedDoctor().getUser().getName()
                                : (p.suggestedDoctor() != null ? "Dr. #" + p.suggestedDoctor().getId() : "N/A"),
                        p.suggestedTime(),
                        p.specialtyName(),
                        p.mode(),
                        p.rationale()
                )
        ).toList();

        List<RecoveryResponse.AlternativeDoctorSummary> altDoctors = result.alternativeDoctors().stream().map(a ->
                new RecoveryResponse.AlternativeDoctorSummary(
                        a.doctor().getId(),
                        a.doctor().getUser() != null ? a.doctor().getUser().getName() : "Dr. #" + a.doctor().getId(),
                        a.primarySpecialty(),
                        a.availableCapacity()
                )
        ).toList();

        RecoveryResponse response = new RecoveryResponse(
                result.unavailableDoctorId(),
                result.unavailableDoctorName(),
                result.date(),
                result.affectedAppointmentCount(),
                suggestions,
                altDoctors,
                result.summaryRecommendation()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/waitlist-rank")
    @Operation(summary = "Smart waitlist candidate ranking", description = "Ranks waiting patients for an available slot using explainable multi-factor scoring")
    public ResponseEntity<List<WaitlistRankResponse>> rankWaitlist(@Valid @RequestBody WaitlistRankRequest request) {
        List<SmartWaitlistService.WaitlistRankResult> results =
                smartWaitlistService.rankWaitingPatients(request.getSpecialtyId(), request.getDoctorId(), request.getMode(), request.getDate());

        List<WaitlistRankResponse> responses = results.stream().map(r -> new WaitlistRankResponse(
                r.waitlistEntry().getId(),
                r.waitlistEntry().getPatient() != null ? r.waitlistEntry().getPatient().getId() : null,
                r.waitlistEntry().getPatient() != null && r.waitlistEntry().getPatient().getUser() != null
                        ? r.waitlistEntry().getPatient().getUser().getName() : "Patient",
                r.waitlistEntry().getSpecialty() != null ? r.waitlistEntry().getSpecialty().getId() : null,
                r.waitlistEntry().getSpecialty() != null ? r.waitlistEntry().getSpecialty().getName() : "Specialty",
                r.waitlistEntry().getPreferredDoctor() != null ? r.waitlistEntry().getPreferredDoctor().getId() : null,
                r.waitlistEntry().getPreferredDoctor() != null && r.waitlistEntry().getPreferredDoctor().getUser() != null
                        ? r.waitlistEntry().getPreferredDoctor().getUser().getName() : null,
                r.waitlistEntry().getPreferredDate(),
                r.waitlistEntry().getPreferredMode() != null ? r.waitlistEntry().getPreferredMode().name() : "ONLINE",
                r.matchScore(),
                r.scoringFactors(),
                r.rank()
        )).toList();

        return ResponseEntity.ok(responses);
    }
}
