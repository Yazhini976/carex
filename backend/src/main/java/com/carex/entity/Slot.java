package com.carex.entity;

import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.SlotStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents a specific bookable time slot for a doctor on a given date.
 * Maps to the {@code slots} table.
 *
 * <p>Slots are generated from {@link DoctorAvailability} windows.
 * A slot can be linked to at most one {@link Appointment} (via the unique {@code slot_id} constraint on appointments).</p>
 *
 * <p>The unique constraint {@code uq_slot(doctor_id, slot_date, start_time)} prevents duplicate slots.</p>
 */
@Entity
@Table(name = "slots")
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    /** The availability window this slot was generated from. May be null for manually created slots. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "availability_id")
    private DoctorAvailability availability;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    private AppointmentMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SlotStatus status = SlotStatus.AVAILABLE;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public Slot() {}

    public Slot(Doctor doctor, LocalDate slotDate, LocalTime startTime, LocalTime endTime, AppointmentMode mode) {
        this.doctor = doctor;
        this.slotDate = slotDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.mode = mode;
        this.status = SlotStatus.AVAILABLE;
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public DoctorAvailability getAvailability() { return availability; }
    public void setAvailability(DoctorAvailability availability) { this.availability = availability; }

    public LocalDate getSlotDate() { return slotDate; }
    public void setSlotDate(LocalDate slotDate) { this.slotDate = slotDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public AppointmentMode getMode() { return mode; }
    public void setMode(AppointmentMode mode) { this.mode = mode; }

    public SlotStatus getStatus() { return status; }
    public void setStatus(SlotStatus status) { this.status = status; }

    // -------------------------------------------------------------------------
    // equals / hashCode
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Slot)) return false;
        Slot slot = (Slot) o;
        return Objects.equals(id, slot.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Slot{id=" + id + ", slotDate=" + slotDate + ", startTime=" + startTime + ", status=" + status + "}";
    }
}
