package com.carex.repository;

import com.carex.entity.WorkloadSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkloadSnapshotRepository extends JpaRepository<WorkloadSnapshot, Long> {
    List<WorkloadSnapshot> findByDoctorIdOrderBySnapshotTimeDesc(Long doctorId);

    Optional<WorkloadSnapshot> findTopByDoctorIdOrderBySnapshotTimeDesc(Long doctorId);

    @Query("SELECT w FROM WorkloadSnapshot w WHERE w.doctor.id = :doctorId " +
           "AND w.snapshotTime BETWEEN :from AND :to ORDER BY w.snapshotTime ASC")
    List<WorkloadSnapshot> findByDoctorIdAndTimeRange(
            @Param("doctorId") Long doctorId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
