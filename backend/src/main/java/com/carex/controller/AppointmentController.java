package com.carex.controller;

import com.carex.dto.appointment.AppointmentBookingRequest;
import com.carex.dto.appointment.AppointmentResponse;
import com.carex.dto.appointment.AppointmentStatusUpdateRequest;
import com.carex.dto.common.DailySummaryResponse;
import com.carex.entity.Appointment;
import com.carex.entity.Patient;
import com.carex.entity.enums.AppointmentStatus;
import com.carex.entity.enums.Role;
import com.carex.mapper.AppointmentMapper;
import com.carex.security.CustomUserPrincipal;
import com.carex.security.SecurityUtils;
import com.carex.service.AppointmentService;
import com.carex.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/appointments")
@Tag(name = "Appointments", description = "Appointment booking, lifecycle status transitions, and query endpoints")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final AppointmentMapper appointmentMapper;

    public AppointmentController(AppointmentService appointmentService,
                                 PatientService patientService,
                                 AppointmentMapper appointmentMapper) {
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.appointmentMapper = appointmentMapper;
    }

    @PostMapping
    @Operation(summary = "Book appointment", description = "Books an appointment enforcing slot availability, concurrency locking, and online/offline doctor constraints")
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @Valid @RequestBody AppointmentBookingRequest request) {

        // Enforce patient identity: if authenticated as PATIENT, verify booking is for own patient profile
        Optional<CustomUserPrincipal> principalOpt = SecurityUtils.getCurrentPrincipal();
        if (principalOpt.isPresent()) {
            CustomUserPrincipal principal = principalOpt.get();
            if (principal.getRole() == Role.PATIENT) {
                try {
                    Patient patient = patientService.getPatientById(request.getPatientId());
                    if (patient.getUser() != null && !patient.getUser().getId().equals(principal.getId())) {
                        throw new AccessDeniedException("Patients can only book appointments for their own profile.");
                    }
                } catch (AccessDeniedException ade) {
                    throw ade;
                } catch (Exception ignored) {
                    // Let service layer handle not-found
                }
            }
        }

        Appointment booked = appointmentService.bookAppointment(
                request.getPatientId(),
                request.getDoctorId(),
                request.getSpecialtyId(),
                request.getSlotId(),
                request.getMode(),
                request.getNotes()
        );
        return new ResponseEntity<>(appointmentMapper.toResponse(booked), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get appointment by ID", description = "Retrieves appointment details including patient, doctor, slot, and status")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        verifyAppointmentAccess(appointment, "You do not have permission to view this appointment.");
        return ResponseEntity.ok(appointmentMapper.toResponse(appointment));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get patient appointments", description = "Retrieves all appointments booked for a patient")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByPatient(@PathVariable Long patientId) {
        // Ownership check for patients
        Optional<CustomUserPrincipal> principalOpt = SecurityUtils.getCurrentPrincipal();
        if (principalOpt.isPresent()) {
            CustomUserPrincipal principal = principalOpt.get();
            if (principal.getRole() == Role.PATIENT) {
                try {
                    Patient patient = patientService.getPatientById(patientId);
                    if (patient.getUser() != null && !patient.getUser().getId().equals(principal.getId())) {
                        throw new AccessDeniedException("You cannot view appointments belonging to another patient.");
                    }
                } catch (AccessDeniedException ade) {
                    throw ade;
                } catch (Exception ignored) {}
            }
        }

        List<Appointment> appointments = appointmentService.getAppointmentsByPatient(patientId);
        return ResponseEntity.ok(appointmentMapper.toResponseList(appointments));
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get doctor appointments", description = "Retrieves all appointments assigned to a doctor")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Appointment> appointments;
        if (date != null) {
            appointments = appointmentService.getAppointmentsByDoctorAndDate(doctorId, date);
        } else {
            appointments = appointmentService.getAppointmentsByDoctor(doctorId);
        }
        return ResponseEntity.ok(appointmentMapper.toResponseList(appointments));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    @Operation(summary = "Filter appointments by status", description = "Retrieves appointments matching a specific status (BOOKED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW)")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByStatus(@PathVariable AppointmentStatus status) {
        List<Appointment> appointments = appointmentService.getAppointmentsByStatus(status);
        return ResponseEntity.ok(appointmentMapper.toResponseList(appointments));
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')")
    @Operation(summary = "Confirm appointment", description = "Transitions an appointment status from BOOKED to CONFIRMED")
    public ResponseEntity<AppointmentResponse> confirmAppointment(
            @PathVariable Long id,
            @RequestBody(required = false) AppointmentStatusUpdateRequest request) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        verifyDoctorOrAdminAccess(appointment, "Only the assigned doctor or admin can confirm this appointment.");

        Long changedBy = (request != null && request.getChangedByUserId() != null)
                ? request.getChangedByUserId()
                : SecurityUtils.getCurrentUserId().orElse(null);

        Appointment confirmed = appointmentService.confirmAppointment(id, changedBy);
        return ResponseEntity.ok(appointmentMapper.toResponse(confirmed));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Complete appointment", description = "Transitions an appointment status from CONFIRMED to COMPLETED")
    public ResponseEntity<AppointmentResponse> completeAppointment(
            @PathVariable Long id,
            @RequestBody(required = false) AppointmentStatusUpdateRequest request) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        verifyDoctorOrAdminAccess(appointment, "Only the assigned doctor or admin can complete this appointment.");

        Long changedBy = (request != null && request.getChangedByUserId() != null)
                ? request.getChangedByUserId()
                : SecurityUtils.getCurrentUserId().orElse(null);

        Appointment completed = appointmentService.completeAppointment(id, changedBy);
        return ResponseEntity.ok(appointmentMapper.toResponse(completed));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel appointment", description = "Cancels an appointment, frees the slot, and triggers smart waitlist matching")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable Long id,
            @RequestBody(required = false) AppointmentStatusUpdateRequest request) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        verifyAppointmentAccess(appointment, "You do not have permission to cancel this appointment.");

        Long changedBy = (request != null && request.getChangedByUserId() != null)
                ? request.getChangedByUserId()
                : SecurityUtils.getCurrentUserId().orElse(null);

        Appointment cancelled = appointmentService.cancelAppointment(id, changedBy);
        return ResponseEntity.ok(appointmentMapper.toResponse(cancelled));
    }

    @PutMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Mark appointment as no-show", description = "Marks a patient appointment as NO_SHOW and records audit history")
    public ResponseEntity<AppointmentResponse> markNoShow(
            @PathVariable Long id,
            @RequestBody(required = false) AppointmentStatusUpdateRequest request) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        verifyDoctorOrAdminAccess(appointment, "Only the assigned doctor or admin can mark an appointment as no-show.");

        Long changedBy = (request != null && request.getChangedByUserId() != null)
                ? request.getChangedByUserId()
                : SecurityUtils.getCurrentUserId().orElse(null);

        Appointment noShow = appointmentService.markNoShow(id, changedBy);
        return ResponseEntity.ok(appointmentMapper.toResponse(noShow));
    }

    @GetMapping("/summary/daily")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'DOCTOR')")
    @Operation(summary = "Get daily appointment summary", description = "Returns aggregated appointment counts by status for a given day (defaults to today)")
    public ResponseEntity<DailySummaryResponse> getDailySummary(
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

    private void verifyAppointmentAccess(Appointment appointment, String message) {
        Optional<CustomUserPrincipal> principalOpt = SecurityUtils.getCurrentPrincipal();
        if (principalOpt.isEmpty()) return;

        CustomUserPrincipal principal = principalOpt.get();
        if (principal.getRole() == Role.ADMIN) return;

        boolean isPatientOwner = appointment.getPatient() != null
                && appointment.getPatient().getUser() != null
                && appointment.getPatient().getUser().getId().equals(principal.getId());

        boolean isDoctorOwner = appointment.getDoctor() != null
                && appointment.getDoctor().getUser() != null
                && appointment.getDoctor().getUser().getId().equals(principal.getId());

        if (!isPatientOwner && !isDoctorOwner) {
            throw new AccessDeniedException(message);
        }
    }

    private void verifyDoctorOrAdminAccess(Appointment appointment, String message) {
        Optional<CustomUserPrincipal> principalOpt = SecurityUtils.getCurrentPrincipal();
        if (principalOpt.isEmpty()) return;

        CustomUserPrincipal principal = principalOpt.get();
        if (principal.getRole() == Role.ADMIN) return;

        boolean isDoctorOwner = appointment.getDoctor() != null
                && appointment.getDoctor().getUser() != null
                && appointment.getDoctor().getUser().getId().equals(principal.getId());

        if (!isDoctorOwner) {
            throw new AccessDeniedException(message);
        }
    }
}
