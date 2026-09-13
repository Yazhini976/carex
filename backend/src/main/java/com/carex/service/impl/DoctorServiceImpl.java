package com.carex.service.impl;

import com.carex.entity.Doctor;
import com.carex.entity.User;
import com.carex.exception.BusinessRuleException;
import com.carex.exception.DuplicateResourceException;
import com.carex.exception.ResourceNotFoundException;
import com.carex.repository.DoctorRepository;
import com.carex.service.DoctorService;
import com.carex.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class DoctorServiceImpl implements DoctorService {

    private static final Logger log = LoggerFactory.getLogger(DoctorServiceImpl.class);

    private final DoctorRepository doctorRepository;
    private final UserService userService;

    public DoctorServiceImpl(DoctorRepository doctorRepository, UserService userService) {
        this.doctorRepository = doctorRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public Doctor createDoctor(Long userId, String licenseNumber, String qualification,
                                Integer experienceYears, BigDecimal consultationFee) {
        User user = userService.getUserById(userId);
        if (doctorRepository.existsByUserId(userId)) {
            throw new BusinessRuleException("A doctor profile already exists for user id: " + userId);
        }
        if (doctorRepository.existsByLicenseNumber(licenseNumber)) {
            throw DuplicateResourceException.of("Doctor", "licenseNumber", licenseNumber);
        }
        Doctor doctor = new Doctor(user, licenseNumber);
        doctor.setQualification(qualification);
        doctor.setExperienceYears(experienceYears != null ? experienceYears : 0);
        doctor.setConsultationFee(consultationFee);
        Doctor saved = doctorRepository.save(doctor);
        log.info("Created doctor id={} licenseNumber={}", saved.getId(), licenseNumber);
        return saved;
    }

    @Override
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Doctor", id));
    }

    @Override
    public Optional<Doctor> findDoctorByUserId(Long userId) {
        return doctorRepository.findByUserId(userId);
    }

    @Override
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Override
    public List<Doctor> getActiveDoctors() {
        return doctorRepository.findByIsActive(true);
    }

    @Override
    @Transactional
    public Doctor updateDoctor(Long id, String qualification, Integer experienceYears, BigDecimal consultationFee) {
        Doctor doctor = getDoctorById(id);
        if (qualification != null) doctor.setQualification(qualification);
        if (experienceYears != null) doctor.setExperienceYears(experienceYears);
        if (consultationFee != null) doctor.setConsultationFee(consultationFee);
        return doctorRepository.save(doctor);
    }

    @Override
    @Transactional
    public Doctor setActiveStatus(Long id, boolean active) {
        Doctor doctor = getDoctorById(id);
        doctor.setIsActive(active);
        log.info("Set doctor id={} active={}", id, active);
        return doctorRepository.save(doctor);
    }

    @Override
    @Transactional
    public void deleteDoctor(Long id) {
        getDoctorById(id);
        doctorRepository.deleteById(id);
        log.info("Deleted doctor id={}", id);
    }
}
