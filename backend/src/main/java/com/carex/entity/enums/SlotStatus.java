package com.carex.entity.enums;

/**
 * Represents the availability status of a doctor slot.
 * Stored as a VARCHAR string in the {@code slots.status} column.
 */
public enum SlotStatus {
    AVAILABLE,
    BOOKED,
    BLOCKED
}
