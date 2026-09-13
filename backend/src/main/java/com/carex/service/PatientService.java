package com.carex.service;

import com.carex.entity.Patient;

import java.time.LocalDate;
import java.util.List;

public interface PatientService {
    Patient createPatient(Long userId, LocalDate dateOfBirth, String gender, String emergencyContact);
    Patient getPatientById(Long id);
    Patient getPatientByUserId(Long userId);
    List<Patient> getAllPatients();
    Patient updatePatient(Long id, LocalDate dateOfBirth, String gender, String emergencyContact);
    void deletePatient(Long id);
}
