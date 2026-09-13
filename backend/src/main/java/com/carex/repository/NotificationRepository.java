package com.carex.repository;

import com.carex.entity.Notification;
import com.carex.entity.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status);
    List<Notification> findByStatus(NotificationStatus status);
    long countByUserIdAndStatus(Long userId, NotificationStatus status);
    long countByStatus(NotificationStatus status);
    boolean existsByAppointmentIdAndType(Long appointmentId, String type);
}
