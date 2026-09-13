package com.carex.entity;

import com.carex.entity.enums.AppointmentMode;
import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents a doctor's recurring weekly availability window.
 * Maps to the {@code doctor_availability} table.
 *
 * <p>A doctor can have multiple availability records covering different
 * days of the week and consultation modes (ONLINE/OFFLINE).</p>
 */
@Entity
@Table(name = "doctor_availability")
public class DoctorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    /**
     * Day of the week as an integer.
     * Convention: 0 = Sunday, 1 = Monday, ..., 6 = Saturday (ISO: 1 = Monday).
     * Stored as {@code day_of_week} column.
     */
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    private AppointmentMode mode;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public DoctorAvailability() {}

    public DoctorAvailability(Doctor doctor, Integer dayOfWeek, LocalTime startTime, LocalTime endTime, AppointmentMode mode) {
        this.doctor = doctor;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.mode = mode;
        this.isActive = Boolean.TRUE;
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public AppointmentMode getMode() { return mode; }
    public void setMode(AppointmentMode mode) { this.mode = mode; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    // -------------------------------------------------------------------------
    // equals / hashCode
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DoctorAvailability)) return false;
        DoctorAvailability that = (DoctorAvailability) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "DoctorAvailability{id=" + id + ", dayOfWeek=" + dayOfWeek + ", mode=" + mode + "}";
    }
}
