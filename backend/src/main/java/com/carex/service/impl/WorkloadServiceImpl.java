package com.carex.service.impl;

import com.carex.entity.Appointment;
import com.carex.entity.Doctor;
import com.carex.entity.WorkloadSnapshot;
import com.carex.entity.enums.AppointmentStatus;
import com.carex.exception.ResourceNotFoundException;
import com.carex.repository.AppointmentRepository;
import com.carex.repository.WorkloadSnapshotRepository;
import com.carex.service.DoctorService;
import com.carex.service.WorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Workload service that calculates doctor workload scores and persists snapshots.
 *
 * <p>Scoring formula (documented and configurable):<br>
 * raw = appointmentCount * 1.0 + completedCount * 0.5 - cancelledCount * 0.2 - noShowCount * 0.3<br>
 * score = min((raw / MAX_LOAD) * 100, 100.0)<br>
 * Where MAX_LOAD = 20 (maximum appointments considered "full capacity").</p>
 */
@Service
@Transactional(readOnly = true)
public class WorkloadServiceImpl implements WorkloadService {

    private static final Logger log = LoggerFactory.getLogger(WorkloadServiceImpl.class);

    /** Maximum appointments treated as 100% load. Configurable in future via properties. */
    private static final double MAX_LOAD = 20.0;

    private final WorkloadSnapshotRepository snapshotRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;

    public WorkloadServiceImpl(WorkloadSnapshotRepository snapshotRepository,
                                AppointmentRepository appointmentRepository,
                                DoctorService doctorService) {
        this.snapshotRepository = snapshotRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorService = doctorService;
    }

    @Override
    @Transactional
    public WorkloadSnapshot captureSnapshot(Long doctorId) {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        LocalDate today = LocalDate.now();

        List<Appointment> todayAppointments = appointmentRepository.findByDoctorIdAndSlotDate(doctorId, today);

        int total = todayAppointments.size();
        int completed = (int) todayAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
        int cancelled = (int) todayAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count();
        int noShow = (int) todayAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.NO_SHOW).count();

        BigDecimal score = computeScore(total, completed, cancelled, noShow);

        WorkloadSnapshot snapshot = new WorkloadSnapshot(doctor);
        snapshot.setAppointmentCount(total);
        snapshot.setCompletedCount(completed);
        snapshot.setCancelledCount(cancelled);
        snapshot.setNoShowCount(noShow);
        snapshot.setWorkloadScore(score);

        WorkloadSnapshot saved = snapshotRepository.save(snapshot);
        log.info("Captured workload snapshot id={} doctor={} score={}", saved.getId(), doctorId, score);
        return saved;
    }

    @Override
    public WorkloadSnapshot getLatestSnapshot(Long doctorId) {
        return snapshotRepository.findTopByDoctorIdOrderBySnapshotTimeDesc(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No workload snapshots found for doctor: " + doctorId));
    }

    @Override
    public List<WorkloadSnapshot> getSnapshotHistory(Long doctorId) {
        return snapshotRepository.findByDoctorIdOrderBySnapshotTimeDesc(doctorId);
    }

    @Override
    @Transactional
    public List<WorkloadSnapshot> captureAllDoctorSnapshots() {
        return doctorService.getActiveDoctors().stream()
                .map(doctor -> captureSnapshot(doctor.getId()))
                .toList();
    }

    // -------------------------------------------------------------------------
    // Private formula
    // -------------------------------------------------------------------------

    /**
     * Computes the workload score based on appointment metrics.
     *
     * <p>Formula:<br>
     * raw = appointments * 1.0 + completed * 0.5 - cancelled * 0.2 - noShow * 0.3<br>
     * score = min(raw / MAX_LOAD * 100, 100.0)</p>
     */
    private BigDecimal computeScore(int appointments, int completed, int cancelled, int noShow) {
        double raw = appointments * 1.0 + completed * 0.5 - cancelled * 0.2 - noShow * 0.3;
        double score = Math.min((raw / MAX_LOAD) * 100.0, 100.0);
        score = Math.max(score, 0.0);
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }
}
