package com.carex.repository;

import com.carex.entity.WaitTimePrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaitTimePredictionRepository extends JpaRepository<WaitTimePrediction, Long> {
    Optional<WaitTimePrediction> findByAppointmentId(Long appointmentId);
    List<WaitTimePrediction> findByAppointmentDoctorId(Long doctorId);
}
