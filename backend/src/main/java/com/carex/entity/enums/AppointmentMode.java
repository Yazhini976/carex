package com.carex.entity.enums;

/**
 * Represents the consultation mode for an appointment or slot.
 * Stored as a VARCHAR string in the respective {@code mode} columns.
 *
 * <p>Business rule: Online and offline appointments must use different doctors.
 * This rule is enforced in {@code AppointmentService}, not here.</p>
 */
public enum AppointmentMode {
    ONLINE,
    OFFLINE
}
