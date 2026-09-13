package com.carex.notification;

import com.carex.notification.impl.EmailNotificationServiceImpl;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailNotificationServiceTest {

    @Test
    @DisplayName("Should skip sending email when carex.email.enabled is false (Dev Mode)")
    void testEmailDisabledMode() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailNotificationService service = new EmailNotificationServiceImpl(mailSender, false, "noreply@carex.com", 3);

        NotificationEvent event = NotificationEvent.builder()
                .recipientEmail("patient@carex.com")
                .subject("Test Subject")
                .message("Test message")
                .build();

        service.send(event);

        assertFalse(service.isEmailEnabled());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should send email when carex.email.enabled is true")
    void testEmailEnabledMode() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        EmailNotificationService service = new EmailNotificationServiceImpl(mailSender, true, "noreply@carex.com", 3);

        NotificationEvent event = NotificationEvent.builder()
                .recipientEmail("patient@carex.com")
                .subject("CAREX Appointment")
                .message("Your appointment is confirmed.")
                .htmlBody("<p>Your appointment is confirmed.</p>")
                .build();

        service.send(event);

        assertTrue(service.isEmailEnabled());
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("Should handle missing or null recipient gracefully without throwing exception")
    void testGracefulHandlingNullRecipient() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailNotificationService service = new EmailNotificationServiceImpl(mailSender, true, "noreply@carex.com", 3);

        NotificationEvent event = NotificationEvent.builder()
                .subject("CAREX Appointment")
                .message("Test")
                .build();

        assertDoesNotThrow(() -> service.send(event));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
