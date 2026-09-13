package com.carex.notification.dto;

public class NotificationStatsResponse {

    private long total;
    private long sent;
    private long pending;
    private long failed;
    private double deliveryRate;

    public NotificationStatsResponse() {}

    public NotificationStatsResponse(long total, long sent, long pending, long failed, double deliveryRate) {
        this.total = total;
        this.sent = sent;
        this.pending = pending;
        this.failed = failed;
        this.deliveryRate = deliveryRate;
    }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getSent() { return sent; }
    public void setSent(long sent) { this.sent = sent; }

    public long getPending() { return pending; }
    public void setPending(long pending) { this.pending = pending; }

    public long getFailed() { return failed; }
    public void setFailed(long failed) { this.failed = failed; }

    public double getDeliveryRate() { return deliveryRate; }
    public void setDeliveryRate(double deliveryRate) { this.deliveryRate = deliveryRate; }
}
