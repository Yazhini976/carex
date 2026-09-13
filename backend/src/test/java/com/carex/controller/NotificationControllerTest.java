package com.carex.controller;

import com.carex.entity.Notification;
import com.carex.entity.User;
import com.carex.entity.enums.NotificationStatus;
import com.carex.entity.enums.Role;
import com.carex.mapper.NotificationMapper;
import com.carex.notification.dto.NotificationStatsResponse;
import com.carex.security.CustomUserPrincipal;
import com.carex.security.JwtService;
import com.carex.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationMapper notificationMapper;

    @MockBean
    private JwtService jwtService;

    private User patientUser;
    private CustomUserPrincipal patientPrincipal;

    @BeforeEach
    void setUp() {
        patientUser = new User();
        patientUser.setId(10L);
        patientUser.setEmail("alice@carex.com");
        patientUser.setName("Alice");
        patientUser.setRole(Role.PATIENT);
        patientUser.setIsActive(true);

        patientPrincipal = new CustomUserPrincipal(patientUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(patientPrincipal, null, patientPrincipal.getAuthorities())
        );
    }

    @Test
    @DisplayName("GET /api/notifications/user/{userId} - should return user notifications")
    void testGetUserNotifications() throws Exception {
        Notification notification = new Notification(patientUser, "APPOINTMENT_BOOKED", "Your appointment is booked");
        notification.setId(101L);

        when(notificationService.getNotificationsForUser(10L)).thenReturn(List.of(notification));

        mockMvc.perform(get("/api/notifications/user/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).getNotificationsForUser(10L);
    }

    @Test
    @DisplayName("GET /api/notifications/user/{userId}/unread-count - should return unread count")
    void testGetUnreadCount() throws Exception {
        when(notificationService.getUnreadCount(10L)).thenReturn(3L);

        mockMvc.perform(get("/api/notifications/user/10/unread-count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.unreadCount").value(3));
    }

    @Test
    @DisplayName("PUT /api/notifications/{id}/read - should mark notification as read")
    void testMarkRead() throws Exception {
        Notification notification = new Notification(patientUser, "APPOINTMENT_REMINDER", "Reminder");
        notification.setId(101L);
        notification.setStatus(NotificationStatus.SENT);

        when(notificationService.markRead(101L)).thenReturn(notification);

        mockMvc.perform(put("/api/notifications/101/read")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).markRead(101L);
    }

    @Test
    @DisplayName("PUT /api/notifications/user/{userId}/read-all - should bulk mark as read")
    void testMarkAllRead() throws Exception {
        mockMvc.perform(put("/api/notifications/user/10/read-all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).markAllRead(10L);
    }

    @Test
    @DisplayName("GET /api/notifications/stats - should return system health stats for ADMIN")
    void testGetStats() throws Exception {
        User adminUser = new User();
        adminUser.setId(99L);
        adminUser.setEmail("admin@carex.com");
        adminUser.setName("Admin");
        adminUser.setRole(Role.ADMIN);
        adminUser.setIsActive(true);

        CustomUserPrincipal adminPrincipal = new CustomUserPrincipal(adminUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminPrincipal, null, adminPrincipal.getAuthorities())
        );

        NotificationStatsResponse stats = new NotificationStatsResponse(100, 95, 3, 2, 95.0);
        when(notificationService.getStats()).thenReturn(stats);

        mockMvc.perform(get("/api/notifications/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(100))
                .andExpect(jsonPath("$.sent").value(95))
                .andExpect(jsonPath("$.failed").value(2))
                .andExpect(jsonPath("$.deliveryRate").value(95.0));
    }
}
