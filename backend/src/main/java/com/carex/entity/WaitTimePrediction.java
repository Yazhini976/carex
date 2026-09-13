package com.carex.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Stores the AI-generated wait time prediction for an appointment.
 * Maps to the {@code wait_time_predictions} table.
 *
 * <p>Captures both the predicted wait time (in minutes) and, once available,
 * the actual wait time for model feedback and accuracy tracking.</p>
 */
@Entity
@Table(name = "wait_time_predictions")
public class WaitTimePrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(name = "predicted_minutes", nullable = false)
    private Integer predictedMinutes;

    /** Nullable — actual wait time is filled in after the appointment completes. */
    @Column(name = "actual_minutes")
    private Integer actualMinutes;

    @Column(name = "predicted_at", nullable = false, updatable = false)
    private LocalDateTime predictedAt;

    // -------------------------------------------------------------------------
    // Lifecycle hooks
    // -------------------------------------------------------------------------

    @PrePersist
    protected void onCreate() {
        this.predictedAt = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public WaitTimePrediction() {}

    public WaitTimePrediction(Appointment appointment, Integer predictedMinutes) {
        this.appointment = appointment;
        this.predictedMinutes = predictedMinutes;
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public Integer getPredictedMinutes() { return predictedMinutes; }
    public void setPredictedMinutes(Integer predictedMinutes) { this.predictedMinutes = predictedMinutes; }

    public Integer getActualMinutes() { return actualMinutes; }
    public void setActualMinutes(Integer actualMinutes) { this.actualMinutes = actualMinutes; }

    public LocalDateTime getPredictedAt() { return predictedAt; }
    public void setPredictedAt(LocalDateTime predictedAt) { this.predictedAt = predictedAt; }

    // -------------------------------------------------------------------------
    // equals / hashCode
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WaitTimePrediction)) return false;
        WaitTimePrediction that = (WaitTimePrediction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "WaitTimePrediction{id=" + id + ", predictedMinutes=" + predictedMinutes + "}";
    }
}
