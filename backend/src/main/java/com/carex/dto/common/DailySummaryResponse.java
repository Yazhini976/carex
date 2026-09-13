package com.carex.dto.common;

import java.time.LocalDate;

public class DailySummaryResponse {

    private LocalDate date;
    private long totalAppointments;
    private long booked;
    private long confirmed;
    private long completed;
    private long cancelled;
    private long noShow;

    public DailySummaryResponse() {}

    public DailySummaryResponse(LocalDate date, long totalAppointments, long booked,
                                long confirmed, long completed, long cancelled, long noShow) {
        this.date = date;
        this.totalAppointments = totalAppointments;
        this.booked = booked;
        this.confirmed = confirmed;
        this.completed = completed;
        this.cancelled = cancelled;
        this.noShow = noShow;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public long getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(long totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public long getBooked() {
        return booked;
    }

    public void setBooked(long booked) {
        this.booked = booked;
    }

    public long getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(long confirmed) {
        this.confirmed = confirmed;
    }

    public long getCompleted() {
        return completed;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }

    public long getCancelled() {
        return cancelled;
    }

    public void setCancelled(long cancelled) {
        this.cancelled = cancelled;
    }

    public long getNoShow() {
        return noShow;
    }

    public void setNoShow(long noShow) {
        this.noShow = noShow;
    }
}
