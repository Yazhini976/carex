package com.carex.repository;

import com.carex.entity.Appointment;
import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);

    List<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

    List<Appointment> findByStatus(AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.mode = :mode " +
           "AND a.status NOT IN (com.carex.entity.enums.AppointmentStatus.CANCELLED, " +
           "com.carex.entity.enums.AppointmentStatus.NO_SHOW)")
    List<Appointment> findActiveByPatientIdAndMode(
            @Param("patientId") Long patientId, @Param("mode") AppointmentMode mode);

    @Query("SELECT a FROM Appointment a WHERE a.slot.slotDate = :date")
    List<Appointment> findBySlotDate(@Param("date") LocalDate date);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.slot.slotDate = :date")
    List<Appointment> findByDoctorIdAndSlotDate(
            @Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    @Query("SELECT a FROM Appointment a WHERE a.bookedAt BETWEEN :from AND :to")
    List<Appointment> findByBookedAtBetween(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT a FROM Appointment a WHERE a.slot.slotDate = :date AND a.doctor.id = :doctorId " +
           "AND a.status NOT IN (com.carex.entity.enums.AppointmentStatus.CANCELLED, " +
           "com.carex.entity.enums.AppointmentStatus.NO_SHOW)")
    List<Appointment> findActiveByDoctorIdAndDate(
            @Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.slot.slotDate = :date AND a.status = :status")
    long countByDoctorIdAndDateAndStatus(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("status") AppointmentStatus status);
}
