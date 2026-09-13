package com.carex.controller;

import com.carex.dto.slot.SlotCreateRequest;
import com.carex.dto.slot.SlotGenerateRequest;
import com.carex.dto.slot.SlotResponse;
import com.carex.entity.Slot;
import com.carex.mapper.SlotMapper;
import com.carex.service.SlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/slots")
@Tag(name = "Slots", description = "Time slot scheduling and generation endpoints")
public class SlotController {

    private final SlotService slotService;
    private final SlotMapper slotMapper;

    public SlotController(SlotService slotService, SlotMapper slotMapper) {
        this.slotService = slotService;
        this.slotMapper = slotMapper;
    }

    @PostMapping
    @Operation(summary = "Create slot", description = "Creates a single appointment time slot manually")
    public ResponseEntity<SlotResponse> createSlot(@Valid @RequestBody SlotCreateRequest request) {
        Slot created = slotService.createSlot(
                request.getDoctorId(),
                request.getSlotDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getMode()
        );
        return new ResponseEntity<>(slotMapper.toResponse(created), HttpStatus.CREATED);
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate slots from availability", description = "Generates recurring appointment slots for a date range from an availability rule")
    public ResponseEntity<List<SlotResponse>> generateSlots(@Valid @RequestBody SlotGenerateRequest request) {
        List<Slot> generated = slotService.generateSlotsFromAvailability(
                request.getDoctorId(),
                request.getAvailabilityId(),
                request.getFromDate(),
                request.getToDate(),
                request.getSlotDurationMinutes()
        );
        return new ResponseEntity<>(slotMapper.toResponseList(generated), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get slot by ID", description = "Retrieves time slot details by ID")
    public ResponseEntity<SlotResponse> getSlotById(@PathVariable Long id) {
        Slot slot = slotService.getSlotById(id);
        return ResponseEntity.ok(slotMapper.toResponse(slot));
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get available slots for doctor", description = "Retrieves all future available slots for a specific doctor")
    public ResponseEntity<List<SlotResponse>> getAvailableSlotsForDoctor(@PathVariable Long doctorId) {
        List<Slot> slots = slotService.getAvailableSlotsForDoctor(doctorId);
        return ResponseEntity.ok(slotMapper.toResponseList(slots));
    }

    @GetMapping("/doctor/{doctorId}/date")
    @Operation(summary = "Get doctor slots for date", description = "Retrieves all slots for a doctor on a specific date")
    public ResponseEntity<List<SlotResponse>> getSlotsForDoctorAndDate(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Slot> slots = slotService.getSlotsForDoctorAndDate(doctorId, date);
        return ResponseEntity.ok(slotMapper.toResponseList(slots));
    }

    @GetMapping("/available")
    @Operation(summary = "Get available slots for doctor and date", description = "Retrieves available (unbooked) slots for a doctor on a specific date")
    public ResponseEntity<List<SlotResponse>> getAvailableSlotsForDoctorAndDate(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Slot> slots = slotService.getAvailableSlotsForDoctorAndDate(doctorId, date);
        return ResponseEntity.ok(slotMapper.toResponseList(slots));
    }

    @PutMapping("/{id}/block")
    @Operation(summary = "Block slot", description = "Marks a slot as BLOCKED (e.g. doctor emergency, maintenance)")
    public ResponseEntity<SlotResponse> blockSlot(@PathVariable Long id) {
        Slot updated = slotService.blockSlot(id);
        return ResponseEntity.ok(slotMapper.toResponse(updated));
    }

    @PutMapping("/{id}/release")
    @Operation(summary = "Release slot", description = "Releases a blocked slot back to AVAILABLE")
    public ResponseEntity<SlotResponse> releaseSlot(@PathVariable Long id) {
        Slot updated = slotService.releaseSlot(id);
        return ResponseEntity.ok(slotMapper.toResponse(updated));
    }
}
