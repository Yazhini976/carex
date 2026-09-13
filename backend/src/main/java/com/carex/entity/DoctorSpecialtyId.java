package com.carex.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for {@link DoctorSpecialty}.
 * Maps to the {@code (doctor_id, specialty_id)} PRIMARY KEY on the {@code doctor_specialties} table.
 */
@Embeddable
public class DoctorSpecialtyId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "specialty_id", nullable = false)
    private Long specialtyId;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public DoctorSpecialtyId() {}

    public DoctorSpecialtyId(Long doctorId, Long specialtyId) {
        this.doctorId = doctorId;
        this.specialtyId = specialtyId;
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getSpecialtyId() { return specialtyId; }
    public void setSpecialtyId(Long specialtyId) { this.specialtyId = specialtyId; }

    // -------------------------------------------------------------------------
    // equals / hashCode — required for composite key correctness
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DoctorSpecialtyId)) return false;
        DoctorSpecialtyId that = (DoctorSpecialtyId) o;
        return Objects.equals(doctorId, that.doctorId) &&
               Objects.equals(specialtyId, that.specialtyId);
    }

    @Override
    public int hashCode() { return Objects.hash(doctorId, specialtyId); }
}
