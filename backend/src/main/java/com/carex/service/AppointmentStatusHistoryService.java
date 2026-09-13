package com.carex.service;

import com.carex.entity.AppointmentStatusHistory;
import com.carex.entity.enums.AppointmentStatus;

import java.util.List;

public interface AppointmentStatusHistoryService {
    AppointmentStatusHistory recordStatusChange(Long appointmentId, AppointmentStatus oldStatus,
                                                AppointmentStatus newStatus, Long changedByUserId);
    List<AppointmentStatusHistory> getHistoryForAppointment(Long appointmentId);
}
