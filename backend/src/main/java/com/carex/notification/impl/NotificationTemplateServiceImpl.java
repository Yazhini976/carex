package com.carex.notification.impl;

import com.carex.entity.Appointment;
import com.carex.entity.Doctor;
import com.carex.entity.Specialty;
import com.carex.entity.enums.AppointmentMode;
import com.carex.notification.NotificationChannel;
import com.carex.notification.NotificationEvent;
import com.carex.notification.NotificationPriority;
import com.carex.notification.NotificationTemplateService;
import com.carex.notification.NotificationType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    @Override
    public NotificationEvent buildAppointmentBookedEvent(Appointment appointment) {
        String patientName = appointment.getPatient().getUser().getName();
        String doctorName = appointment.getDoctor().getUser().getName();
        String specialty = appointment.getSpecialty().getName();
        String dateStr = appointment.getSlot().getSlotDate().format(DATE_FMT);
        String timeStr = appointment.getSlot().getStartTime().format(TIME_FMT);
        String modeStr = appointment.getMode().name();
        String instructions = getModeInstructions(appointment.getMode());

        String subject = "CAREX — Appointment Booked (#" + appointment.getId() + ")";
        String text = String.format(
                "Hello %s, your appointment (#%d) with Dr. %s (%s) has been booked for %s at %s. %s",
                patientName, appointment.getId(), doctorName, specialty, dateStr, timeStr, instructions
        );

        String html = buildHtmlCard("Appointment Booked", "#0284c7", patientName,
                "Your appointment has been successfully booked.",
                new String[][]{
                        {"Appointment ID", "#" + appointment.getId()},
                        {"Doctor", "Dr. " + doctorName},
                        {"Specialty", specialty},
                        {"Date & Time", dateStr + " at " + timeStr},
                        {"Consultation Mode", modeStr}
                }, instructions);

        return NotificationEvent.builder()
                .recipientUserId(appointment.getPatient().getUser().getId())
                .recipientEmail(appointment.getPatient().getUser().getEmail())
                .recipientName(patientName)
                .appointmentId(appointment.getId())
                .notificationType(NotificationType.APPOINTMENT_BOOKED)
                .priority(NotificationPriority.NORMAL)
                .channel(NotificationChannel.ALL)
                .subject(subject)
                .message(text)
                .htmlBody(html)
                .build();
    }

    @Override
    public NotificationEvent buildAppointmentConfirmedEvent(Appointment appointment) {
        String patientName = appointment.getPatient().getUser().getName();
        String doctorName = appointment.getDoctor().getUser().getName();
        String specialty = appointment.getSpecialty().getName();
        String dateStr = appointment.getSlot().getSlotDate().format(DATE_FMT);
        String timeStr = appointment.getSlot().getStartTime().format(TIME_FMT);
        String instructions = getModeInstructions(appointment.getMode());

        String subject = "CAREX — Appointment Confirmed (#" + appointment.getId() + ")";
        String text = String.format(
                "Hello %s, your appointment (#%d) with Dr. %s on %s at %s is CONFIRMED. %s",
                patientName, appointment.getId(), doctorName, dateStr, timeStr, instructions
        );

        String html = buildHtmlCard("Appointment Confirmed", "#16a34a", patientName,
                "Your appointment has been officially confirmed.",
                new String[][]{
                        {"Appointment ID", "#" + appointment.getId()},
                        {"Doctor", "Dr. " + doctorName},
                        {"Specialty", specialty},
                        {"Date & Time", dateStr + " at " + timeStr},
                        {"Consultation Mode", appointment.getMode().name()}
                }, instructions);

        return NotificationEvent.builder()
                .recipientUserId(appointment.getPatient().getUser().getId())
                .recipientEmail(appointment.getPatient().getUser().getEmail())
                .recipientName(patientName)
                .appointmentId(appointment.getId())
                .notificationType(NotificationType.APPOINTMENT_CONFIRMED)
                .priority(NotificationPriority.NORMAL)
                .channel(NotificationChannel.ALL)
                .subject(subject)
                .message(text)
                .htmlBody(html)
                .build();
    }

    @Override
    public NotificationEvent buildAppointmentCancelledEvent(Appointment appointment) {
        String patientName = appointment.getPatient().getUser().getName();
        String doctorName = appointment.getDoctor().getUser().getName();
        String dateStr = appointment.getSlot().getSlotDate().format(DATE_FMT);
        String timeStr = appointment.getSlot().getStartTime().format(TIME_FMT);

        String subject = "CAREX — Appointment Cancelled (#" + appointment.getId() + ")";
        String text = String.format(
                "Hello %s, your appointment (#%d) with Dr. %s scheduled for %s at %s has been CANCELLED.",
                patientName, appointment.getId(), doctorName, dateStr, timeStr
        );

        String html = buildHtmlCard("Appointment Cancelled", "#dc2626", patientName,
                "Your appointment has been cancelled. You can book a new slot through your dashboard at any time.",
                new String[][]{
                        {"Appointment ID", "#" + appointment.getId()},
                        {"Doctor", "Dr. " + doctorName},
                        {"Cancelled Slot", dateStr + " at " + timeStr}
                }, "If you need immediate assistance or a new appointment, visit the CAREX booking portal.");

        return NotificationEvent.builder()
                .recipientUserId(appointment.getPatient().getUser().getId())
                .recipientEmail(appointment.getPatient().getUser().getEmail())
                .recipientName(patientName)
                .appointmentId(appointment.getId())
                .notificationType(NotificationType.APPOINTMENT_CANCELLED)
                .priority(NotificationPriority.NORMAL)
                .channel(NotificationChannel.ALL)
                .subject(subject)
                .message(text)
                .htmlBody(html)
                .build();
    }

    @Override
    public NotificationEvent buildAppointmentCompletedEvent(Appointment appointment) {
        String patientName = appointment.getPatient().getUser().getName();
        String doctorName = appointment.getDoctor().getUser().getName();

        String subject = "CAREX — Consultation Completed (#" + appointment.getId() + ")";
        String text = String.format(
                "Hello %s, your consultation (#%d) with Dr. %s is marked as COMPLETED. Thank you for choosing CAREX.",
                patientName, appointment.getId(), doctorName
        );

        String html = buildHtmlCard("Consultation Completed", "#0d9488", patientName,
                "Your consultation has concluded successfully.",
                new String[][]{
                        {"Appointment ID", "#" + appointment.getId()},
                        {"Doctor", "Dr. " + doctorName},
                        {"Status", "COMPLETED"}
                }, "Consultation records are available on your CAREX patient portal.");

        return NotificationEvent.builder()
                .recipientUserId(appointment.getPatient().getUser().getId())
                .recipientEmail(appointment.getPatient().getUser().getEmail())
                .recipientName(patientName)
                .appointmentId(appointment.getId())
                .notificationType(NotificationType.APPOINTMENT_COMPLETED)
                .priority(NotificationPriority.LOW)
                .channel(NotificationChannel.ALL)
                .subject(subject)
                .message(text)
                .htmlBody(html)
                .build();
    }

    @Override
    public NotificationEvent buildAppointmentNoShowEvent(Appointment appointment) {
        String patientName = appointment.getPatient().getUser().getName();
        String doctorName = appointment.getDoctor().getUser().getName();

        String subject = "CAREX — Appointment Marked No-Show (#" + appointment.getId() + ")";
        String text = String.format(
                "Hello %s, you were marked as no-show for appointment (#%d) with Dr. %s. Please reach out to reschedule if needed.",
                patientName, appointment.getId(), doctorName
        );

        String html = buildHtmlCard("Appointment Missed", "#e11d48", patientName,
                "You were marked as not in attendance for your scheduled consultation.",
                new String[][]{
                        {"Appointment ID", "#" + appointment.getId()},
                        {"Doctor", "Dr. " + doctorName},
                        {"Status", "NO-SHOW"}
                }, "To book a new slot, please log in to your CAREX portal.");

        return NotificationEvent.builder()
                .recipientUserId(appointment.getPatient().getUser().getId())
                .recipientEmail(appointment.getPatient().getUser().getEmail())
                .recipientName(patientName)
                .appointmentId(appointment.getId())
                .notificationType(NotificationType.APPOINTMENT_NO_SHOW)
                .priority(NotificationPriority.NORMAL)
                .channel(NotificationChannel.ALL)
                .subject(subject)
                .message(text)
                .htmlBody(html)
                .build();
    }

    @Override
    public NotificationEvent buildAppointmentReminderEvent(Appointment appointment) {
        String patientName = appointment.getPatient().getUser().getName();
        String doctorName = appointment.getDoctor().getUser().getName();
        String specialty = appointment.getSpecialty().getName();
        String timeStr = appointment.getSlot().getStartTime().format(TIME_FMT);
        String instructions = getModeInstructions(appointment.getMode());

        String subject = "CAREX Reminder — Appointment with Dr. " + doctorName + " at " + timeStr;
        String text = String.format(
                "REMINDER: Hello %s, your appointment (#%d) with Dr. %s (%s) starts soon at %s. %s",
                patientName, appointment.getId(), doctorName, specialty, timeStr, instructions
        );

        String html = buildHtmlCard("Upcoming Appointment Reminder", "#f59e0b", patientName,
                "Your consultation is starting shortly.",
                new String[][]{
                        {"Appointment ID", "#" + appointment.getId()},
                        {"Doctor", "Dr. " + doctorName},
                        {"Specialty", specialty},
                        {"Scheduled Time", timeStr},
                        {"Mode", appointment.getMode().name()}
                }, instructions);

        return NotificationEvent.builder()
                .recipientUserId(appointment.getPatient().getUser().getId())
                .recipientEmail(appointment.getPatient().getUser().getEmail())
                .recipientName(patientName)
                .appointmentId(appointment.getId())
                .notificationType(NotificationType.APPOINTMENT_REMINDER)
                .priority(NotificationPriority.HIGH)
                .channel(NotificationChannel.ALL)
                .subject(subject)
                .message(text)
                .htmlBody(html)
                .build();
    }

    @Override
    public NotificationEvent buildWaitlistSlotAvailableEvent(Long userId, String userEmail, String userName,
                                                            Doctor doctor, Specialty specialty,
                                                            LocalDate date, LocalTime startTime, AppointmentMode mode) {
        String doctorName = doctor.getUser().getName();
        String specName = specialty.getName();
        String dateStr = date.format(DATE_FMT);
        String timeStr = startTime.format(TIME_FMT);
        String instructions = getModeInstructions(mode);

        String subject = "CAREX Waitlist Alert — Slot Available with Dr. " + doctorName;
        String text = String.format(
                "Hello %s, an appointment slot has opened up with Dr. %s (%s) on %s at %s (%s). Please claim this slot in your dashboard.",
                userName, doctorName, specName, dateStr, timeStr, mode
        );

        String html = buildHtmlCard("Waitlist Slot Available!", "#6366f1", userName,
                "A slot has become available matching your waitlist request.",
                new String[][]{
                        {"Doctor", "Dr. " + doctorName},
                        {"Specialty", specName},
                        {"Available Date", dateStr},
                        {"Available Time", timeStr},
                        {"Mode", mode.name()}
                }, "Log in to CAREX now to confirm your booking before the slot is released to the next candidate.");

        return NotificationEvent.builder()
                .recipientUserId(userId)
                .recipientEmail(userEmail)
                .recipientName(userName)
                .notificationType(NotificationType.WAITLIST_SLOT_AVAILABLE)
                .priority(NotificationPriority.HIGH)
                .channel(NotificationChannel.ALL)
                .subject(subject)
                .message(text)
                .htmlBody(html)
                .build();
    }

    @Override
    public NotificationEvent buildDoctorUnavailableEvent(Appointment appointment, String doctorName) {
        String patientName = appointment.getPatient().getUser().getName();
        String dateStr = appointment.getSlot().getSlotDate().format(DATE_FMT);
        String timeStr = appointment.getSlot().getStartTime().format(TIME_FMT);

        String subject = "CAREX Alert — Doctor Unavailability for Appointment #" + appointment.getId();
        String text = String.format(
                "Urgent: Dr. %s is unavailable for your scheduled appointment on %s at %s. CAREX has prepared alternative options. Please review your dashboard.",
                doctorName, dateStr, timeStr
        );

        String html = buildHtmlCard("Schedule Disruption Alert", "#dc2626", patientName,
                "Your scheduled doctor has reported an unexpected unavailability.",
                new String[][]{
                        {"Appointment ID", "#" + appointment.getId()},
                        {"Assigned Doctor", "Dr. " + doctorName},
                        {"Original Slot", dateStr + " at " + timeStr}
                }, "Alternative scheduling options are available. Please log in to select an alternative doctor or slot.");

        return NotificationEvent.builder()
                .recipientUserId(appointment.getPatient().getUser().getId())
                .recipientEmail(appointment.getPatient().getUser().getEmail())
                .recipientName(patientName)
                .appointmentId(appointment.getId())
                .notificationType(NotificationType.DOCTOR_UNAVAILABLE)
                .priority(NotificationPriority.CRITICAL)
                .channel(NotificationChannel.ALL)
                .subject(subject)
                .message(text)
                .htmlBody(html)
                .build();
    }

    @Override
    public NotificationEvent buildSchedulingAlertEvent(Long userId, String userEmail, String userName,
                                                      String subject, String message, NotificationPriority priority) {
        String html = buildHtmlCard("System Alert", "#475569", userName,
                message, new String[][]{}, "Please check your CAREX portal for further details.");

        return NotificationEvent.builder()
                .recipientUserId(userId)
                .recipientEmail(userEmail)
                .recipientName(userName)
                .notificationType(NotificationType.SCHEDULING_ALERT)
                .priority(priority != null ? priority : NotificationPriority.NORMAL)
                .channel(NotificationChannel.ALL)
                .subject("CAREX — " + subject)
                .message(message)
                .htmlBody(html)
                .build();
    }

    private String getModeInstructions(AppointmentMode mode) {
        if (mode == AppointmentMode.ONLINE) {
            return "Consultation Mode: ONLINE. Please join from your CAREX dashboard at the scheduled time.";
        } else {
            return "Consultation Mode: OFFLINE. Please arrive at the clinic before your scheduled appointment.";
        }
    }

    private String buildHtmlCard(String title, String primaryColor, String recipientName,
                                 String leadText, String[][] fields, String footerNote) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><body style=\"margin:0;padding:20px;background-color:#f8fafc;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;\">");
        sb.append("<div style=\"max-width:560px;margin:0 auto;background:#ffffff;border-radius:12px;overflow:hidden;border:1px solid #e2e8f0;box-shadow:0 4px 6px -1px rgba(0,0,0,0.05);\">");
        
        // Header
        sb.append("<div style=\"background-color:").append(primaryColor).append(";padding:24px;color:#ffffff;\">");
        sb.append("<div style=\"font-size:12px;font-weight:700;letter-spacing:1px;text-transform:uppercase;opacity:0.9;\">CAREX INTELLIGENT HEALTH</div>");
        sb.append("<h1 style=\"margin:8px 0 0 0;font-size:20px;font-weight:700;\">").append(title).append("</h1>");
        sb.append("</div>");

        // Content
        sb.append("<div style=\"padding:24px;color:#334155;\">");
        sb.append("<p style=\"margin:0 0 16px 0;font-size:15px;\">Hello <strong>").append(recipientName).append("</strong>,</p>");
        sb.append("<p style=\"margin:0 0 20px 0;font-size:14px;color:#475569;\">").append(leadText).append("</p>");

        if (fields != null && fields.length > 0) {
            sb.append("<table style=\"width:100%;border-collapse:collapse;margin-bottom:20px;\">");
            for (String[] field : fields) {
                sb.append("<tr style=\"border-bottom:1px solid #f1f5f9;\">");
                sb.append("<td style=\"padding:10px 0;font-size:13px;color:#64748b;font-weight:600;width:40%;\">").append(field[0]).append("</td>");
                sb.append("<td style=\"padding:10px 0;font-size:13px;color:#0f172a;font-weight:600;\">").append(field[1]).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
        }

        if (footerNote != null && !footerNote.isBlank()) {
            sb.append("<div style=\"background:#f1f5f9;border-left:4px solid ").append(primaryColor).append(";padding:12px 16px;border-radius:4px;font-size:13px;color:#334155;margin-bottom:20px;\">");
            sb.append(footerNote);
            sb.append("</div>");
        }

        sb.append("<div style=\"text-align:center;padding-top:12px;\">");
        sb.append("<a href=\"http://localhost:5173\" style=\"display:inline-block;background:").append(primaryColor).append(";color:#ffffff;text-decoration:none;padding:10px 24px;border-radius:6px;font-weight:600;font-size:14px;\">Open CAREX Portal</a>");
        sb.append("</div>");

        sb.append("</div>");

        // Footer
        sb.append("<div style=\"background:#f8fafc;padding:16px 24px;border-top:1px solid #e2e8f0;font-size:11px;color:#94a3b8;text-align:center;\">");
        sb.append("CAREX Platform • Automated Healthcare Operations & Patient Flow • Non-Diagnostic Notification Engine");
        sb.append("</div>");

        sb.append("</div></body></html>");
        return sb.toString();
    }
}
