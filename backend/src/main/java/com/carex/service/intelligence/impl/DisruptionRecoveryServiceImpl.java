package com.carex.service.intelligence.impl;

import com.carex.entity.Appointment;
import com.carex.entity.Doctor;
import com.carex.entity.Slot;
import com.carex.entity.Specialty;
import com.carex.repository.AppointmentRepository;
import com.carex.service.DoctorService;
import com.carex.service.DoctorSpecialtyService;
import com.carex.service.SlotService;
import com.carex.service.intelligence.DisruptionRecoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Disruption recovery implementation.
 *
 * <p><strong>READ-ONLY: Proposes patient recovery alternatives for administrative review.
 * Never modifies database records or appointments automatically.</strong></p>
 */
@Service
@Transactional(readOnly = true)
public class DisruptionRecoveryServiceImpl implements DisruptionRecoveryService {

    private static final Logger log = LoggerFactory.getLogger(DisruptionRecoveryServiceImpl.class);

    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;
    private final DoctorSpecialtyService doctorSpecialtyService;
    private final SlotService slotService;

    public DisruptionRecoveryServiceImpl(AppointmentRepository appointmentRepository,
                                         DoctorService doctorService,
                                         DoctorSpecialtyService doctorSpecialtyService,
                                         SlotService slotService) {
        this.appointmentRepository = appointmentRepository;
        this.doctorService = doctorService;
        this.doctorSpecialtyService = doctorSpecialtyService;
        this.slotService = slotService;
    }

    @Override
    public DisruptionRecoveryResult evaluateRecovery(Long doctorId, LocalDate date) {
        log.info("[RECOVERY-ANALYSIS][READ-ONLY] doctorId={} date={}", doctorId, date);

        Doctor unavailableDoctor = doctorService.getDoctorById(doctorId);
        String docName = unavailableDoctor.getUser() != null
                ? unavailableDoctor.getUser().getName()
                : "Dr. #" + doctorId;

        // 1. Fetch active appointments affected
        List<Appointment> affectedAppointments = appointmentRepository.findActiveByDoctorIdAndDate(doctorId, date);

        // 2. Find specialties for unavailable doctor
        List<Specialty> specialties = doctorSpecialtyService.getSpecialtiesForDoctor(doctorId);
        List<Long> specialtyIds = specialties.stream().map(Specialty::getId).toList();

        // 3. Find alternative active doctors with matching specialties
        List<Doctor> candidateDoctors = doctorService.getActiveDoctors().stream()
                .filter(d -> !d.getId().equals(doctorId))
                .filter(d -> specialtyIds.stream().anyMatch(sId -> doctorSpecialtyService.isAssigned(d.getId(), sId)))
                .toList();

        // 4. Determine alternative capacity
        List<AlternativeCapacity> alternativeCapacities = new ArrayList<>();
        for (Doctor alt : candidateDoctors) {
            List<Slot> availableSlots = slotService.getAvailableSlotsForDoctorAndDate(alt.getId(), date);
            String primarySpec = specialties.isEmpty() ? "General" : specialties.get(0).getName();
            alternativeCapacities.add(new AlternativeCapacity(alt, primarySpec, availableSlots.size()));
        }

        // 5. Build patient-level recovery suggestions
        List<PatientRecoveryPlan> patientPlans = new ArrayList<>();
        int altIndex = 0;

        for (Appointment appt : affectedAppointments) {
            Doctor suggestedDoctor = null;
            LocalTime suggestedTime = null;
            String rationale;

            if (!candidateDoctors.isEmpty()) {
                // Round-robin assignment across compatible alternative doctors
                suggestedDoctor = candidateDoctors.get(altIndex % candidateDoctors.size());
                altIndex++;

                // Check for available slot on alternative doctor
                List<Slot> altSlots = slotService.getAvailableSlotsForDoctorAndDate(suggestedDoctor.getId(), date);
                if (!altSlots.isEmpty()) {
                    suggestedTime = altSlots.get(0).getStartTime();
                    rationale = "Compatible specialty and mode. Open slot at " + suggestedTime + ".";
                } else {
                    suggestedTime = appt.getSlot() != null ? appt.getSlot().getStartTime() : LocalTime.of(10, 0);
                    rationale = "Compatible specialty & mode. Parallel clinic session proposed at " + suggestedTime + ".";
                }
            } else {
                rationale = "No available alternative doctor in same specialty on " + date + ". Rescheduling to next business day recommended.";
            }

            String specName = appt.getSpecialty() != null ? appt.getSpecialty().getName() : "General";
            String modeStr = appt.getMode() != null ? appt.getMode().name() : "ONLINE";

            patientPlans.add(new PatientRecoveryPlan(
                    appt, suggestedDoctor, suggestedTime, specName, modeStr, rationale
            ));
        }

        // 6. Summary recommendation
        String summary;
        if (affectedAppointments.isEmpty()) {
            summary = "No active appointments scheduled for " + docName + " on " + date + ". No recovery action needed.";
        } else if (candidateDoctors.isEmpty()) {
            summary = "CRITICAL: " + affectedAppointments.size() + " appointment(s) affected with zero matching alternative doctors on " + date + ". Patient notification and date rescheduling required.";
        } else {
            summary = "CAREX Recovery Plan: Redistribute " + affectedAppointments.size() + " patient(s) across " + candidateDoctors.size() + " active alternative specialist(s). Ready for administrative approval.";
        }

        return new DisruptionRecoveryResult(
                doctorId, docName, date, affectedAppointments.size(),
                patientPlans, alternativeCapacities, summary, true
        );
    }
}
