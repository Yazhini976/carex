package com.carex.notification.impl;

import com.carex.entity.Appointment;
import com.carex.entity.Notification;
import com.carex.entity.User;
import com.carex.entity.enums.NotificationStatus;
import com.carex.exception.ResourceNotFoundException;
import com.carex.notification.InAppNotificationService;
import com.carex.notification.NotificationEvent;
import com.carex.notification.dto.NotificationStatsResponse;
import com.carex.repository.AppointmentRepository;
import com.carex.repository.NotificationRepository;
import com.carex.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class InAppNotificationServiceImpl implements InAppNotificationService {

    private static final Logger log = LoggerFactory.getLogger(InAppNotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    public InAppNotificationServiceImpl(NotificationRepository notificationRepository,
                                        UserRepository userRepository,
                                        AppointmentRepository appointmentRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    @Transactional
    public Notification recordNotification(NotificationEvent event) {
        if (event == null || event.getRecipientUserId() == null) {
            log.warn("Cannot record in-app notification: missing recipient user id in event={}", event);
            return null;
        }

        User user = userRepository.findById(event.getRecipientUserId())
                .orElseThrow(() -> ResourceNotFoundException.of("User", event.getRecipientUserId()));

        Appointment appointment = null;
        if (event.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(event.getAppointmentId()).orElse(null);
        }

        String typeStr = event.getNotificationType() != null
                ? event.getNotificationType().name()
                : "GENERAL_ALERT";

        Notification notification = new Notification(user, typeStr, event.getMessage());
        notification.setAppointment(appointment);
        notification.setStatus(NotificationStatus.PENDING);

        Notification saved = notificationRepository.save(notification);
        log.info("Recorded in-app notification id={} type={} userId={} appointmentId={}",
                saved.getId(), typeStr, user.getId(), event.getAppointmentId());
        return saved;
    }

    @Override
    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.PENDING);
    }

    @Override
    @Transactional
    public Notification markRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", id));
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        List<Notification> pending = notificationRepository.findByUserIdAndStatus(userId, NotificationStatus.PENDING);
        LocalDateTime now = LocalDateTime.now();
        for (Notification n : pending) {
            n.setStatus(NotificationStatus.SENT);
            n.setSentAt(now);
        }
        notificationRepository.saveAll(pending);
        log.info("Marked {} pending notifications as read for userId={}", pending.size(), userId);
    }

    @Override
    public NotificationStatsResponse getStats() {
        long total = notificationRepository.count();
        long sent = notificationRepository.countByStatus(NotificationStatus.SENT);
        long pending = notificationRepository.countByStatus(NotificationStatus.PENDING);
        long failed = notificationRepository.countByStatus(NotificationStatus.FAILED);

        double rate = total > 0 ? ((double) sent / total) * 100.0 : 100.0;
        return new NotificationStatsResponse(total, sent, pending, failed, Math.round(rate * 10.0) / 10.0);
    }
}
