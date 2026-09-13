package com.carex.controller;

import com.carex.dto.waitlist.WaitlistRequest;
import com.carex.dto.waitlist.WaitlistResponse;
import com.carex.entity.Waitlist;
import com.carex.entity.enums.AppointmentMode;
import com.carex.mapper.WaitlistMapper;
import com.carex.service.WaitlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/waitlist")
@Tag(name = "Waitlist", description = "Patient waitlist queue and smart matching endpoints")
public class WaitlistController {

    private final WaitlistService waitlistService;
    private final WaitlistMapper waitlistMapper;

    public WaitlistController(WaitlistService waitlistService, WaitlistMapper waitlistMapper) {
        this.waitlistService = waitlistService;
        this.waitlistMapper = waitlistMapper;
    }

    @PostMapping
    @Operation(summary = "Join waitlist", description = "Places a patient on the priority waitlist for an unavailable specialty or doctor")
    public ResponseEntity<WaitlistResponse> joinWaitlist(@Valid @RequestBody WaitlistRequest request) {
        Waitlist entry = waitlistService.joinWaitlist(
                request.getPatientId(),
                request.getSpecialtyId(),
                request.getPreferredDoctorId(),
                request.getPreferredDate(),
                request.getPreferredMode()
        );
        return new ResponseEntity<>(waitlistMapper.toResponse(entry), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get waitlist entry by ID", description = "Retrieves details of a waitlist entry")
    public ResponseEntity<WaitlistResponse> getWaitlistEntryById(@PathVariable Long id) {
        Waitlist entry = waitlistService.getWaitlistEntryById(id);
        return ResponseEntity.ok(waitlistMapper.toResponse(entry));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get patient waitlist entries", description = "Retrieves all waitlist entries for a specific patient")
    public ResponseEntity<List<WaitlistResponse>> getPatientWaitlist(@PathVariable Long patientId) {
        List<Waitlist> list = waitlistService.getPatientWaitlist(patientId);
        return ResponseEntity.ok(waitlistMapper.toResponseList(list));
    }

    @GetMapping("/matching")
    @Operation(summary = "Find matching waitlist entries", description = "Finds eligible waiting patients when a slot opens up")
    public ResponseEntity<List<WaitlistResponse>> findMatchingWaiting(
            @RequestParam Long specialtyId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) AppointmentMode mode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Waitlist> list = waitlistService.findMatchingWaiting(specialtyId, doctorId, mode, date);
        return ResponseEntity.ok(waitlistMapper.toResponseList(list));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel waitlist entry", description = "Cancels a patient's waitlist request")
    public ResponseEntity<WaitlistResponse> cancelEntry(@PathVariable Long id) {
        Waitlist entry = waitlistService.cancelEntry(id);
        return ResponseEntity.ok(waitlistMapper.toResponse(entry));
    }

    @PutMapping("/{id}/offer")
    @Operation(summary = "Mark waitlist slot offered", description = "Marks a waitlist entry as OFFERED when a candidate slot is proposed")
    public ResponseEntity<WaitlistResponse> markOffered(@PathVariable Long id) {
        Waitlist entry = waitlistService.markOffered(id);
        return ResponseEntity.ok(waitlistMapper.toResponse(entry));
    }

    @PutMapping("/{id}/fulfill")
    @Operation(summary = "Mark waitlist fulfilled", description = "Marks a waitlist entry as FULFILLED once the patient successfully books the slot")
    public ResponseEntity<WaitlistResponse> markFulfilled(@PathVariable Long id) {
        Waitlist entry = waitlistService.markFulfilled(id);
        return ResponseEntity.ok(waitlistMapper.toResponse(entry));
    }
}
