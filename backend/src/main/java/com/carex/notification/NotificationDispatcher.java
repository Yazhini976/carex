package com.carex.notification;

/**
 * Central event bus / dispatcher routing notifications across configured channels.
 */
public interface NotificationDispatcher {

    /**
     * Dispatches the event to the appropriate channels (IN_APP, EMAIL, ALL).
     *
     * @param event the notification payload
     */
    void dispatch(NotificationEvent event);
}
