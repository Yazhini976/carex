package com.carex.service;

import com.carex.entity.Doctor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DoctorService {
    Doctor createDoctor(Long userId, String licenseNumber, String qualification,
                        Integer experienceYears, BigDecimal consultationFee);
    Doctor getDoctorById(Long id);
    Optional<Doctor> findDoctorByUserId(Long userId);
    List<Doctor> getAllDoctors();
    List<Doctor> getActiveDoctors();
    Doctor updateDoctor(Long id, String qualification, Integer experienceYears, BigDecimal consultationFee);
    Doctor setActiveStatus(Long id, boolean active);
    void deleteDoctor(Long id);
}
