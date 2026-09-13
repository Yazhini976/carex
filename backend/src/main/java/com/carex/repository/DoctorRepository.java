package com.carex.repository;

import com.carex.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    boolean existsByLicenseNumber(String licenseNumber);
    Optional<Doctor> findByLicenseNumber(String licenseNumber);
    List<Doctor> findByIsActive(Boolean isActive);
}
