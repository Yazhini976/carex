package com.carex.notification.dto;

public class UnreadCountResponse {

    private Long userId;
    private long unreadCount;

    public UnreadCountResponse() {}

    public UnreadCountResponse(Long userId, long unreadCount) {
        this.userId = userId;
        this.unreadCount = unreadCount;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }
}
