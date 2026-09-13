package com.carex.notification;

import com.carex.entity.Appointment;
import com.carex.entity.enums.AppointmentStatus;
import com.carex.repository.AppointmentRepository;
import com.carex.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Scheduled runner for automated appointment reminders with duplicate prevention and clock abstraction.
 */
@Component
public class ReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationTemplateService templateService;
    private final NotificationDispatcher dispatcher;
    private final int minutesBefore;
    private final boolean schedulerEnabled;
    private Clock clock;

    @Autowired
    public ReminderScheduler(AppointmentRepository appointmentRepository,
                             NotificationRepository notificationRepository,
                             NotificationTemplateService templateService,
                             NotificationDispatcher dispatcher,
                             @Value("${carex.reminder.minutes-before:30}") int minutesBefore,
                             @Value("${carex.reminder.scheduler.enabled:true}") boolean schedulerEnabled) {
        this.appointmentRepository = appointmentRepository;
        this.notificationRepository = notificationRepository;
        this.templateService = templateService;
        this.dispatcher = dispatcher;
        this.minutesBefore = minutesBefore;
        this.schedulerEnabled = schedulerEnabled;
        this.clock = Clock.systemDefaultZone();
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    /**
     * Runs every 5 minutes to scan for upcoming appointments requiring reminders.
     */
    @Scheduled(fixedRate = 300000, initialDelay = 10000) // 5 minutes
    @Transactional(readOnly = true)
    public void runScheduledReminderScan() {
        if (!schedulerEnabled) {
            log.debug("Reminder scheduler is disabled (carex.reminder.scheduler.enabled=false)");
            return;
        }

        LocalDateTime now = LocalDateTime.now(clock);
        int processed = processUpcomingReminders(now);
        if (processed > 0) {
            log.info("[REMINDER-SCAN] Dispatched {} appointment reminders at referenceTime={}", processed, now);
        }
    }

    /**
     * Core deterministic logic for identifying and dispatching reminders within the threshold window.
     *
     * @param referenceTime current virtual/real time
     * @return count of newly dispatched reminders
     */
    public int processUpcomingReminders(LocalDateTime referenceTime) {
        LocalDate today = referenceTime.toLocalDate();
        LocalTime nowTime = referenceTime.toLocalTime();
        LocalTime windowEnd = nowTime.plusMinutes(minutesBefore);

        // Fetch appointments scheduled for today
        List<Appointment> todayAppointments = appointmentRepository.findBySlotDate(today);

        int count = 0;
        for (Appointment apt : todayAppointments) {
            // Must be in active state (BOOKED or CONFIRMED)
            if (apt.getStatus() != AppointmentStatus.BOOKED && apt.getStatus() != AppointmentStatus.CONFIRMED) {
                continue;
            }

            LocalTime slotStartTime = apt.getSlot().getStartTime();

            // Check if slot start time is within the reminder window: [nowTime - 5min buffer, nowTime + minutesBefore]
            // We allow slot times between now and windowEnd
            boolean inWindow = !slotStartTime.isBefore(nowTime.minusMinutes(2)) && !slotStartTime.isAfter(windowEnd);

            if (inWindow) {
                // Idempotency check: strictly prevent duplicate reminders
                boolean alreadyReminded = notificationRepository.existsByAppointmentIdAndType(
                        apt.getId(),
                        NotificationType.APPOINTMENT_REMINDER.name()
                );

                if (alreadyReminded) {
                    log.debug("Appointment #{} already has an APPOINTMENT_REMINDER recorded. Skipping.", apt.getId());
                    continue;
                }

                // Build and dispatch reminder
                NotificationEvent reminderEvent = templateService.buildAppointmentReminderEvent(apt);
                dispatcher.dispatch(reminderEvent);
                count++;
                log.info("[REMINDER-TRIGGERED] Sent reminder for appointmentId={} patient={} startTime={}",
                        apt.getId(), apt.getPatient().getId(), slotStartTime);
            }
        }

        return count;
    }
}
