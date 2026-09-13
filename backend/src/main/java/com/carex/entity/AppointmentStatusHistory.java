package com.carex.entity;

import com.carex.entity.enums.AppointmentStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Audit trail for appointment status transitions.
 * Maps to the {@code appointment_status_history} table.
 *
 * <p>Each record captures a single status change event: who changed it, when, and from/to which states.</p>
 */
@Entity
@Table(name = "appointment_status_history")
public class AppointmentStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    /** Previous status before the transition. Nullable for the first recorded state. */
    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 30)
    private AppointmentStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private AppointmentStatus newStatus;

    /** The user who triggered the status change. Nullable for system-initiated changes. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    // -------------------------------------------------------------------------
    // Lifecycle hooks
    // -------------------------------------------------------------------------

    @PrePersist
    protected void onCreate() {
        this.changedAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public AppointmentStatusHistory() {}

    public AppointmentStatusHistory(Appointment appointment, AppointmentStatus oldStatus,
                                    AppointmentStatus newStatus, User changedBy) {
        this.appointment = appointment;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public AppointmentStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(AppointmentStatus oldStatus) { this.oldStatus = oldStatus; }

    public AppointmentStatus getNewStatus() { return newStatus; }
    public void setNewStatus(AppointmentStatus newStatus) { this.newStatus = newStatus; }

    public User getChangedBy() { return changedBy; }
    public void setChangedBy(User changedBy) { this.changedBy = changedBy; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    // -------------------------------------------------------------------------
    // equals / hashCode
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppointmentStatusHistory)) return false;
        AppointmentStatusHistory that = (AppointmentStatusHistory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "AppointmentStatusHistory{id=" + id + ", oldStatus=" + oldStatus + ", newStatus=" + newStatus + "}";
    }
}
