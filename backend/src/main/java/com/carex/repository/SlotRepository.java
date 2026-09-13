package com.carex.repository;

import com.carex.entity.Slot;
import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.SlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {

    List<Slot> findByDoctorIdAndSlotDate(Long doctorId, LocalDate slotDate);

    List<Slot> findByDoctorIdAndSlotDateAndStatus(Long doctorId, LocalDate slotDate, SlotStatus status);

    List<Slot> findByDoctorIdAndStatus(Long doctorId, SlotStatus status);

    List<Slot> findByDoctorIdAndSlotDateBetweenAndStatus(
            Long doctorId, LocalDate startDate, LocalDate endDate, SlotStatus status);

    boolean existsByDoctorIdAndSlotDateAndStartTime(Long doctorId, LocalDate slotDate, LocalTime startTime);

    List<Slot> findByAvailabilityId(Long availabilityId);

    List<Slot> findByDoctorIdAndModeAndStatus(Long doctorId, AppointmentMode mode, SlotStatus status);

    /**
     * Pessimistic write lock — prevents concurrent bookings of the same slot.
     * Used by AppointmentServiceImpl during the booking transaction.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Slot s WHERE s.id = :id")
    Optional<Slot> findByIdWithLock(@Param("id") Long id);
}
