package com.carex.dto.appointment;

public class AppointmentStatusUpdateRequest {

    private Long changedByUserId;

    public AppointmentStatusUpdateRequest() {}

    public AppointmentStatusUpdateRequest(Long changedByUserId) {
        this.changedByUserId = changedByUserId;
    }

    public Long getChangedByUserId() {
        return changedByUserId;
    }

    public void setChangedByUserId(Long changedByUserId) {
        this.changedByUserId = changedByUserId;
    }
}
