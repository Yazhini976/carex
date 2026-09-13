package com.carex.controller;

import com.carex.dto.doctor.DoctorResponse;
import com.carex.dto.specialty.SpecialtyResponse;
import com.carex.entity.Doctor;
import com.carex.entity.Specialty;
import com.carex.mapper.DoctorMapper;
import com.carex.mapper.SpecialtyMapper;
import com.carex.service.DoctorSpecialtyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/doctor-specialties")
@Tag(name = "Doctor Specialties", description = "Doctor-to-specialty mapping endpoints")
public class DoctorSpecialtyController {

    private final DoctorSpecialtyService doctorSpecialtyService;
    private final SpecialtyMapper specialtyMapper;
    private final DoctorMapper doctorMapper;

    public DoctorSpecialtyController(DoctorSpecialtyService doctorSpecialtyService,
                                   SpecialtyMapper specialtyMapper,
                                   DoctorMapper doctorMapper) {
        this.doctorSpecialtyService = doctorSpecialtyService;
        this.specialtyMapper = specialtyMapper;
        this.doctorMapper = doctorMapper;
    }

    @PostMapping
    @Operation(summary = "Assign specialty to doctor", description = "Maps a medical specialty to a doctor profile")
    public ResponseEntity<Void> assignSpecialty(
            @RequestParam Long doctorId,
            @RequestParam Long specialtyId) {
        doctorSpecialtyService.assignSpecialty(doctorId, specialtyId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    @Operation(summary = "Remove specialty from doctor", description = "Removes a mapped specialty from a doctor profile")
    public ResponseEntity<Void> removeSpecialty(
            @RequestParam Long doctorId,
            @RequestParam Long specialtyId) {
        doctorSpecialtyService.removeSpecialty(doctorId, specialtyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get doctor specialties", description = "Retrieves all specialties assigned to a specific doctor")
    public ResponseEntity<List<SpecialtyResponse>> getSpecialtiesForDoctor(@PathVariable Long doctorId) {
        List<Specialty> specialties = doctorSpecialtyService.getSpecialtiesForDoctor(doctorId);
        return ResponseEntity.ok(specialtyMapper.toResponseList(specialties));
    }

    @GetMapping("/specialty/{specialtyId}")
    @Operation(summary = "Get doctors for specialty", description = "Retrieves all doctors practicing a specific specialty")
    public ResponseEntity<List<DoctorResponse>> getDoctorsForSpecialty(@PathVariable Long specialtyId) {
        List<Doctor> doctors = doctorSpecialtyService.getDoctorsForSpecialty(specialtyId);
        List<DoctorResponse> responses = doctors.stream().map(doc -> {
            List<Specialty> specialties = doctorSpecialtyService.getSpecialtiesForDoctor(doc.getId());
            return doctorMapper.toResponse(doc, specialties);
        }).toList();
        return ResponseEntity.ok(responses);
    }
}
