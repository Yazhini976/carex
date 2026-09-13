package com.carex.entity;

import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.WaitlistStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a patient's entry on a waitlist for a doctor or specialty.
 * Maps to the {@code waitlist} table.
 *
 * <p>Preferred doctor is optional — patients may waitlist for any available doctor
 * in a given specialty.</p>
 */
@Entity
@Table(name = "waitlist")
public class Waitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    /** Optional preferred doctor. Null means the patient accepts any doctor in the specialty. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_doctor_id")
    private Doctor preferredDoctor;

    @Column(name = "preferred_date")
    private LocalDate preferredDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_mode", length = 20)
    private AppointmentMode preferredMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WaitlistStatus status = WaitlistStatus.WAITING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "fulfilled_at")
    private LocalDateTime fulfilledAt;

    // -------------------------------------------------------------------------
    // Lifecycle hooks
    // -------------------------------------------------------------------------

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Waitlist() {}

    public Waitlist(Patient patient, Specialty specialty) {
        this.patient = patient;
        this.specialty = specialty;
        this.status = WaitlistStatus.WAITING;
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Specialty getSpecialty() { return specialty; }
    public void setSpecialty(Specialty specialty) { this.specialty = specialty; }

    public Doctor getPreferredDoctor() { return preferredDoctor; }
    public void setPreferredDoctor(Doctor preferredDoctor) { this.preferredDoctor = preferredDoctor; }

    public LocalDate getPreferredDate() { return preferredDate; }
    public void setPreferredDate(LocalDate preferredDate) { this.preferredDate = preferredDate; }

    public AppointmentMode getPreferredMode() { return preferredMode; }
    public void setPreferredMode(AppointmentMode preferredMode) { this.preferredMode = preferredMode; }

    public WaitlistStatus getStatus() { return status; }
    public void setStatus(WaitlistStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getFulfilledAt() { return fulfilledAt; }
    public void setFulfilledAt(LocalDateTime fulfilledAt) { this.fulfilledAt = fulfilledAt; }

    // -------------------------------------------------------------------------
    // equals / hashCode
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Waitlist)) return false;
        Waitlist waitlist = (Waitlist) o;
        return Objects.equals(id, waitlist.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Waitlist{id=" + id + ", status=" + status + "}";
    }
}
