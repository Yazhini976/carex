package com.carex.service.impl;

import com.carex.entity.Doctor;
import com.carex.entity.DoctorAvailability;
import com.carex.entity.Slot;
import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.SlotStatus;
import com.carex.exception.BusinessRuleException;
import com.carex.exception.DuplicateResourceException;
import com.carex.exception.ResourceNotFoundException;
import com.carex.repository.SlotRepository;
import com.carex.service.DoctorAvailabilityService;
import com.carex.service.DoctorService;
import com.carex.service.SlotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SlotServiceImpl implements SlotService {

    private static final Logger log = LoggerFactory.getLogger(SlotServiceImpl.class);

    private final SlotRepository slotRepository;
    private final DoctorService doctorService;
    private final DoctorAvailabilityService availabilityService;

    public SlotServiceImpl(SlotRepository slotRepository,
                            DoctorService doctorService,
                            DoctorAvailabilityService availabilityService) {
        this.slotRepository = slotRepository;
        this.doctorService = doctorService;
        this.availabilityService = availabilityService;
    }

    @Override
    @Transactional
    public Slot createSlot(Long doctorId, LocalDate slotDate, LocalTime startTime,
                            LocalTime endTime, AppointmentMode mode) {
        validateSlotTimes(startTime, endTime);
        Doctor doctor = doctorService.getDoctorById(doctorId);
        requireActiveDoctorForSlot(doctor);
        if (slotRepository.existsByDoctorIdAndSlotDateAndStartTime(doctorId, slotDate, startTime)) {
            throw new DuplicateResourceException(
                    "Slot already exists for doctor " + doctorId + " on " + slotDate + " at " + startTime);
        }
        Slot slot = new Slot(doctor, slotDate, startTime, endTime, mode);
        Slot saved = slotRepository.save(slot);
        log.info("Created slot id={} doctor={} date={} start={}", saved.getId(), doctorId, slotDate, startTime);
        return saved;
    }

    @Override
    @Transactional
    public Slot createSlotFromAvailability(Long doctorId, Long availabilityId, LocalDate slotDate) {
        DoctorAvailability availability = availabilityService.getAvailabilityById(availabilityId);
        validateSlotTimes(availability.getStartTime(), availability.getEndTime());
        Doctor doctor = doctorService.getDoctorById(doctorId);
        requireActiveDoctorForSlot(doctor);
        if (slotRepository.existsByDoctorIdAndSlotDateAndStartTime(doctorId, slotDate, availability.getStartTime())) {
            throw new DuplicateResourceException(
                    "Slot already exists for doctor " + doctorId + " on " + slotDate + " at " + availability.getStartTime());
        }
        Slot slot = new Slot(doctor, slotDate, availability.getStartTime(), availability.getEndTime(), availability.getMode());
        slot.setAvailability(availability);
        Slot saved = slotRepository.save(slot);
        log.info("Created slot id={} from availability {} for date {}", saved.getId(), availabilityId, slotDate);
        return saved;
    }

    @Override
    @Transactional
    public List<Slot> generateSlotsFromAvailability(Long doctorId, Long availabilityId,
                                                      LocalDate fromDate, LocalDate toDate,
                                                      int slotDurationMinutes) {
        DoctorAvailability availability = availabilityService.getAvailabilityById(availabilityId);
        Doctor doctor = doctorService.getDoctorById(doctorId);
        requireActiveDoctorForSlot(doctor);
        if (slotDurationMinutes <= 0) {
            throw new BusinessRuleException("slotDurationMinutes must be positive");
        }

        List<Slot> created = new ArrayList<>();
        LocalDate current = fromDate;

        while (!current.isAfter(toDate)) {
            // DayOfWeek: Java DayOfWeek is 1=Monday..7=Sunday; schema stores 0=Sunday..6=Saturday
            int javaDow = current.getDayOfWeek().getValue(); // 1=Mon..7=Sun
            int schemaDow = javaDow % 7; // convert: Mon=1, Tue=2, ..., Sun=0

            if (schemaDow == availability.getDayOfWeek()) {
                LocalTime cursor = availability.getStartTime();
                while (cursor.plusMinutes(slotDurationMinutes).compareTo(availability.getEndTime()) <= 0) {
                    LocalTime slotEnd = cursor.plusMinutes(slotDurationMinutes);
                    if (!slotRepository.existsByDoctorIdAndSlotDateAndStartTime(doctorId, current, cursor)) {
                        Slot slot = new Slot(doctor, current, cursor, slotEnd, availability.getMode());
                        slot.setAvailability(availability);
                        created.add(slotRepository.save(slot));
                    }
                    cursor = slotEnd;
                }
            }
            current = current.plusDays(1);
        }
        log.info("Generated {} slots for doctor={} availabilityId={} {} to {}", created.size(), doctorId, availabilityId, fromDate, toDate);
        return created;
    }

    @Override
    public Slot getSlotById(Long id) {
        return slotRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Slot", id));
    }

    @Override
    public List<Slot> getSlotsForDoctorAndDate(Long doctorId, LocalDate slotDate) {
        return slotRepository.findByDoctorIdAndSlotDate(doctorId, slotDate);
    }

    @Override
    public List<Slot> getAvailableSlotsForDoctor(Long doctorId) {
        return slotRepository.findByDoctorIdAndStatus(doctorId, SlotStatus.AVAILABLE);
    }

    @Override
    public List<Slot> getAvailableSlotsForDoctorAndDate(Long doctorId, LocalDate slotDate) {
        return slotRepository.findByDoctorIdAndSlotDateAndStatus(doctorId, slotDate, SlotStatus.AVAILABLE);
    }

    @Override
    @Transactional
    public Slot blockSlot(Long id) {
        return updateSlotStatus(id, SlotStatus.BLOCKED);
    }

    @Override
    @Transactional
    public Slot releaseSlot(Long id) {
        Slot slot = getSlotById(id);
        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new BusinessRuleException("Cannot release a BOOKED slot directly. Cancel the appointment first.");
        }
        slot.setStatus(SlotStatus.AVAILABLE);
        return slotRepository.save(slot);
    }

    @Override
    @Transactional
    public Slot updateSlotStatus(Long id, SlotStatus status) {
        Slot slot = getSlotById(id);
        slot.setStatus(status);
        return slotRepository.save(slot);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void validateSlotTimes(LocalTime start, LocalTime end) {
        if (!start.isBefore(end)) {
            throw new BusinessRuleException("Slot startTime (" + start + ") must be before endTime (" + end + ")");
        }
    }

    private void requireActiveDoctorForSlot(Doctor doctor) {
        if (!Boolean.TRUE.equals(doctor.getIsActive())) {
            throw new BusinessRuleException(
                    "Doctor " + doctor.getId() + " is not active and cannot have new bookable slots");
        }
    }
}
