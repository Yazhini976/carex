package com.carex.service.impl;

import com.carex.entity.Appointment;
import com.carex.entity.AppointmentStatusHistory;
import com.carex.entity.User;
import com.carex.entity.enums.AppointmentStatus;
import com.carex.repository.AppointmentStatusHistoryRepository;
import com.carex.service.AppointmentStatusHistoryService;
import com.carex.service.AppointmentService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AppointmentStatusHistoryServiceImpl implements AppointmentStatusHistoryService {

    private final AppointmentStatusHistoryRepository historyRepository;
    private final AppointmentService appointmentService;

    public AppointmentStatusHistoryServiceImpl(AppointmentStatusHistoryRepository historyRepository,
                                                @Lazy AppointmentService appointmentService) {
        this.historyRepository = historyRepository;
        this.appointmentService = appointmentService;
    }

    @Override
    @Transactional
    public AppointmentStatusHistory recordStatusChange(Long appointmentId, AppointmentStatus oldStatus,
                                                        AppointmentStatus newStatus, Long changedByUserId) {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        User changedBy = null;
        if (changedByUserId != null) {
            changedBy = new User();
            changedBy.setId(changedByUserId);
        }
        AppointmentStatusHistory history =
                new AppointmentStatusHistory(appointment, oldStatus, newStatus, changedBy);
        return historyRepository.save(history);
    }

    @Override
    public List<AppointmentStatusHistory> getHistoryForAppointment(Long appointmentId) {
        return historyRepository.findByAppointmentIdOrderByChangedAtAsc(appointmentId);
    }
}
