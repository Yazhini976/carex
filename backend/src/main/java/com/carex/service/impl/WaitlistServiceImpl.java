package com.carex.service.impl;

import com.carex.entity.Doctor;
import com.carex.entity.Patient;
import com.carex.entity.Specialty;
import com.carex.entity.Waitlist;
import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.WaitlistStatus;
import com.carex.exception.ResourceNotFoundException;
import com.carex.repository.WaitlistRepository;
import com.carex.service.DoctorService;
import com.carex.service.PatientService;
import com.carex.service.SpecialtyService;
import com.carex.service.WaitlistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class WaitlistServiceImpl implements WaitlistService {

    private static final Logger log = LoggerFactory.getLogger(WaitlistServiceImpl.class);

    private final WaitlistRepository waitlistRepository;
    private final PatientService patientService;
    private final SpecialtyService specialtyService;
    private final DoctorService doctorService;

    public WaitlistServiceImpl(WaitlistRepository waitlistRepository,
                                PatientService patientService,
                                SpecialtyService specialtyService,
                                DoctorService doctorService) {
        this.waitlistRepository = waitlistRepository;
        this.patientService = patientService;
        this.specialtyService = specialtyService;
        this.doctorService = doctorService;
    }

    @Override
    @Transactional
    public Waitlist joinWaitlist(Long patientId, Long specialtyId, Long preferredDoctorId,
                                  LocalDate preferredDate, AppointmentMode preferredMode) {
        Patient patient = patientService.getPatientById(patientId);
        Specialty specialty = specialtyService.getSpecialtyById(specialtyId);

        Waitlist entry = new Waitlist(patient, specialty);
        entry.setPreferredDate(preferredDate);
        entry.setPreferredMode(preferredMode);

        if (preferredDoctorId != null) {
            Doctor preferredDoctor = doctorService.getDoctorById(preferredDoctorId);
            entry.setPreferredDoctor(preferredDoctor);
        }

        Waitlist saved = waitlistRepository.save(entry);
        log.info("Patient {} joined waitlist for specialty {} entry={}", patientId, specialtyId, saved.getId());
        return saved;
    }

    @Override
    public Waitlist getWaitlistEntryById(Long id) {
        return waitlistRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Waitlist", id));
    }

    @Override
    public List<Waitlist> getPatientWaitlist(Long patientId) {
        return waitlistRepository.findByPatientId(patientId);
    }

    @Override
    public List<Waitlist> findMatchingWaiting(Long specialtyId, Long doctorId,
                                               AppointmentMode mode, LocalDate date) {
        return waitlistRepository.findMatchingWaiting(specialtyId, doctorId, mode, date);
    }

    @Override
    @Transactional
    public Waitlist cancelEntry(Long id) {
        return transitionStatus(id, WaitlistStatus.CANCELLED,
                Set.of(WaitlistStatus.WAITING, WaitlistStatus.OFFERED));
    }

    @Override
    @Transactional
    public Waitlist markOffered(Long id) {
        return transitionStatus(id, WaitlistStatus.OFFERED, Set.of(WaitlistStatus.WAITING));
    }

    @Override
    @Transactional
    public Waitlist markFulfilled(Long id) {
        Waitlist entry = transitionStatus(id, WaitlistStatus.FULFILLED,
                Set.of(WaitlistStatus.OFFERED, WaitlistStatus.WAITING));
        entry.setFulfilledAt(LocalDateTime.now());
        return waitlistRepository.save(entry);
    }

    @Override
    @Transactional
    public Waitlist expireEntry(Long id) {
        return transitionStatus(id, WaitlistStatus.EXPIRED,
                Set.of(WaitlistStatus.WAITING, WaitlistStatus.OFFERED));
    }

    /**
     * Smart waitlist: when a slot becomes available, find and notify top-ranked matching patients.
     *
     * <p>Ranking: first-come-first-served (ordered by createdAt in the repository query).
     * The first match is offered the slot.</p>
     */
    @Override
    @Transactional
    public void triggerSmartWaitlist(Long specialtyId, Long doctorId, AppointmentMode mode, LocalDate date) {
        List<Waitlist> candidates = findMatchingWaiting(specialtyId, doctorId, mode, date);
        if (candidates.isEmpty()) {
            log.info("Smart waitlist: no candidates for specialty={} doctor={} mode={} date={}", specialtyId, doctorId, mode, date);
            return;
        }
        // Offer to top candidate (first in queue)
        Waitlist top = candidates.get(0);
        top.setStatus(WaitlistStatus.OFFERED);
        waitlistRepository.save(top);
        log.info("Smart waitlist: offered slot to waitlist entry {} patient={}", top.getId(), top.getPatient().getId());
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Waitlist transitionStatus(Long id, WaitlistStatus target, Set<WaitlistStatus> allowedFrom) {
        Waitlist entry = getWaitlistEntryById(id);
        if (!allowedFrom.contains(entry.getStatus())) {
            throw new com.carex.exception.BusinessRuleException(
                    "Waitlist entry " + id + " cannot transition from " + entry.getStatus() + " to " + target);
        }
        entry.setStatus(target);
        return waitlistRepository.save(entry);
    }
}
