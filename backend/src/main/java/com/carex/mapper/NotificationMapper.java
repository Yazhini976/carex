package com.carex.mapper;

import com.carex.dto.notification.NotificationResponse;
import com.carex.entity.Notification;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        Long userId = notification.getUser() != null ? notification.getUser().getId() : null;
        String userName = (notification.getUser() != null) ? notification.getUser().getName() : null;
        Long appointmentId = notification.getAppointment() != null ? notification.getAppointment().getId() : null;

        return new NotificationResponse(
                notification.getId(),
                userId,
                userName,
                appointmentId,
                notification.getType(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getCreatedAt(),
                notification.getSentAt()
        );
    }

    public List<NotificationResponse> toResponseList(List<Notification> notifications) {
        if (notifications == null) {
            return Collections.emptyList();
        }
        return notifications.stream().map(this::toResponse).toList();
    }
}
