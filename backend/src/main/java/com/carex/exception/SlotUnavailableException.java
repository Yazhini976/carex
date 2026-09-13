package com.carex.exception;

/**
 * Thrown when a slot is no longer available for booking.
 *
 * <p>This can occur when two concurrent requests attempt to book the same slot.
 * The service layer uses pessimistic locking to prevent silent data corruption,
 * but this exception is raised if the slot is already in BOOKED or BLOCKED state
 * at the point of the booking check.</p>
 *
 * <p>Example: "Slot is no longer available"</p>
 */
public class SlotUnavailableException extends RuntimeException {

    public SlotUnavailableException(String message) {
        super(message);
    }

    public static SlotUnavailableException forSlot(Long slotId) {
        return new SlotUnavailableException("Slot " + slotId + " is no longer available");
    }
}
