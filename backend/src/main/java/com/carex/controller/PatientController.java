package com.carex.controller;

import com.carex.dto.patient.PatientRequest;
import com.carex.dto.patient.PatientResponse;
import com.carex.dto.patient.PatientUpdateRequest;
import com.carex.entity.Patient;
import com.carex.mapper.PatientMapper;
import com.carex.service.PatientService;
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
@RequestMapping("/api/patients")
@Tag(name = "Patients", description = "Patient registration and profile management endpoints")
public class PatientController {

    private final PatientService patientService;
    private final PatientMapper patientMapper;

    public PatientController(PatientService patientService, PatientMapper patientMapper) {
        this.patientService = patientService;
        this.patientMapper = patientMapper;
    }

    @GetMapping
    @Operation(summary = "Get all patients", description = "Retrieves all patient records with associated user details")
    public ResponseEntity<List<PatientResponse>> getAllPatients() {
        List<Patient> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patientMapper.toResponseList(patients));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get patient by ID", description = "Retrieves a patient record by ID")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long id) {
        Patient patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patientMapper.toResponse(patient));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get patient by User ID", description = "Retrieves a patient record associated with a specific user ID")
    public ResponseEntity<PatientResponse> getPatientByUserId(@PathVariable Long userId) {
        Patient patient = patientService.getPatientByUserId(userId);
        return ResponseEntity.ok(patientMapper.toResponse(patient));
    }

    @PostMapping
    @Operation(summary = "Create patient record", description = "Creates a patient profile linked to an existing user account")
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody PatientRequest request) {
        Patient created = patientService.createPatient(
                request.getUserId(),
                request.getDateOfBirth(),
                request.getGender(),
                request.getEmergencyContact()
        );
        return new ResponseEntity<>(patientMapper.toResponse(created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update patient record", description = "Updates patient demographic and emergency contact details")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientUpdateRequest request) {
        Patient updated = patientService.updatePatient(
                id,
                request.getDateOfBirth(),
                request.getGender(),
                request.getEmergencyContact()
        );
        return ResponseEntity.ok(patientMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete patient record", description = "Deletes a patient record by ID")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
