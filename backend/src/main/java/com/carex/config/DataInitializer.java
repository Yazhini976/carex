package com.carex.config;

import com.carex.entity.*;
import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.Role;
import com.carex.entity.enums.SlotStatus;
import com.carex.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Deterministic Database Seeder for Hackathon Demonstrations & Local Testing.
 * Automatically provisions demo accounts, clinical specialties, provider profiles,
 * weekly availability templates, and active bookable slots for the upcoming 7 days.
 *
 * <p>Activated via configuration property: {@code carex.seed-data.enabled=true}</p>
 */
@Component
@ConditionalOnProperty(name = "carex.seed-data.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorSpecialtyRepository doctorSpecialtyRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final SlotRepository slotRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            SpecialtyRepository specialtyRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            DoctorSpecialtyRepository doctorSpecialtyRepository,
            DoctorAvailabilityRepository availabilityRepository,
            SlotRepository slotRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.specialtyRepository = specialtyRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorSpecialtyRepository = doctorSpecialtyRepository;
        this.availabilityRepository = availabilityRepository;
        this.slotRepository = slotRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.findByEmail("admin@carex.com").isPresent()) {
            log.info("CAREX Demo Data already provisioned. Ensuring OFFLINE slots exist for all doctors...");
            // Top-up: make sure all doctors have OFFLINE slots for upcoming days
            ensureOfflineSlotsForAllDoctors();
            return;
        }

        log.info("Provisioning CAREX Deterministic Hackathon Demo Dataset...");

        String defaultPass = passwordEncoder.encode("Password123!");

        // 1. Specialties
        Specialty genMed = getOrCreateSpecialty("General Medicine", "Primary healthcare, routine consultations, preventative checkups");
        Specialty cardio = getOrCreateSpecialty("Cardiology", "Heart, blood vessels, hypertension, chest discomfort evaluations");
        Specialty derma = getOrCreateSpecialty("Dermatology", "Skin conditions, rashes, eczema, allergy consultations");
        Specialty pedia = getOrCreateSpecialty("Pediatrics", "Child healthcare, vaccinations, pediatric development monitoring");
        Specialty neuro = getOrCreateSpecialty("Neurology", "Nervous system, headache management, neuropathy assessments");
        Specialty ortho = getOrCreateSpecialty("Orthopedics", "Bone, joint, fractures, and musculoskeletal conditions");
        Specialty ophth = getOrCreateSpecialty("Ophthalmology", "Eye care, vision assessments, and optical health");
        Specialty gastro = getOrCreateSpecialty("Gastroenterology", "Digestive system, stomach, and gastrointestinal health");
        Specialty psych = getOrCreateSpecialty("Psychiatry", "Mental health, anxiety, depression, and stress management");
        Specialty ent = getOrCreateSpecialty("ENT", "Ear, nose, and throat evaluations");

        // 2. Admin
        User adminUser = new User("System Administrator", "admin@carex.com", defaultPass, Role.ADMIN);
        adminUser.setPhone("+1-555-0100");
        userRepository.save(adminUser);

        // 3. Doctors
        Doctor docPriya = createDoctor("Dr. Priya Sharma", "priya@carex.com", defaultPass, "+1-555-0102", "MED-NY-1002", "MD Dermatology, FAAD", 10, new BigDecimal("120.00"), derma);
        Doctor docArun = createDoctor("Dr. Arun Kumar", "arun@carex.com", defaultPass, "+1-555-0101", "MED-NY-1001", "MD, FACC (Cardiology)", 14, new BigDecimal("150.00"), cardio);
        Doctor docKarthik = createDoctor("Dr. Karthik Rao", "karthik@carex.com", defaultPass, "+1-555-0103", "MED-NY-1003", "MBBS, MD General Medicine", 8, new BigDecimal("90.00"), genMed);

        // 4. Patients
        createPatient("Alice Walker", "alice@carex.com", defaultPass, "+1-555-0104");
        createPatient("Bob Johnson", "bob@carex.com", defaultPass, "+1-555-0105");

        // 5. Weekly Availabilities & Slots for the next 7 days (both ONLINE and OFFLINE)
        provisionSlotsForDoctor(docPriya, AppointmentMode.ONLINE);
        provisionSlotsForDoctor(docPriya, AppointmentMode.OFFLINE);
        provisionSlotsForDoctor(docArun, AppointmentMode.ONLINE);
        provisionSlotsForDoctor(docArun, AppointmentMode.OFFLINE);
        provisionSlotsForDoctor(docKarthik, AppointmentMode.ONLINE);
        provisionSlotsForDoctor(docKarthik, AppointmentMode.OFFLINE);

        log.info("CAREX Demo Data provisioned successfully! Admin, Doctor, and Patient accounts ready.");
    }

    private Specialty getOrCreateSpecialty(String name, String desc) {
        return specialtyRepository.findByName(name).orElseGet(() -> {
            Specialty s = new Specialty(name);
            s.setDescription(desc);
            s.setIsActive(true);
            return specialtyRepository.save(s);
        });
    }

    private Doctor createDoctor(String name, String email, String pass, String phone, String license, String qual, int exp, BigDecimal fee, Specialty specialty) {
        User u = new User(name, email, pass, Role.DOCTOR);
        u.setPhone(phone);
        u = userRepository.save(u);

        Doctor d = new Doctor(u, license);
        d.setQualification(qual);
        d.setExperienceYears(exp);
        d.setConsultationFee(fee);
        d.setIsActive(true);
        d = doctorRepository.save(d);

        DoctorSpecialty ds = new DoctorSpecialty(d, specialty);
        doctorSpecialtyRepository.save(ds);
        return d;
    }

    private Patient createPatient(String name, String email, String pass, String phone) {
        User u = new User(name, email, pass, Role.PATIENT);
        u.setPhone(phone);
        u = userRepository.save(u);

        Patient p = new Patient(u);
        return patientRepository.save(p);
    }

    private void provisionSlotsForDoctor(Doctor doctor, AppointmentMode mode) {
        LocalDate today = LocalDate.now();

        // Create weekly availability template
        DoctorAvailability av = new DoctorAvailability(doctor, 1, LocalTime.of(9, 0), LocalTime.of(17, 0), mode);
        av = availabilityRepository.save(av);

        // Generate slots for the next 7 days
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);

            // Slot 1: 09:30 - 10:00
            slotRepository.save(new Slot(doctor, date, LocalTime.of(9, 30), LocalTime.of(10, 0), mode));
            // Slot 2: 11:00 - 11:30
            slotRepository.save(new Slot(doctor, date, LocalTime.of(11, 0), LocalTime.of(11, 30), mode));
            // Slot 3: 14:00 - 14:30
            slotRepository.save(new Slot(doctor, date, LocalTime.of(14, 0), LocalTime.of(14, 30), mode));
            // Slot 4: 16:30 - 17:00
            slotRepository.save(new Slot(doctor, date, LocalTime.of(16, 30), LocalTime.of(17, 0), mode));
        }
    }

    /**
     * Top-up method: called on subsequent boots when data already exists.
     * Ensures every doctor has OFFLINE slots for the upcoming 7 days.
     * Uses AFTERNOON times (13:00-17:00) for OFFLINE to avoid unique constraint
     * collision with ONLINE slots that use MORNING times (09:30-12:00).
     */
    private void ensureOfflineSlotsForAllDoctors() {
        LocalDate today = LocalDate.now();
        java.util.List<Doctor> allDoctors = doctorRepository.findAll();

        for (Doctor doctor : allDoctors) {
            // Check if this doctor already has OFFLINE slots
            boolean hasOfflineSlots = slotRepository.findAll().stream().anyMatch(s ->
                    s.getDoctor() != null &&
                    s.getDoctor().getId().equals(doctor.getId()) &&
                    s.getMode() == AppointmentMode.OFFLINE &&
                    !s.getSlotDate().isBefore(today)
            );
            if (!hasOfflineSlots) {
                String docName = doctor.getUser() != null ? doctor.getUser().getName() : "ID#" + doctor.getId();
                log.info("Adding OFFLINE slots for doctor {} ({})", doctor.getId(), docName);
                for (int i = 0; i < 7; i++) {
                    LocalDate date = today.plusDays(i);
                    // Use PM times to avoid unique constraint collision with ONLINE AM slots
                    slotRepository.save(new Slot(doctor, date, LocalTime.of(13, 0), LocalTime.of(13, 30), AppointmentMode.OFFLINE));
                    slotRepository.save(new Slot(doctor, date, LocalTime.of(14, 30), LocalTime.of(15, 0), AppointmentMode.OFFLINE));
                    slotRepository.save(new Slot(doctor, date, LocalTime.of(15, 30), LocalTime.of(16, 0), AppointmentMode.OFFLINE));
                    slotRepository.save(new Slot(doctor, date, LocalTime.of(17, 0), LocalTime.of(17, 30), AppointmentMode.OFFLINE));
                }
            }
        }
        log.info("Slot top-up complete for {} doctors.", allDoctors.size());
    }
}
