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
            log.info("CAREX Demo Data already provisioned. Topping up missing specialty doctors and OFFLINE slots...");
            ensureAllSpecialtyDoctorsExist();
            ensureOfflineSlotsForAllDoctors();
            return;
        }

        log.info("Provisioning CAREX Deterministic Hackathon Demo Dataset...");

        String defaultPass = passwordEncoder.encode("Password123!");

        // 1. Specialties
        Specialty genMed = getOrCreateSpecialty("General Medicine", "Primary healthcare, routine consultations, preventative checkups");
        Specialty cardio = getOrCreateSpecialty("Cardiology", "Heart, blood vessels, hypertension, chest discomfort evaluations");
        Specialty derma  = getOrCreateSpecialty("Dermatology", "Skin conditions, rashes, eczema, allergy consultations");
        Specialty pedia  = getOrCreateSpecialty("Pediatrics", "Child healthcare, vaccinations, pediatric development monitoring");
        Specialty neuro  = getOrCreateSpecialty("Neurology", "Nervous system, headache management, neuropathy assessments");
        Specialty ortho  = getOrCreateSpecialty("Orthopedics", "Bone, joint, fractures, and musculoskeletal conditions");
        Specialty ophth  = getOrCreateSpecialty("Ophthalmology", "Eye care, vision assessments, and optical health");
        Specialty gastro = getOrCreateSpecialty("Gastroenterology", "Digestive system, stomach, and gastrointestinal health");
        Specialty psych  = getOrCreateSpecialty("Psychiatry", "Mental health, anxiety, depression, and stress management");
        Specialty ent    = getOrCreateSpecialty("ENT", "Ear, nose, and throat evaluations");
        Specialty pulmo  = getOrCreateSpecialty("Pulmonology", "Lungs, asthma, respiratory and breathing disorders");
        Specialty endo   = getOrCreateSpecialty("Endocrinology", "Diabetes, thyroid, hormonal and metabolic disorders");
        Specialty uro    = getOrCreateSpecialty("Urology", "Kidney, bladder, urinary tract and prostate conditions");
        Specialty dent   = getOrCreateSpecialty("Dentistry", "Dental, oral hygiene, tooth and gum health");

        // 2. Admin
        User adminUser = new User("System Administrator", "admin@carex.com", passwordEncoder.encode("Password123!"), Role.ADMIN);
        adminUser.setPhone("+1-555-0100");
        userRepository.save(adminUser);

        // 3. Doctors — one per specialty
        Doctor docPriya    = createDoctor("Dr. Priya Sharma",    "priya@carex.com",    defaultPass, "+1-555-0102", "MED-NY-1002", "MD Dermatology, FAAD",              10, new BigDecimal("120.00"), derma);
        Doctor docArun     = createDoctor("Dr. Arun Kumar",      "arun@carex.com",     defaultPass, "+1-555-0101", "MED-NY-1001", "MD, FACC (Cardiology)",             14, new BigDecimal("150.00"), cardio);
        Doctor docKarthik  = createDoctor("Dr. Karthik Rao",     "karthik@carex.com",  defaultPass, "+1-555-0103", "MED-NY-1003", "MBBS, MD General Medicine",         8,  new BigDecimal("90.00"),  genMed);
        Doctor docMeena    = createDoctor("Dr. Meena Nair",      "meena@carex.com",    defaultPass, "+1-555-0106", "MED-NY-1006", "MD Pediatrics, DCH",                12, new BigDecimal("110.00"), pedia);
        Doctor docRavi     = createDoctor("Dr. Ravi Shankar",    "ravi@carex.com",     defaultPass, "+1-555-0107", "MED-NY-1007", "DM Neurology, MBBS",                9,  new BigDecimal("140.00"), neuro);
        Doctor docSuresh   = createDoctor("Dr. Suresh Iyer",     "suresh@carex.com",   defaultPass, "+1-555-0108", "MED-NY-1008", "MS Orthopedics, DNB",               11, new BigDecimal("130.00"), ortho);
        Doctor docAnitha   = createDoctor("Dr. Anitha Reddy",    "anitha@carex.com",   defaultPass, "+1-555-0109", "MED-NY-1009", "MS Ophthalmology, DO",              7,  new BigDecimal("100.00"), ophth);
        Doctor docVijay    = createDoctor("Dr. Vijay Menon",     "vijay@carex.com",    defaultPass, "+1-555-0110", "MED-NY-1010", "DM Gastroenterology, MD",           13, new BigDecimal("135.00"), gastro);
        Doctor docLakshmi  = createDoctor("Dr. Lakshmi Devi",    "lakshmi@carex.com",  defaultPass, "+1-555-0111", "MED-NY-1011", "MD Psychiatry, DPM",                6,  new BigDecimal("95.00"),  psych);
        Doctor docPrakash  = createDoctor("Dr. Prakash Nair",    "prakash@carex.com",  defaultPass, "+1-555-0112", "MED-NY-1012", "MS ENT, DLO",                       8,  new BigDecimal("105.00"), ent);
        Doctor docSaraswati= createDoctor("Dr. Saraswati Bhat",  "saraswati@carex.com",defaultPass, "+1-555-0113", "MED-NY-1013", "MD Pulmonology, DTCD",              9,  new BigDecimal("125.00"), pulmo);
        Doctor docHarish   = createDoctor("Dr. Harish Gupta",    "harish@carex.com",   defaultPass, "+1-555-0114", "MED-NY-1014", "DM Endocrinology, MD",              15, new BigDecimal("145.00"), endo);
        Doctor docNandini  = createDoctor("Dr. Nandini Pillai",  "nandini@carex.com",  defaultPass, "+1-555-0115", "MED-NY-1015", "MCh Urology, MS",                   10, new BigDecimal("130.00"), uro);
        Doctor docBalaji   = createDoctor("Dr. Balaji Krishnan", "balaji@carex.com",   defaultPass, "+1-555-0116", "MED-NY-1016", "MDS Dentistry, BDS",                5,  new BigDecimal("80.00"),  dent);

        // 4. Patients
        createPatient("Alice Walker", "alice@carex.com", defaultPass, "+1-555-0104");
        createPatient("Bob Johnson",  "bob@carex.com",   defaultPass, "+1-555-0105");

        // 5. Slots for all doctors (both ONLINE and OFFLINE)
        for (Doctor doc : new Doctor[]{docPriya, docArun, docKarthik, docMeena, docRavi, docSuresh,
                docAnitha, docVijay, docLakshmi, docPrakash, docSaraswati, docHarish, docNandini, docBalaji}) {
            provisionSlotsForDoctor(doc, AppointmentMode.ONLINE);
            provisionSlotsForDoctor(doc, AppointmentMode.OFFLINE);
        }

        log.info("CAREX Demo Data provisioned! 14 doctors across 14 specialties ready.");
    }

    /**
     * Top-up: ensures a doctor exists for every specialty on subsequent boots.
     * Called when data already exists (DB already seeded).
     */
    private void ensureAllSpecialtyDoctorsExist() {
        String defaultPass = passwordEncoder.encode("Password123!");
        String[][] specialtyDoctors = {
            {"Pediatrics",       "Dr. Meena Nair",       "meena@carex.com",       "+1-555-0106", "MED-NY-1006", "MD Pediatrics, DCH",         "12", "110.00"},
            {"Neurology",        "Dr. Ravi Shankar",     "ravi@carex.com",        "+1-555-0107", "MED-NY-1007", "DM Neurology, MBBS",         "9",  "140.00"},
            {"Orthopedics",      "Dr. Suresh Iyer",      "suresh@carex.com",      "+1-555-0108", "MED-NY-1008", "MS Orthopedics, DNB",        "11", "130.00"},
            {"Ophthalmology",    "Dr. Anitha Reddy",     "anitha@carex.com",      "+1-555-0109", "MED-NY-1009", "MS Ophthalmology, DO",       "7",  "100.00"},
            {"Gastroenterology", "Dr. Vijay Menon",      "vijay@carex.com",       "+1-555-0110", "MED-NY-1010", "DM Gastroenterology, MD",    "13", "135.00"},
            {"Psychiatry",       "Dr. Lakshmi Devi",     "lakshmi@carex.com",     "+1-555-0111", "MED-NY-1011", "MD Psychiatry, DPM",         "6",  "95.00"},
            {"ENT",              "Dr. Prakash Nair",     "prakash@carex.com",     "+1-555-0112", "MED-NY-1012", "MS ENT, DLO",                "8",  "105.00"},
            {"Pulmonology",      "Dr. Saraswati Bhat",   "saraswati@carex.com",   "+1-555-0113", "MED-NY-1013", "MD Pulmonology, DTCD",       "9",  "125.00"},
            {"Endocrinology",    "Dr. Harish Gupta",     "harish@carex.com",      "+1-555-0114", "MED-NY-1014", "DM Endocrinology, MD",       "15", "145.00"},
            {"Urology",          "Dr. Nandini Pillai",   "nandini@carex.com",     "+1-555-0115", "MED-NY-1015", "MCh Urology, MS",            "10", "130.00"},
            {"Dentistry",        "Dr. Balaji Krishnan",  "balaji@carex.com",      "+1-555-0116", "MED-NY-1016", "MDS Dentistry, BDS",         "5",  "80.00"},
        };

        for (String[] row : specialtyDoctors) {
            String specialtyName = row[0];
            String email = row[2];
            // Skip if doctor already exists
            if (userRepository.findByEmail(email).isPresent()) continue;

            Specialty spec = getOrCreateSpecialty(specialtyName, specialtyName + " specialist");
            Doctor doc = createDoctor(row[1], email, defaultPass, row[3], row[4], row[5],
                    Integer.parseInt(row[6]), new BigDecimal(row[7]), spec);
            provisionSlotsForDoctor(doc, AppointmentMode.ONLINE);
            provisionSlotsForDoctor(doc, AppointmentMode.OFFLINE);
            log.info("Top-up: Created {} for specialty {}", row[1], specialtyName);
        }
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
