package com.carex.mapper;

import com.carex.dto.slot.SlotResponse;
import com.carex.entity.Slot;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SlotMapper {

    public SlotResponse toResponse(Slot slot) {
        if (slot == null) {
            return null;
        }

        Long doctorId = slot.getDoctor() != null ? slot.getDoctor().getId() : null;
        String doctorName = (slot.getDoctor() != null && slot.getDoctor().getUser() != null)
                ? slot.getDoctor().getUser().getName()
                : null;

        return new SlotResponse(
                slot.getId(),
                doctorId,
                doctorName,
                slot.getSlotDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getMode(),
                slot.getStatus()
        );
    }

    public List<SlotResponse> toResponseList(List<Slot> slots) {
        if (slots == null) {
            return Collections.emptyList();
        }
        return slots.stream().map(this::toResponse).toList();
    }
}
