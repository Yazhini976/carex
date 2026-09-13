package com.carex.repository;

import com.carex.entity.DoctorAvailability;
import com.carex.entity.enums.AppointmentMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {
    List<DoctorAvailability> findByDoctorId(Long doctorId);
    List<DoctorAvailability> findByDoctorIdAndIsActive(Long doctorId, Boolean isActive);
    List<DoctorAvailability> findByDoctorIdAndDayOfWeek(Long doctorId, Integer dayOfWeek);
    List<DoctorAvailability> findByDoctorIdAndMode(Long doctorId, AppointmentMode mode);
    boolean existsByDoctorIdAndDayOfWeekAndMode(Long doctorId, Integer dayOfWeek, AppointmentMode mode);
}
