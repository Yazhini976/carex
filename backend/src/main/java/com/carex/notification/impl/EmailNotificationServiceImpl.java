package com.carex.notification.impl;

import com.carex.notification.EmailNotificationService;
import com.carex.notification.NotificationEvent;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationServiceImpl.class);

    private final JavaMailSender mailSender;
    private final boolean emailEnabled;
    private final String mailFrom;
    private final int maxRetries;

    @Autowired
    public EmailNotificationServiceImpl(
            @Autowired(required = false) JavaMailSender mailSender,
            @Value("${carex.email.enabled:false}") boolean emailEnabled,
            @Value("${carex.email.from:noreply@carex.com}") String mailFrom,
            @Value("${carex.email.max-retries:3}") int maxRetries) {
        this.mailSender = mailSender;
        this.emailEnabled = emailEnabled;
        this.mailFrom = mailFrom;
        this.maxRetries = maxRetries;
    }

    @Override
    @Async
    public void send(NotificationEvent event) {
        if (event == null || event.getRecipientEmail() == null || event.getRecipientEmail().isBlank()) {
            log.warn("Cannot send email: missing recipient email in event={}", event);
            return;
        }

        if (!emailEnabled) {
            log.info("[EMAIL-DEV-MODE] Email delivery disabled (carex.email.enabled=false). Simulated sending to={} subject='{}' type={}",
                    maskEmail(event.getRecipientEmail()), event.getSubject(), event.getNotificationType());
            return;
        }

        if (mailSender == null) {
            log.warn("[EMAIL-SKIPPED] JavaMailSender is not configured. Skipping email to={}", maskEmail(event.getRecipientEmail()));
            return;
        }

        int attempts = 0;
        boolean sent = false;

        while (attempts < maxRetries && !sent) {
            attempts++;
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(
                        message,
                        MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                        StandardCharsets.UTF_8.name()
                );

                helper.setFrom(mailFrom);
                helper.setTo(event.getRecipientEmail());
                helper.setSubject(event.getSubject());

                if (event.getHtmlBody() != null && !event.getHtmlBody().isBlank()) {
                    helper.setText(event.getMessage(), event.getHtmlBody());
                } else {
                    helper.setText(event.getMessage(), false);
                }

                mailSender.send(message);
                sent = true;
                log.info("[EMAIL-SENT] Successfully delivered email to={} subject='{}' type={} attempt={}/{}",
                        maskEmail(event.getRecipientEmail()), event.getSubject(), event.getNotificationType(), attempts, maxRetries);
            } catch (Exception ex) {
                log.warn("[EMAIL-RETRY] Failed attempt {}/{} to send email to={} error={}",
                        attempts, maxRetries, maskEmail(event.getRecipientEmail()), ex.getMessage());
                if (attempts < maxRetries) {
                    try {
                        Thread.sleep(500L * attempts); // Linear backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        if (!sent) {
            log.error("[EMAIL-FAILED] Exhausted {} retry attempts sending email to={} for appointmentId={}",
                    maxRetries, maskEmail(event.getRecipientEmail()), event.getAppointmentId());
        }
    }

    @Override
    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) return "***" + email.substring(atIndex);
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }
}
