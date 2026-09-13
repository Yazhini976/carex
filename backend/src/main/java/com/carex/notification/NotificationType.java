package com.carex.notification;

/**
 * Supported notification event types in the CAREX platform.
 */
public enum NotificationType {
    APPOINTMENT_BOOKED,
    APPOINTMENT_CONFIRMED,
    APPOINTMENT_CANCELLED,
    APPOINTMENT_COMPLETED,
    APPOINTMENT_NO_SHOW,
    APPOINTMENT_REMINDER,
    WAITLIST_SLOT_AVAILABLE,
    WAITLIST_OFFER_EXPIRED,
    DOCTOR_UNAVAILABLE,
    SCHEDULING_ALERT
}
