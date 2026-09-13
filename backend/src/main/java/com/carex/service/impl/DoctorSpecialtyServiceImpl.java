package com.carex.service.impl;

import com.carex.entity.Doctor;
import com.carex.entity.DoctorSpecialty;
import com.carex.entity.DoctorSpecialtyId;
import com.carex.entity.Specialty;
import com.carex.exception.BusinessRuleException;
import com.carex.exception.DuplicateResourceException;
import com.carex.exception.ResourceNotFoundException;
import com.carex.repository.DoctorSpecialtyRepository;
import com.carex.service.DoctorService;
import com.carex.service.DoctorSpecialtyService;
import com.carex.service.SpecialtyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DoctorSpecialtyServiceImpl implements DoctorSpecialtyService {

    private static final Logger log = LoggerFactory.getLogger(DoctorSpecialtyServiceImpl.class);

    private final DoctorSpecialtyRepository doctorSpecialtyRepository;
    private final DoctorService doctorService;
    private final SpecialtyService specialtyService;

    public DoctorSpecialtyServiceImpl(DoctorSpecialtyRepository doctorSpecialtyRepository,
                                       DoctorService doctorService,
                                       SpecialtyService specialtyService) {
        this.doctorSpecialtyRepository = doctorSpecialtyRepository;
        this.doctorService = doctorService;
        this.specialtyService = specialtyService;
    }

    @Override
    @Transactional
    public DoctorSpecialty assignSpecialty(Long doctorId, Long specialtyId) {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        Specialty specialty = specialtyService.getSpecialtyById(specialtyId);
        if (doctorSpecialtyRepository.existsByDoctorIdAndSpecialtyId(doctorId, specialtyId)) {
            throw new DuplicateResourceException(
                    "Doctor " + doctorId + " is already assigned to specialty " + specialtyId);
        }
        DoctorSpecialty mapping = new DoctorSpecialty(doctor, specialty);
        DoctorSpecialty saved = doctorSpecialtyRepository.save(mapping);
        log.info("Assigned specialty {} to doctor {}", specialtyId, doctorId);
        return saved;
    }

    @Override
    @Transactional
    public void removeSpecialty(Long doctorId, Long specialtyId) {
        DoctorSpecialty mapping = doctorSpecialtyRepository
                .findByDoctorIdAndSpecialtyId(doctorId, specialtyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor-specialty mapping not found for doctorId=" + doctorId + " specialtyId=" + specialtyId));
        doctorSpecialtyRepository.delete(mapping);
        log.info("Removed specialty {} from doctor {}", specialtyId, doctorId);
    }

    @Override
    public List<Specialty> getSpecialtiesForDoctor(Long doctorId) {
        return doctorSpecialtyRepository.findByDoctorId(doctorId)
                .stream()
                .map(DoctorSpecialty::getSpecialty)
                .toList();
    }

    @Override
    public List<Doctor> getDoctorsForSpecialty(Long specialtyId) {
        return doctorSpecialtyRepository.findBySpecialtyId(specialtyId)
                .stream()
                .map(DoctorSpecialty::getDoctor)
                .toList();
    }

    @Override
    public boolean isAssigned(Long doctorId, Long specialtyId) {
        return doctorSpecialtyRepository.existsByDoctorIdAndSpecialtyId(doctorId, specialtyId);
    }
}
