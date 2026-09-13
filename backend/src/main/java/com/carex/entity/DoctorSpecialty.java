package com.carex.entity;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * Junction entity representing the many-to-many relationship between {@link Doctor} and {@link Specialty}.
 * Maps to the {@code doctor_specialties} table.
 *
 * <p>Uses a composite primary key {@link DoctorSpecialtyId} corresponding to
 * {@code PRIMARY KEY (doctor_id, specialty_id)} in the schema.</p>
 */
@Entity
@Table(name = "doctor_specialties")
public class DoctorSpecialty {

    @EmbeddedId
    private DoctorSpecialtyId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("doctorId")
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("specialtyId")
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public DoctorSpecialty() {}

    public DoctorSpecialty(Doctor doctor, Specialty specialty) {
        this.doctor = doctor;
        this.specialty = specialty;
        this.id = new DoctorSpecialtyId(doctor.getId(), specialty.getId());
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public DoctorSpecialtyId getId() { return id; }
    public void setId(DoctorSpecialtyId id) { this.id = id; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public Specialty getSpecialty() { return specialty; }
    public void setSpecialty(Specialty specialty) { this.specialty = specialty; }

    // -------------------------------------------------------------------------
    // equals / hashCode — delegates to composite key
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DoctorSpecialty)) return false;
        DoctorSpecialty that = (DoctorSpecialty) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "DoctorSpecialty{id=" + id + "}";
    }
}
