package com.carex.service;

import com.carex.entity.Doctor;
import com.carex.entity.DoctorAvailability;
import com.carex.entity.Slot;
import com.carex.entity.User;
import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.Role;
import com.carex.entity.enums.SlotStatus;
import com.carex.exception.BusinessRuleException;
import com.carex.repository.SlotRepository;
import com.carex.service.impl.SlotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private DoctorService doctorService;

    @Mock
    private DoctorAvailabilityService availabilityService;

    @InjectMocks
    private SlotServiceImpl slotService;

    private Doctor doctor;
    private Slot slot;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setName("Dr. John");
        user.setRole(Role.DOCTOR);
        doctor = new Doctor(user, "DOC-101");
        doctor.setId(10L);

        slot = new Slot(doctor, LocalDate.of(2026, 9, 25), LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentMode.ONLINE);
        slot.setId(100L);
        slot.setStatus(SlotStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Should create single slot successfully")
    void testCreateSlot_Success() {
        when(doctorService.getDoctorById(10L)).thenReturn(doctor);
        when(slotRepository.save(any(Slot.class))).thenReturn(slot);

        Slot created = slotService.createSlot(10L, LocalDate.of(2026, 9, 25), LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentMode.ONLINE);

        assertNotNull(created);
        assertEquals(SlotStatus.AVAILABLE, created.getStatus());
        verify(slotRepository, times(1)).save(any(Slot.class));
    }

    @Test
    @DisplayName("Should block available slot")
    void testBlockSlot_Success() {
        when(slotRepository.findById(100L)).thenReturn(Optional.of(slot));
        when(slotRepository.save(any(Slot.class))).thenAnswer(inv -> inv.getArgument(0));

        Slot blocked = slotService.blockSlot(100L);

        assertEquals(SlotStatus.BLOCKED, blocked.getStatus());
        verify(slotRepository, times(1)).save(slot);
    }

    @Test
    @DisplayName("Should release blocked slot back to available")
    void testReleaseSlot_Success() {
        slot.setStatus(SlotStatus.BLOCKED);
        when(slotRepository.findById(100L)).thenReturn(Optional.of(slot));
        when(slotRepository.save(any(Slot.class))).thenAnswer(inv -> inv.getArgument(0));

        Slot released = slotService.releaseSlot(100L);

        assertEquals(SlotStatus.AVAILABLE, released.getStatus());
        verify(slotRepository, times(1)).save(slot);
    }

    @Test
    @DisplayName("Should reject releasing BOOKED slot")
    void testReleaseSlot_RejectIfBooked() {
        slot.setStatus(SlotStatus.BOOKED);
        when(slotRepository.findById(100L)).thenReturn(Optional.of(slot));

        assertThrows(BusinessRuleException.class, () ->
                slotService.releaseSlot(100L)
        );
    }
}
