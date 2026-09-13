package com.carex.controller;

import com.carex.dto.specialty.SpecialtyRequest;
import com.carex.dto.specialty.SpecialtyResponse;
import com.carex.entity.Specialty;
import com.carex.mapper.SpecialtyMapper;
import com.carex.service.SpecialtyService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/specialties")
@Tag(name = "Specialties", description = "Medical specialties catalog endpoints")
public class SpecialtyController {

    private final SpecialtyService specialtyService;
    private final SpecialtyMapper specialtyMapper;

    public SpecialtyController(SpecialtyService specialtyService, SpecialtyMapper specialtyMapper) {
        this.specialtyService = specialtyService;
        this.specialtyMapper = specialtyMapper;
    }

    @GetMapping
    @Operation(summary = "Get all specialties", description = "Retrieves all medical specialties")
    public ResponseEntity<List<SpecialtyResponse>> getAllSpecialties() {
        List<Specialty> specialties = specialtyService.getAllSpecialties();
        return ResponseEntity.ok(specialtyMapper.toResponseList(specialties));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active specialties", description = "Retrieves only active medical specialties")
    public ResponseEntity<List<SpecialtyResponse>> getActiveSpecialties() {
        List<Specialty> specialties = specialtyService.getActiveSpecialties();
        return ResponseEntity.ok(specialtyMapper.toResponseList(specialties));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get specialty by ID", description = "Retrieves details of a specific medical specialty")
    public ResponseEntity<SpecialtyResponse> getSpecialtyById(@PathVariable Long id) {
        Specialty specialty = specialtyService.getSpecialtyById(id);
        return ResponseEntity.ok(specialtyMapper.toResponse(specialty));
    }

    @PostMapping
    @Operation(summary = "Create specialty", description = "Creates a new medical specialty catalog item")
    public ResponseEntity<SpecialtyResponse> createSpecialty(@Valid @RequestBody SpecialtyRequest request) {
        Specialty created = specialtyService.createSpecialty(request.getName(), request.getDescription());
        return new ResponseEntity<>(specialtyMapper.toResponse(created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update specialty", description = "Updates the name and description of a medical specialty")
    public ResponseEntity<SpecialtyResponse> updateSpecialty(
            @PathVariable Long id,
            @Valid @RequestBody SpecialtyRequest request) {
        Specialty updated = specialtyService.updateSpecialty(id, request.getName(), request.getDescription());
        return ResponseEntity.ok(specialtyMapper.toResponse(updated));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Toggle specialty status", description = "Activates or deactivates a medical specialty")
    public ResponseEntity<SpecialtyResponse> setSpecialtyStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        Specialty updated = specialtyService.setActiveStatus(id, active);
        return ResponseEntity.ok(specialtyMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete specialty", description = "Deletes a medical specialty")
    public ResponseEntity<Void> deleteSpecialty(@PathVariable Long id) {
        specialtyService.deleteSpecialty(id);
        return ResponseEntity.noContent().build();
    }
}
