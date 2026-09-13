package com.carex.exception;

import com.carex.entity.enums.AppointmentStatus;

/**
 * Thrown when an appointment status transition is not valid.
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>"Appointment cannot be completed from CANCELLED state"</li>
 *   <li>"Appointment cannot be confirmed from NO_SHOW state"</li>
 * </ul>
 */
public class InvalidAppointmentStateException extends RuntimeException {

    public InvalidAppointmentStateException(String message) {
        super(message);
    }

    public static InvalidAppointmentStateException forTransition(
            AppointmentStatus current, AppointmentStatus target) {
        return new InvalidAppointmentStateException(
                "Appointment cannot transition from " + current + " to " + target);
    }
}
