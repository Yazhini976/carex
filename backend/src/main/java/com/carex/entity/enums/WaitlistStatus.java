package com.carex.entity.enums;

/**
 * Represents the lifecycle status of a patient's waitlist entry.
 * Stored as a VARCHAR string in the {@code waitlist.status} column.
 */
public enum WaitlistStatus {
    WAITING,
    OFFERED,
    FULFILLED,
    EXPIRED,
    CANCELLED
}
