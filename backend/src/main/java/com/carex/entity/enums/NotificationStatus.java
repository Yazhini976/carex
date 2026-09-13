package com.carex.entity.enums;

/**
 * Represents the delivery status of a notification.
 * Stored as a VARCHAR string in the {@code notifications.status} column.
 */
public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED
}
