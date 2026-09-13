package com.carex.repository;

import com.carex.entity.Waitlist;
import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {

    List<Waitlist> findByPatientId(Long patientId);

    List<Waitlist> findByPatientIdAndStatus(Long patientId, WaitlistStatus status);

    List<Waitlist> findByStatus(WaitlistStatus status);

    List<Waitlist> findBySpecialtyIdAndStatus(Long specialtyId, WaitlistStatus status);

    @Query("SELECT w FROM Waitlist w WHERE w.specialty.id = :specialtyId " +
           "AND w.status = com.carex.entity.enums.WaitlistStatus.WAITING " +
           "AND (w.preferredDoctor IS NULL OR w.preferredDoctor.id = :doctorId) " +
           "AND (w.preferredMode IS NULL OR w.preferredMode = :mode) " +
           "AND (w.preferredDate IS NULL OR w.preferredDate >= :date) " +
           "ORDER BY w.createdAt ASC")
    List<Waitlist> findMatchingWaiting(
            @Param("specialtyId") Long specialtyId,
            @Param("doctorId") Long doctorId,
            @Param("mode") AppointmentMode mode,
            @Param("date") LocalDate date);
}
