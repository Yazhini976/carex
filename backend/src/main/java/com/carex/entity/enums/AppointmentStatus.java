package com.carex.entity.enums;

/**
 * Represents the lifecycle status of an appointment.
 * Stored as a VARCHAR string in the {@code appointments.status} column
 * and in {@code appointment_status_history.old_status} / {@code new_status}.
 */
public enum AppointmentStatus {
    BOOKED,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
