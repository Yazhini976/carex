package com.carex.entity;

import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.AppointmentStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Central entity representing a doctor-patient appointment booking.
 * Maps to the {@code appointments} table.
 *
 * <p>Each appointment exclusively owns one {@link Slot} — enforced by the UNIQUE constraint
 * on {@code appointments.slot_id}.</p>
 *
 * <p><strong>Business rule (NOT enforced here):</strong> Online and offline appointments
 * must use different doctors. This is a cross-row transactional rule enforced in
 * {@code AppointmentService}.</p>
 */
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    /**
     * The slot this appointment occupies.
     * UNIQUE constraint on {@code slot_id} ensures a slot can only be booked once.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false, unique = true)
    private Slot slot;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    private AppointmentMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AppointmentStatus status = AppointmentStatus.BOOKED;

    @Column(name = "booked_at", nullable = false, updatable = false)
    private LocalDateTime bookedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /** History of status transitions for this appointment. */
    @JsonIgnore
    @OneToMany(mappedBy = "appointment", fetch = FetchType.LAZY)
    private List<AppointmentStatusHistory> statusHistory = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Lifecycle hooks
    // -------------------------------------------------------------------------

    @PrePersist
    protected void onCreate() {
        this.bookedAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Appointment() {}

    public Appointment(Patient patient, Doctor doctor, Specialty specialty, Slot slot, AppointmentMode mode) {
        this.patient = patient;
        this.doctor = doctor;
        this.specialty = specialty;
        this.slot = slot;
        this.mode = mode;
        this.status = AppointmentStatus.BOOKED;
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public Specialty getSpecialty() { return specialty; }
    public void setSpecialty(Specialty specialty) { this.specialty = specialty; }

    public Slot getSlot() { return slot; }
    public void setSlot(Slot slot) { this.slot = slot; }

    public AppointmentMode getMode() { return mode; }
    public void setMode(AppointmentMode mode) { this.mode = mode; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public LocalDateTime getBookedAt() { return bookedAt; }
    public void setBookedAt(LocalDateTime bookedAt) { this.bookedAt = bookedAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<AppointmentStatusHistory> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<AppointmentStatusHistory> statusHistory) { this.statusHistory = statusHistory; }

    // -------------------------------------------------------------------------
    // equals / hashCode
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Appointment)) return false;
        Appointment that = (Appointment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Appointment{id=" + id + ", status=" + status + ", mode=" + mode + "}";
    }
}
