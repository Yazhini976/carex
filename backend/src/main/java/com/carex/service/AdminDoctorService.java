package com.carex.service;

import com.carex.dto.doctor.AdminDoctorRequest;
import com.carex.dto.doctor.DoctorResponse;
import com.carex.entity.Doctor;
import com.carex.entity.Specialty;
import com.carex.entity.User;
import com.carex.entity.enums.Role;
import com.carex.exception.DuplicateResourceException;
import com.carex.mapper.DoctorMapper;
import com.carex.repository.DoctorRepository;
import com.carex.repository.SpecialtyRepository;
import com.carex.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for admin-initiated one-shot doctor registration.
 * Creates both the user account (role=DOCTOR) and the doctor profile in one transaction.
 */
@Service
public class AdminDoctorService {

    private static final Logger log = LoggerFactory.getLogger(AdminDoctorService.class);

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorMapper doctorMapper;
    private final DoctorSpecialtyService doctorSpecialtyService;

    public AdminDoctorService(UserRepository userRepository,
                              DoctorRepository doctorRepository,
                              SpecialtyRepository specialtyRepository,
                              PasswordEncoder passwordEncoder,
                              DoctorMapper doctorMapper,
                              DoctorSpecialtyService doctorSpecialtyService) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.specialtyRepository = specialtyRepository;
        this.passwordEncoder = passwordEncoder;
        this.doctorMapper = doctorMapper;
        this.doctorSpecialtyService = doctorSpecialtyService;
    }

    @Transactional
    public DoctorResponse registerDoctor(AdminDoctorRequest req) {
        // 1. Check email uniqueness
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new DuplicateResourceException("Email is already registered: " + req.getEmail());
        }

        // 2. Create user account with DOCTOR role
        String hashed = passwordEncoder.encode(req.getPassword());
        User user = new User(req.getName(), req.getEmail(), hashed, Role.DOCTOR);
        user.setPhone(req.getPhone());
        user.setIsActive(Boolean.TRUE);
        user = userRepository.save(user);
        log.info("Admin created user id={} email={} role=DOCTOR", user.getId(), user.getEmail());

        // 3. Generate a unique license number automatically
        String licenseNumber = "LIC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 4. Create doctor profile
        Doctor doctor = new Doctor(user, licenseNumber);
        doctor.setQualification(req.getQualification());
        doctor.setExperienceYears(req.getExperienceYears() != null ? req.getExperienceYears() : 0);
        doctor.setConsultationFee(req.getConsultationFee() != null ? req.getConsultationFee() : BigDecimal.ZERO);
        doctor = doctorRepository.save(doctor);
        log.info("Admin created doctor id={} licenseNumber={}", doctor.getId(), licenseNumber);

        // 5. Assign specialty if provided
        final Long savedDoctorId = doctor.getId();
        if (req.getSpecialtyId() != null) {
            specialtyRepository.findById(req.getSpecialtyId()).ifPresent(spec -> {
                try {
                    doctorSpecialtyService.assignSpecialty(savedDoctorId, spec.getId());
                } catch (Exception ignored) {
                    // specialty assignment is best-effort
                }
            });
        }

        List<Specialty> specialties = doctorSpecialtyService.getSpecialtiesForDoctor(doctor.getId());
        return doctorMapper.toResponse(doctor, specialties);
    }
}
