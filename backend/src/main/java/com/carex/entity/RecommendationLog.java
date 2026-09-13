package com.carex.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Records an AI-assisted specialty navigation/recommendation event.
 * Maps to the {@code recommendation_logs} table.
 *
 * <p>Captures the patient's symptom/complaint input, the recommended specialty,
 * and the AI's reasoning. Both patient and specialty references are nullable
 * to support anonymous and partial recommendation scenarios.</p>
 *
 * <p><strong>This entity represents specialty navigation guidance, NOT medical diagnosis.</strong></p>
 */
@Entity
@Table(name = "recommendation_logs")
public class RecommendationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nullable — recommendation may be generated for an anonymous or pre-login session. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(name = "input_text", nullable = false, columnDefinition = "TEXT")
    private String inputText;

    /** Nullable — model may fail to map to any specialty. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_specialty_id")
    private Specialty recommendedSpecialty;

    @Column(name = "recommendation_reason", columnDefinition = "TEXT")
    private String recommendationReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

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

    public RecommendationLog() {}

    public RecommendationLog(String inputText) {
        this.inputText = inputText;
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public String getInputText() { return inputText; }
    public void setInputText(String inputText) { this.inputText = inputText; }

    public Specialty getRecommendedSpecialty() { return recommendedSpecialty; }
    public void setRecommendedSpecialty(Specialty recommendedSpecialty) { this.recommendedSpecialty = recommendedSpecialty; }

    public String getRecommendationReason() { return recommendationReason; }
    public void setRecommendationReason(String recommendationReason) { this.recommendationReason = recommendationReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // -------------------------------------------------------------------------
    // equals / hashCode
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecommendationLog)) return false;
        RecommendationLog that = (RecommendationLog) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "RecommendationLog{id=" + id + "}";
    }
}
