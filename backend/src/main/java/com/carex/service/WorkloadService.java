package com.carex.service;

import com.carex.entity.WorkloadSnapshot;

import java.time.LocalDate;
import java.util.List;

public interface WorkloadService {
    /**
     * Calculates the current workload for a doctor and persists a snapshot.
     *
     * <p>Workload score formula (documented):<br>
     * score = (appointmentCount * 1.0 + completedCount * 0.5 - cancelledCount * 0.2 - noShowCount * 0.3) / maxLoad * 100<br>
     * Where maxLoad = configurable ceiling (default 20 appointments/day).
     * Score is capped at 100.0.</p>
     */
    WorkloadSnapshot captureSnapshot(Long doctorId);

    WorkloadSnapshot getLatestSnapshot(Long doctorId);

    List<WorkloadSnapshot> getSnapshotHistory(Long doctorId);

    /**
     * Returns a summary of all doctors' current workload scores for admin/dashboard use.
     */
    List<WorkloadSnapshot> captureAllDoctorSnapshots();
}
