package com.carex.service;

import com.carex.entity.Slot;
import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.SlotStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SlotService {
    Slot createSlot(Long doctorId, LocalDate slotDate, LocalTime startTime, LocalTime endTime, AppointmentMode mode);
    Slot createSlotFromAvailability(Long doctorId, Long availabilityId, LocalDate slotDate);
    List<Slot> generateSlotsFromAvailability(Long doctorId, Long availabilityId,
                                              LocalDate fromDate, LocalDate toDate, int slotDurationMinutes);
    Slot getSlotById(Long id);
    List<Slot> getSlotsForDoctorAndDate(Long doctorId, LocalDate slotDate);
    List<Slot> getAvailableSlotsForDoctor(Long doctorId);
    List<Slot> getAvailableSlotsForDoctorAndDate(Long doctorId, LocalDate slotDate);
    Slot blockSlot(Long id);
    Slot releaseSlot(Long id);
    Slot updateSlotStatus(Long id, SlotStatus status);
}
