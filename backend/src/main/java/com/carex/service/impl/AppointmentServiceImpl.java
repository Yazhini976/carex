package com.carex.service.impl;

import com.carex.entity.*;
import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.AppointmentStatus;
import com.carex.entity.enums.SlotStatus;
import com.carex.exception.*;
import com.carex.repository.AppointmentRepository;
import com.carex.repository.AppointmentStatusHistoryRepository;
import com.carex.repository.SlotRepository;
import com.carex.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Core appointment service implementing the full booking workflow.
 *
 * <p>Key business rules enforced here:</p>
 * <ul>
 *   <li>HCL Online/Offline Rule: a patient's online and offline appointments must use different doctors.</li>
 *   <li>Slot concurrency: pessimistic write lock prevents double-booking.</li>
 *   <li>Status transitions: only valid state changes are allowed.</li>
 *   <li>Atomic booking: appointment + slot update + status history are a single transaction.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private final AppointmentRepository appointmentRepository;
    private final AppointmentStatusHistoryRepository historyRepository;
    private final SlotRepository slotRepository;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final SpecialtyService specialtyService;
    private final DoctorSpecialtyService doctorSpecialtyService;
    private final com.carex.notification.NotificationDispatcher notificationDispatcher;
    private final com.carex.notification.NotificationTemplateService notificationTemplateService;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                   AppointmentStatusHistoryRepository historyRepository,
                                   SlotRepository slotRepository,
                                   PatientService patientService,
                                   DoctorService doctorService,
                                   SpecialtyService specialtyService,
                                   DoctorSpecialtyService doctorSpecialtyService,
                                   @org.springframework.context.annotation.Lazy com.carex.notification.NotificationDispatcher notificationDispatcher,
                                   @org.springframework.context.annotation.Lazy com.carex.notification.NotificationTemplateService notificationTemplateService) {
        this.appointmentRepository = appointmentRepository;
        this.historyRepository = historyRepository;
        this.slotRepository = slotRepository;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.specialtyService = specialtyService;
        this.doctorSpecialtyService = doctorSpecialtyService;
        this.notificationDispatcher = notificationDispatcher;
        this.notificationTemplateService = notificationTemplateService;
    }

    // =========================================================================
    // BOOKING WORKFLOW
    // =========================================================================

    /**
     * Books an appointment following the validated booking workflow.
     *
     * <p>Transaction guarantee: Appointment creation, slot status update, and status history record
     * are all committed atomically or all rolled back.</p>
     */
    @Override
    @Transactional
    public Appointment bookAppointment(Long patientId, Long doctorId, Long specialtyId,
                                        Long slotId, AppointmentMode mode, String notes) {
        // Step 1: Validate patient
        Patient patient = patientService.getPatientById(patientId);

        // Step 2: Validate doctor
        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (!Boolean.TRUE.equals(doctor.getIsActive())) {
            throw new BusinessRuleException("Doctor " + doctorId + " is not active");
        }

        // Step 3: Validate specialty
        Specialty specialty = specialtyService.getSpecialtyById(specialtyId);
        if (!Boolean.TRUE.equals(specialty.getIsActive())) {
            throw new BusinessRuleException("Specialty " + specialtyId + " is not active");
        }

        // Step 4 & 5: Validate slot with pessimistic lock (prevents race conditions)
        Slot slot = slotRepository.findByIdWithLock(slotId)
                .orElseThrow(() -> ResourceNotFoundException.of("Slot", slotId));

        // Step 6: Validate slot availability
        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw SlotUnavailableException.forSlot(slotId);
        }

        // Step 7: Validate slot belongs to the requested doctor
        if (!slot.getDoctor().getId().equals(doctorId)) {
            throw new BusinessRuleException(
                    "Slot " + slotId + " does not belong to doctor " + doctorId);
        }

        // Step 8: Validate appointment mode matches slot mode
        if (slot.getMode() != mode) {
            throw new BusinessRuleException(
                    "Slot mode " + slot.getMode() + " does not match requested appointment mode " + mode);
        }

        // Step 9: Validate Doctor-Specialty relationship
        if (!doctorSpecialtyService.isAssigned(doctorId, specialtyId)) {
            throw new BusinessRuleException(
                    "Doctor " + doctorId + " is not assigned to specialty " + specialtyId);
        }

        // Step 10: HCL Business Rule — Online and Offline appointments must use different doctors
        enforceOnlineOfflineDoctorRule(patientId, doctorId, mode);

        // Step 11: Create the appointment
        Appointment appointment = new Appointment(patient, doctor, specialty, slot, mode);
        appointment.setNotes(notes);
        Appointment saved = appointmentRepository.save(appointment);

        // Step 12: Mark slot as BOOKED
        slot.setStatus(SlotStatus.BOOKED);
        slotRepository.save(slot);

        // Step 13: Create initial status history record
        AppointmentStatusHistory history = new AppointmentStatusHistory(
                saved, null, AppointmentStatus.BOOKED, null);
        historyRepository.save(history);

        log.info("Booked appointment id={} patient={} doctor={} slot={} mode={}",
                saved.getId(), patientId, doctorId, slotId, mode);

        // Step 14: Non-blocking notification dispatch
        safeDispatchEvent(() -> notificationTemplateService.buildAppointmentBookedEvent(saved));

        return saved;
    }

    // =========================================================================
    // STATUS TRANSITIONS
    // =========================================================================

    @Override
    @Transactional
    public Appointment confirmAppointment(Long appointmentId, Long changedByUserId) {
        Appointment appointment = getAppointmentById(appointmentId);
        validateTransition(appointment.getStatus(), AppointmentStatus.CONFIRMED,
                Set.of(AppointmentStatus.BOOKED));
        Appointment updated = applyStatusChange(appointment, AppointmentStatus.CONFIRMED, changedByUserId);
        safeDispatchEvent(() -> notificationTemplateService.buildAppointmentConfirmedEvent(updated));
        return updated;
    }

    @Override
    @Transactional
    public Appointment completeAppointment(Long appointmentId, Long changedByUserId) {
        Appointment appointment = getAppointmentById(appointmentId);
        validateTransition(appointment.getStatus(), AppointmentStatus.COMPLETED,
                Set.of(AppointmentStatus.BOOKED, AppointmentStatus.CONFIRMED));
        appointment.setCompletedAt(LocalDateTime.now());
        // Historical slot: do NOT release; it was consumed. Leave as BOOKED.
        Appointment updated = applyStatusChange(appointment, AppointmentStatus.COMPLETED, changedByUserId);
        safeDispatchEvent(() -> notificationTemplateService.buildAppointmentCompletedEvent(updated));
        return updated;
    }

    @Override
    @Transactional
    public Appointment cancelAppointment(Long appointmentId, Long changedByUserId) {
        Appointment appointment = getAppointmentById(appointmentId);
        validateTransition(appointment.getStatus(), AppointmentStatus.CANCELLED,
                Set.of(AppointmentStatus.BOOKED, AppointmentStatus.CONFIRMED));
        appointment.setCancelledAt(LocalDateTime.now());

        // Release the slot back to AVAILABLE so other patients can book it
        Slot slot = appointment.getSlot();
        slot.setStatus(SlotStatus.AVAILABLE);
        slotRepository.save(slot);

        Appointment updated = applyStatusChange(appointment, AppointmentStatus.CANCELLED, changedByUserId);
        safeDispatchEvent(() -> notificationTemplateService.buildAppointmentCancelledEvent(updated));
        return updated;
    }

    @Override
    @Transactional
    public Appointment markNoShow(Long appointmentId, Long changedByUserId) {
        Appointment appointment = getAppointmentById(appointmentId);
        validateTransition(appointment.getStatus(), AppointmentStatus.NO_SHOW,
                Set.of(AppointmentStatus.BOOKED, AppointmentStatus.CONFIRMED));
        // Slot remains BOOKED — slot was allocated and the time was consumed
        Appointment updated = applyStatusChange(appointment, AppointmentStatus.NO_SHOW, changedByUserId);
        safeDispatchEvent(() -> notificationTemplateService.buildAppointmentNoShowEvent(updated));
        return updated;
    }

    private void safeDispatchEvent(java.util.function.Supplier<com.carex.notification.NotificationEvent> eventSupplier) {
        try {
            if (notificationDispatcher != null && notificationTemplateService != null) {
                com.carex.notification.NotificationEvent event = eventSupplier.get();
                if (event != null) {
                    notificationDispatcher.dispatch(event);
                }
            }
        } catch (Exception ex) {
            log.error("[NOTIFICATION-DISPATCH-SAFE-FAIL] Notification dispatch encountered an error, appointment workflow preserved: {}", ex.getMessage());
        }
    }

    // =========================================================================
    // QUERIES
    // =========================================================================

    @Override
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Appointment", id));
    }

    @Override
    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    @Override
    public List<Appointment> getAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    @Override
    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status);
    }

    @Override
    public List<Appointment> getAppointmentsByDoctorAndDate(Long doctorId, LocalDate date) {
        return appointmentRepository.findByDoctorIdAndSlotDate(doctorId, date);
    }

    // =========================================================================
    // DAILY SUMMARY
    // =========================================================================

    @Override
    public Map<String, Object> getDailySummary(LocalDate date) {
        List<Appointment> appointments = appointmentRepository.findBySlotDate(date);

        long total = appointments.size();
        long online = appointments.stream()
                .filter(a -> a.getMode() == AppointmentMode.ONLINE).count();
        long offline = appointments.stream()
                .filter(a -> a.getMode() == AppointmentMode.OFFLINE).count();
        long completed = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
        long cancelled = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count();
        long noShow = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.NO_SHOW).count();
        long booked = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.BOOKED).count();
        long confirmed = appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED).count();

        // Group by specialty
        Map<String, Long> bySpecialty = new LinkedHashMap<>();
        appointments.forEach(a -> bySpecialty.merge(a.getSpecialty().getName(), 1L, Long::sum));

        // Group by doctor
        Map<String, Long> byDoctor = new LinkedHashMap<>();
        appointments.forEach(a -> {
            String name = a.getDoctor().getUser().getName();
            byDoctor.merge(name, 1L, Long::sum);
        });

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("date", date.toString());
        summary.put("total", total);
        summary.put("online", online);
        summary.put("offline", offline);
        summary.put("completed", completed);
        summary.put("cancelled", cancelled);
        summary.put("noShow", noShow);
        summary.put("booked", booked);
        summary.put("confirmed", confirmed);
        summary.put("bySpecialty", bySpecialty);
        summary.put("byDoctor", byDoctor);
        return summary;
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * HCL Business Rule enforcement.
     *
     * <p>For a given patient booking a given mode:<br>
     * - If booking ONLINE: check that the same doctor does not already have an active OFFLINE appointment for this patient.<br>
     * - If booking OFFLINE: check that the same doctor does not already have an active ONLINE appointment for this patient.<br>
     *
     * <p>The rule requires the doctors to be <em>different</em>, meaning: the doctor
     * used for ONLINE must not also be used for OFFLINE (and vice versa) for the same patient.</p>
     */
    private void enforceOnlineOfflineDoctorRule(Long patientId, Long doctorId, AppointmentMode requestedMode) {
        AppointmentMode oppositeMode = (requestedMode == AppointmentMode.ONLINE)
                ? AppointmentMode.OFFLINE
                : AppointmentMode.ONLINE;

        List<Appointment> oppositeActive = appointmentRepository.findActiveByPatientIdAndMode(patientId, oppositeMode);

        boolean sameDoctor = oppositeActive.stream()
                .anyMatch(a -> a.getDoctor().getId().equals(doctorId));

        if (sameDoctor) {
            throw new BusinessRuleException(
                    "Online and offline appointments require different doctors. " +
                    "Doctor " + doctorId + " already has an active " + oppositeMode + " appointment with this patient.");
        }
    }

    /**
     * Validates that a status transition from {@code current} to {@code target} is permitted.
     */
    private void validateTransition(AppointmentStatus current, AppointmentStatus target,
                                     Set<AppointmentStatus> allowedFrom) {
        if (!allowedFrom.contains(current)) {
            throw InvalidAppointmentStateException.forTransition(current, target);
        }
    }

    /**
     * Applies a status change to an appointment and records the audit trail.
     * The changedBy user is resolved lazily; null means system-initiated.
     */
    private Appointment applyStatusChange(Appointment appointment, AppointmentStatus newStatus,
                                           Long changedByUserId) {
        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(newStatus);
        Appointment saved = appointmentRepository.save(appointment);

        // Audit record — changedBy is nullable (system-initiated change if null)
        User changedBy = null;
        if (changedByUserId != null) {
            // We use a lazy-safe approach: only set if ID provided. Full resolution handled by UserService layer.
            changedBy = new User();
            changedBy.setId(changedByUserId);
        }

        AppointmentStatusHistory history = new AppointmentStatusHistory(saved, oldStatus, newStatus, changedBy);
        historyRepository.save(history);

        log.info("Appointment id={} status changed {} → {}", saved.getId(), oldStatus, newStatus);
        return saved;
    }
}
