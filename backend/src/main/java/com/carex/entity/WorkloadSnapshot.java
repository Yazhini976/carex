package com.carex.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Point-in-time snapshot of a doctor's workload metrics.
 * Maps to the {@code workload_snapshots} table.
 *
 * <p>Snapshots are used by the doctor workload intelligence module to monitor
 * and forecast doctor capacity over time.</p>
 */
@Entity
@Table(name = "workload_snapshots")
public class WorkloadSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "snapshot_time", nullable = false, updatable = false)
    private LocalDateTime snapshotTime;

    @Column(name = "appointment_count", nullable = false)
    private Integer appointmentCount = 0;

    @Column(name = "completed_count", nullable = false)
    private Integer completedCount = 0;

    @Column(name = "cancelled_count", nullable = false)
    private Integer cancelledCount = 0;

    @Column(name = "no_show_count", nullable = false)
    private Integer noShowCount = 0;

    /** Nullable — composite workload score computed by the intelligence module. */
    @Column(name = "workload_score", precision = 5, scale = 2)
    private BigDecimal workloadScore;

    // -------------------------------------------------------------------------
    // Lifecycle hooks
    // -------------------------------------------------------------------------

    @PrePersist
    protected void onCreate() {
        this.snapshotTime = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public WorkloadSnapshot() {}

    public WorkloadSnapshot(Doctor doctor) {
        this.doctor = doctor;
        this.appointmentCount = 0;
        this.completedCount = 0;
        this.cancelledCount = 0;
        this.noShowCount = 0;
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public LocalDateTime getSnapshotTime() { return snapshotTime; }
    public void setSnapshotTime(LocalDateTime snapshotTime) { this.snapshotTime = snapshotTime; }

    public Integer getAppointmentCount() { return appointmentCount; }
    public void setAppointmentCount(Integer appointmentCount) { this.appointmentCount = appointmentCount; }

    public Integer getCompletedCount() { return completedCount; }
    public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }

    public Integer getCancelledCount() { return cancelledCount; }
    public void setCancelledCount(Integer cancelledCount) { this.cancelledCount = cancelledCount; }

    public Integer getNoShowCount() { return noShowCount; }
    public void setNoShowCount(Integer noShowCount) { this.noShowCount = noShowCount; }

    public BigDecimal getWorkloadScore() { return workloadScore; }
    public void setWorkloadScore(BigDecimal workloadScore) { this.workloadScore = workloadScore; }

    // -------------------------------------------------------------------------
    // equals / hashCode
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkloadSnapshot)) return false;
        WorkloadSnapshot that = (WorkloadSnapshot) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "WorkloadSnapshot{id=" + id + ", doctor=" + (doctor != null ? doctor.getId() : null) + ", snapshotTime=" + snapshotTime + "}";
    }
}
