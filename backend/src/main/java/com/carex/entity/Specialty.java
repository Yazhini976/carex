package com.carex.entity;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * Represents a medical specialty offered by CAREX doctors.
 * Maps to the {@code specialties} table.
 *
 * <p>Specialty names must be globally unique.
 * Specialties drive the AI-assisted specialty recommendation feature.</p>
 */
@Entity
@Table(name = "specialties")
public class Specialty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 120)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Specialty() {}

    public Specialty(String name) {
        this.name = name;
        this.isActive = Boolean.TRUE;
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    // -------------------------------------------------------------------------
    // equals / hashCode
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Specialty)) return false;
        Specialty specialty = (Specialty) o;
        return Objects.equals(name, specialty.name);
    }

    @Override
    public int hashCode() { return Objects.hash(name); }

    @Override
    public String toString() {
        return "Specialty{id=" + id + ", name='" + name + "'}";
    }
}
