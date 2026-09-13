package com.carex.service.impl;

import com.carex.entity.Patient;
import com.carex.entity.User;
import com.carex.exception.BusinessRuleException;
import com.carex.exception.ResourceNotFoundException;
import com.carex.repository.PatientRepository;
import com.carex.service.PatientService;
import com.carex.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PatientServiceImpl implements PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientServiceImpl.class);

    private final PatientRepository patientRepository;
    private final UserService userService;

    public PatientServiceImpl(PatientRepository patientRepository, UserService userService) {
        this.patientRepository = patientRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public Patient createPatient(Long userId, LocalDate dateOfBirth, String gender, String emergencyContact) {
        User user = userService.getUserById(userId);
        if (patientRepository.existsByUserId(userId)) {
            throw new BusinessRuleException("A patient profile already exists for user id: " + userId);
        }
        Patient patient = new Patient(user);
        patient.setDateOfBirth(dateOfBirth);
        patient.setGender(gender);
        patient.setEmergencyContact(emergencyContact);
        Patient saved = patientRepository.save(patient);
        log.info("Created patient id={} for userId={}", saved.getId(), userId);
        return saved;
    }

    @Override
    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Patient", id));
    }

    @Override
    public Patient getPatientByUserId(Long userId) {
        return patientRepository.findByUserId(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Patient", "userId", String.valueOf(userId)));
    }

    @Override
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Override
    @Transactional
    public Patient updatePatient(Long id, LocalDate dateOfBirth, String gender, String emergencyContact) {
        Patient patient = getPatientById(id);
        if (dateOfBirth != null) patient.setDateOfBirth(dateOfBirth);
        if (gender != null) patient.setGender(gender);
        if (emergencyContact != null) patient.setEmergencyContact(emergencyContact);
        return patientRepository.save(patient);
    }

    @Override
    @Transactional
    public void deletePatient(Long id) {
        getPatientById(id); // ensure exists
        patientRepository.deleteById(id);
        log.info("Deleted patient id={}", id);
    }
}
