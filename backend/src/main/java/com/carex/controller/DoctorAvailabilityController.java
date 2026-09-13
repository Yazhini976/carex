package com.carex.controller;

import com.carex.dto.availability.DoctorAvailabilityRequest;
import com.carex.dto.availability.DoctorAvailabilityResponse;
import com.carex.entity.DoctorAvailability;
import com.carex.mapper.AvailabilityMapper;
import com.carex.service.DoctorAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/availability")
@Tag(name = "Availability", description = "Doctor schedule availability pattern endpoints")
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService availabilityService;
    private final AvailabilityMapper availabilityMapper;

    public DoctorAvailabilityController(DoctorAvailabilityService availabilityService,
                                        AvailabilityMapper availabilityMapper) {
        this.availabilityService = availabilityService;
        this.availabilityMapper = availabilityMapper;
    }

    @PostMapping
    @Operation(summary = "Create availability rule", description = "Defines a doctor's weekly recurring availability pattern")
    public ResponseEntity<DoctorAvailabilityResponse> createAvailability(
            @Valid @RequestBody DoctorAvailabilityRequest request) {
        DoctorAvailability created = availabilityService.createAvailability(
                request.getDoctorId(),
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime(),
                request.getMode()
        );
        return new ResponseEntity<>(availabilityMapper.toResponse(created), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get availability by ID", description = "Retrieves an availability pattern by ID")
    public ResponseEntity<DoctorAvailabilityResponse> getAvailabilityById(@PathVariable Long id) {
        DoctorAvailability availability = availabilityService.getAvailabilityById(id);
        return ResponseEntity.ok(availabilityMapper.toResponse(availability));
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get doctor availability patterns", description = "Retrieves all availability patterns for a specific doctor")
    public ResponseEntity<List<DoctorAvailabilityResponse>> getAvailabilityForDoctor(
            @PathVariable Long doctorId) {
        List<DoctorAvailability> list = availabilityService.getAvailabilityForDoctor(doctorId);
        return ResponseEntity.ok(availabilityMapper.toResponseList(list));
    }

    @GetMapping("/doctor/{doctorId}/active")
    @Operation(summary = "Get active doctor availability", description = "Retrieves only active availability patterns for a doctor")
    public ResponseEntity<List<DoctorAvailabilityResponse>> getActiveAvailabilityForDoctor(
            @PathVariable Long doctorId) {
        List<DoctorAvailability> list = availabilityService.getActiveAvailabilityForDoctor(doctorId);
        return ResponseEntity.ok(availabilityMapper.toResponseList(list));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update availability rule", description = "Updates day of week, hours, or mode for an availability pattern")
    public ResponseEntity<DoctorAvailabilityResponse> updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody DoctorAvailabilityRequest request) {
        DoctorAvailability updated = availabilityService.updateAvailability(
                id,
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime(),
                request.getMode()
        );
        return ResponseEntity.ok(availabilityMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete availability rule", description = "Deletes an availability pattern")
    public ResponseEntity<Void> deleteAvailability(@PathVariable Long id) {
        availabilityService.deleteAvailability(id);
        return ResponseEntity.noContent().build();
    }
}
