package com.carex.service.impl;

import com.carex.entity.Appointment;
import com.carex.entity.Notification;
import com.carex.entity.User;
import com.carex.entity.enums.NotificationStatus;
import com.carex.exception.ResourceNotFoundException;
import com.carex.notification.InAppNotificationService;
import com.carex.notification.NotificationDispatcher;
import com.carex.notification.NotificationEvent;
import com.carex.notification.dto.NotificationStatsResponse;
import com.carex.repository.NotificationRepository;
import com.carex.service.AppointmentService;
import com.carex.service.NotificationService;
import com.carex.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final AppointmentService appointmentService;
    private final InAppNotificationService inAppNotificationService;
    private final NotificationDispatcher notificationDispatcher;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserService userService,
                                   @Lazy AppointmentService appointmentService,
                                   InAppNotificationService inAppNotificationService,
                                   @Lazy NotificationDispatcher notificationDispatcher) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
        this.appointmentService = appointmentService;
        this.inAppNotificationService = inAppNotificationService;
        this.notificationDispatcher = notificationDispatcher;
    }

    @Override
    @Transactional
    public Notification createNotification(Long userId, Long appointmentId, String type, String message) {
        User user = userService.getUserById(userId);
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        Notification notification = new Notification(user, type, message);
        notification.setAppointment(appointment);
        Notification saved = notificationRepository.save(notification);
        log.info("Created notification id={} type={} userId={} appointmentId={}", saved.getId(), type, userId, appointmentId);
        return saved;
    }

    @Override
    @Transactional
    public Notification createSystemNotification(Long userId, String type, String message) {
        User user = userService.getUserById(userId);
        Notification notification = new Notification(user, type, message);
        Notification saved = notificationRepository.save(notification);
        log.info("Created system notification id={} type={} userId={}", saved.getId(), type, userId);
        return saved;
    }

    @Override
    public List<Notification> getNotificationsForUser(Long userId) {
        return inAppNotificationService.getNotificationsForUser(userId);
    }

    @Override
    public List<Notification> getPendingNotifications() {
        return notificationRepository.findByStatus(NotificationStatus.PENDING);
    }

    @Override
    @Transactional
    public Notification markSent(Long id) {
        return inAppNotificationService.markRead(id);
    }

    @Override
    @Transactional
    public Notification markRead(Long id) {
        return inAppNotificationService.markRead(id);
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        inAppNotificationService.markAllRead(userId);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return inAppNotificationService.getUnreadCount(userId);
    }

    @Override
    public NotificationStatsResponse getStats() {
        return inAppNotificationService.getStats();
    }

    @Override
    public void sendNotificationEvent(NotificationEvent event) {
        notificationDispatcher.dispatch(event);
    }

    @Override
    @Transactional
    public Notification markFailed(Long id) {
        Notification notification = getById(id);
        notification.setStatus(NotificationStatus.FAILED);
        return notificationRepository.save(notification);
    }

    private Notification getById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", id));
    }
}
