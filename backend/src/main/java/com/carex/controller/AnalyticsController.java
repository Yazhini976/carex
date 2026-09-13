package com.carex.controller;

import com.carex.dto.common.DailySummaryResponse;
import com.carex.entity.Appointment;
import com.carex.entity.WorkloadSnapshot;
import com.carex.entity.enums.AppointmentStatus;
import com.carex.service.AppointmentService;
import com.carex.service.DoctorService;
import com.carex.service.WorkloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Operational reporting, daily statistics, workload, and revenue metrics")
public class AnalyticsController {

    private final AppointmentService appointmentService;
    private final WorkloadService workloadService;
    private final DoctorService doctorService;

    public AnalyticsController(AppointmentService appointmentService,
                               WorkloadService workloadService,
                               DoctorService doctorService) {
        this.appointmentService = appointmentService;
        this.workloadService = workloadService;
        this.doctorService = doctorService;
    }

    @GetMapping("/daily")
    @Operation(summary = "Daily operations analytics", description = "Returns aggregated appointment statistics for a specified day")
    public ResponseEntity<DailySummaryResponse> getDailyMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate queryDate = date != null ? date : LocalDate.now();
        Map<String, Object> summaryMap = appointmentService.getDailySummary(queryDate);

        DailySummaryResponse response = new DailySummaryResponse(
                queryDate,
                ((Number) summaryMap.getOrDefault("totalAppointments", 0)).longValue(),
                ((Number) summaryMap.getOrDefault("booked", 0)).longValue(),
                ((Number) summaryMap.getOrDefault("confirmed", 0)).longValue(),
                ((Number) summaryMap.getOrDefault("completed", 0)).longValue(),
                ((Number) summaryMap.getOrDefault("cancelled", 0)).longValue(),
                ((Number) summaryMap.getOrDefault("noShow", 0)).longValue()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/appointments")
    @Operation(summary = "Appointment status distribution", description = "Returns distribution across all appointment statuses for charting")
    public ResponseEntity<Map<String, Long>> getAppointmentDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        for (AppointmentStatus status : AppointmentStatus.values()) {
            List<Appointment> list = appointmentService.getAppointmentsByStatus(status);
            distribution.put(status.name(), (long) list.size());
        }
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/workload")
    @Operation(summary = "Doctor workload snapshots", description = "Captures and returns real-time workload snapshots across all doctors")
    public ResponseEntity<List<WorkloadSnapshot>> getWorkloadAnalytics() {
        List<WorkloadSnapshot> snapshots = workloadService.captureAllDoctorSnapshots();
        return ResponseEntity.ok(snapshots);
    }

    @GetMapping("/revenue")
    @Operation(summary = "Estimated revenue analytics", description = "Calculates realized and estimated revenue based on completed and booked appointments")
    public ResponseEntity<Map<String, Object>> getRevenueAnalytics() {
        List<Appointment> completed = appointmentService.getAppointmentsByStatus(AppointmentStatus.COMPLETED);
        List<Appointment> booked = appointmentService.getAppointmentsByStatus(AppointmentStatus.BOOKED);
        List<Appointment> confirmed = appointmentService.getAppointmentsByStatus(AppointmentStatus.CONFIRMED);

        BigDecimal realizedRevenue = completed.stream()
                .filter(a -> a.getDoctor() != null && a.getDoctor().getConsultationFee() != null)
                .map(a -> a.getDoctor().getConsultationFee())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pipelineRevenue = booked.stream()
                .filter(a -> a.getDoctor() != null && a.getDoctor().getConsultationFee() != null)
                .map(a -> a.getDoctor().getConsultationFee())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(confirmed.stream()
                        .filter(a -> a.getDoctor() != null && a.getDoctor().getConsultationFee() != null)
                        .map(a -> a.getDoctor().getConsultationFee())
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        Map<String, Object> revenue = new HashMap<>();
        revenue.put("realizedRevenue", realizedRevenue);
        revenue.put("pipelineRevenue", pipelineRevenue);
        revenue.put("completedAppointmentsCount", completed.size());
        revenue.put("activeAppointmentsCount", booked.size() + confirmed.size());

        return ResponseEntity.ok(revenue);
    }
}
