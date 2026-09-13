package com.carex.controller;

import com.carex.dto.notification.NotificationCreateRequest;
import com.carex.dto.notification.NotificationResponse;
import com.carex.entity.Notification;
import com.carex.mapper.NotificationMapper;
import com.carex.notification.dto.NotificationStatsResponse;
import com.carex.notification.dto.UnreadCountResponse;
import com.carex.security.SecurityUtils;
import com.carex.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "User notifications, in-app queue, and dispatch metrics endpoints")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    public NotificationController(NotificationService notificationService, NotificationMapper notificationMapper) {
        this.notificationService = notificationService;
        this.notificationMapper = notificationMapper;
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user notifications", description = "Retrieves all notifications targeted to a specific user (enforces ownership)")
    public ResponseEntity<List<NotificationResponse>> getNotificationsForUser(@PathVariable Long userId) {
        if (!SecurityUtils.isCurrentUser(userId)) {
            throw new AccessDeniedException("You are not authorized to view notifications for user " + userId);
        }
        List<Notification> list = notificationService.getNotificationsForUser(userId);
        return ResponseEntity.ok(notificationMapper.toResponseList(list));
    }

    @GetMapping("/user/{userId}/unread-count")
    @Operation(summary = "Get unread count", description = "Retrieves the count of pending/unread notifications for a user")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(@PathVariable Long userId) {
        if (!SecurityUtils.isCurrentUser(userId)) {
            throw new AccessDeniedException("You are not authorized to view notifications for user " + userId);
        }
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(new UnreadCountResponse(userId, count));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification read", description = "Updates notification status to SENT / read")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable Long id) {
        Notification notification = notificationService.markRead(id);
        return ResponseEntity.ok(notificationMapper.toResponse(notification));
    }

    @PutMapping("/user/{userId}/read-all")
    @Operation(summary = "Mark all notifications read", description = "Bulk marks all pending notifications as read for a user")
    public ResponseEntity<Void> markAllRead(@PathVariable Long userId) {
        if (!SecurityUtils.isCurrentUser(userId)) {
            throw new AccessDeniedException("You are not authorized to modify notifications for user " + userId);
        }
        notificationService.markAllRead(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get notification system health stats", description = "Returns overall delivery metrics and status counts for administration")
    public ResponseEntity<NotificationStatsResponse> getStats() {
        return ResponseEntity.ok(notificationService.getStats());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get pending notifications", description = "Retrieves notifications awaiting dispatch")
    public ResponseEntity<List<NotificationResponse>> getPendingNotifications() {
        List<Notification> list = notificationService.getPendingNotifications();
        return ResponseEntity.ok(notificationMapper.toResponseList(list));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create notification", description = "Queues a new notification for a user or appointment")
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationCreateRequest request) {
        Notification notification;
        if (request.getAppointmentId() != null) {
            notification = notificationService.createNotification(
                    request.getUserId(),
                    request.getAppointmentId(),
                    request.getType(),
                    request.getMessage()
            );
        } else {
            notification = notificationService.createSystemNotification(
                    request.getUserId(),
                    request.getType(),
                    request.getMessage()
            );
        }
        return new ResponseEntity<>(notificationMapper.toResponse(notification), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/sent")
    @Operation(summary = "Mark notification sent", description = "Updates notification status to SENT with timestamp")
    public ResponseEntity<NotificationResponse> markSent(@PathVariable Long id) {
        Notification notification = notificationService.markSent(id);
        return ResponseEntity.ok(notificationMapper.toResponse(notification));
    }

    @PutMapping("/{id}/failed")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark notification failed", description = "Updates notification status to FAILED")
    public ResponseEntity<NotificationResponse> markFailed(@PathVariable Long id) {
        Notification notification = notificationService.markFailed(id);
        return ResponseEntity.ok(notificationMapper.toResponse(notification));
    }
}
