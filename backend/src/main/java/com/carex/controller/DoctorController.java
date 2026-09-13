package com.carex.controller;

import com.carex.dto.doctor.AdminDoctorRequest;
import com.carex.dto.doctor.DoctorRequest;
import com.carex.dto.doctor.DoctorResponse;
import com.carex.dto.doctor.DoctorUpdateRequest;
import com.carex.entity.Doctor;
import com.carex.entity.Specialty;
import com.carex.exception.ResourceNotFoundException;
import com.carex.mapper.DoctorMapper;
import com.carex.service.DoctorService;
import com.carex.service.DoctorSpecialtyService;
import com.carex.service.AdminDoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@Tag(name = "Doctors", description = "Doctor profile, qualification, and specialty endpoints")
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorSpecialtyService doctorSpecialtyService;
    private final DoctorMapper doctorMapper;
    private final AdminDoctorService adminDoctorService;

    public DoctorController(DoctorService doctorService,
                            DoctorSpecialtyService doctorSpecialtyService,
                            DoctorMapper doctorMapper,
                            AdminDoctorService adminDoctorService) {
        this.doctorService = doctorService;
        this.doctorSpecialtyService = doctorSpecialtyService;
        this.doctorMapper = doctorMapper;
        this.adminDoctorService = adminDoctorService;
    }

    @GetMapping
    @Operation(summary = "Get all doctors", description = "Retrieves a list of all doctors with their assigned specialties")
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        List<DoctorResponse> responses = doctors.stream().map(doc -> {
            List<Specialty> specialties = doctorSpecialtyService.getSpecialtiesForDoctor(doc.getId());
            return doctorMapper.toResponse(doc, specialties);
        }).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active doctors", description = "Retrieves only currently active practicing doctors")
    public ResponseEntity<List<DoctorResponse>> getActiveDoctors() {
        List<Doctor> doctors = doctorService.getActiveDoctors();
        List<DoctorResponse> responses = doctors.stream().map(doc -> {
            List<Specialty> specialties = doctorSpecialtyService.getSpecialtiesForDoctor(doc.getId());
            return doctorMapper.toResponse(doc, specialties);
        }).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get doctor by ID", description = "Retrieves doctor details and assigned specialties by doctor ID")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable Long id) {
        Doctor doctor = doctorService.getDoctorById(id);
        List<Specialty> specialties = doctorSpecialtyService.getSpecialtiesForDoctor(id);
        return ResponseEntity.ok(doctorMapper.toResponse(doctor, specialties));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get doctor by User ID", description = "Retrieves doctor details associated with a specific user ID")
    public ResponseEntity<DoctorResponse> getDoctorByUserId(@PathVariable Long userId) {
        Doctor doctor = doctorService.findDoctorByUserId(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Doctor", "userId", String.valueOf(userId)));
        List<Specialty> specialties = doctorSpecialtyService.getSpecialtiesForDoctor(doctor.getId());
        return ResponseEntity.ok(doctorMapper.toResponse(doctor, specialties));
    }

    @PostMapping
    @Operation(summary = "Create doctor profile", description = "Creates a doctor profile linked to an existing user account")
    public ResponseEntity<DoctorResponse> createDoctor(@Valid @RequestBody DoctorRequest request) {
        Doctor created = doctorService.createDoctor(
                request.getUserId(),
                request.getLicenseNumber(),
                request.getQualification(),
                request.getExperienceYears(),
                request.getConsultationFee()
        );
        return new ResponseEntity<>(doctorMapper.toResponse(created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update doctor profile", description = "Updates doctor qualifications, experience, fee, and active status")
    public ResponseEntity<DoctorResponse> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorUpdateRequest request) {
        Doctor updated = doctorService.updateDoctor(
                id,
                request.getQualification(),
                request.getExperienceYears(),
                request.getConsultationFee()
        );

        if (request.getActive() != null) {
            updated = doctorService.setActiveStatus(id, request.getActive());
        }

        List<Specialty> specialties = doctorSpecialtyService.getSpecialtiesForDoctor(id);
        return ResponseEntity.ok(doctorMapper.toResponse(updated, specialties));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Set doctor active status", description = "Explicitly activates or deactivates a doctor")
    public ResponseEntity<DoctorResponse> setDoctorStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        Doctor updated = doctorService.setActiveStatus(id, active);
        List<Specialty> specialties = doctorSpecialtyService.getSpecialtiesForDoctor(id);
        return ResponseEntity.ok(doctorMapper.toResponse(updated, specialties));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Toggle doctor active status", description = "Flips the active status of a doctor without requiring a query param")
    public ResponseEntity<DoctorResponse> toggleDoctorStatus(@PathVariable Long id) {
        Doctor doctor = doctorService.getDoctorById(id);
        boolean newStatus = !Boolean.TRUE.equals(doctor.getIsActive());
        Doctor updated = doctorService.setActiveStatus(id, newStatus);
        List<Specialty> specialties = doctorSpecialtyService.getSpecialtiesForDoctor(id);
        return ResponseEntity.ok(doctorMapper.toResponse(updated, specialties));
    }

    @PostMapping("/admin")
    @Operation(summary = "Admin: Register new doctor", description = "Creates user account + doctor profile in one step (admin only)")
    public ResponseEntity<DoctorResponse> adminCreateDoctor(@Valid @RequestBody AdminDoctorRequest request) {
        DoctorResponse response = adminDoctorService.registerDoctor(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete doctor profile", description = "Deletes or deactivates a doctor record")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }
}
