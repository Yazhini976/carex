package com.carex.service.intelligence;

import com.carex.entity.*;
import com.carex.entity.enums.AppointmentMode;
import com.carex.entity.enums.AppointmentStatus;
import com.carex.entity.enums.SlotStatus;
import com.carex.entity.enums.WaitlistStatus;
import com.carex.repository.AppointmentRepository;
import com.carex.repository.WaitlistRepository;
import com.carex.service.*;
import com.carex.service.intelligence.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IntelligenceServiceTest {

    @Mock
    private SpecialtyService specialtyService;
    @Mock
    private RecommendationService recommendationService;
    @Mock
    private DoctorService doctorService;
    @Mock
    private DoctorSpecialtyService doctorSpecialtyService;
    @Mock
    private SlotService slotService;
    @Mock
    private WaitTimeService waitTimeService;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private WaitlistRepository waitlistRepository;
    @Mock
    private WorkloadService workloadService;

    private SpecialtyRecommendationServiceImpl specialtyRecommendationService;
    private DoctorMatchingServiceImpl doctorMatchingService;
    private WaitTimeServiceImpl waitTimeServiceImpl;
    private SmartWaitlistServiceImpl smartWaitlistService;
    private WorkloadIntelligenceServiceImpl workloadIntelligenceService;
    private SchedulingSimulationServiceImpl schedulingSimulationService;
    private DisruptionRecoveryServiceImpl disruptionRecoveryService;
    private NoShowPredictionServiceImpl noShowPredictionService;

    private Specialty cardiology;
    private Specialty dermatology;
    private Specialty generalMedicine;
    private Doctor doctor1;
    private Doctor doctor2;
    private Patient patient1;

    @BeforeEach
    void setUp() {
        specialtyRecommendationService = new SpecialtyRecommendationServiceImpl(specialtyService, recommendationService);
        doctorMatchingService = new DoctorMatchingServiceImpl(doctorService, doctorSpecialtyService, slotService, waitTimeService);
        waitTimeServiceImpl = new WaitTimeServiceImpl(appointmentRepository);
        smartWaitlistService = new SmartWaitlistServiceImpl(waitlistRepository);
        workloadIntelligenceService = new WorkloadIntelligenceServiceImpl(workloadService, doctorService, appointmentRepository);
        schedulingSimulationService = new SchedulingSimulationServiceImpl(appointmentRepository, doctorService, doctorSpecialtyService);
        disruptionRecoveryService = new DisruptionRecoveryServiceImpl(appointmentRepository, doctorService, doctorSpecialtyService, slotService);
        noShowPredictionService = new NoShowPredictionServiceImpl(appointmentRepository);

        cardiology = new Specialty();
        cardiology.setId(1L);
        cardiology.setName("Cardiology");
        cardiology.setIsActive(true);

        dermatology = new Specialty();
        dermatology.setId(2L);
        dermatology.setName("Dermatology");
        dermatology.setIsActive(true);

        generalMedicine = new Specialty();
        generalMedicine.setId(3L);
        generalMedicine.setName("General Medicine");
        generalMedicine.setIsActive(true);

        User docUser1 = new User();
        docUser1.setId(10L);
        docUser1.setName("Dr. Priya");

        doctor1 = new Doctor();
        doctor1.setId(100L);
        doctor1.setUser(docUser1);
        doctor1.setExperienceYears(8);
        doctor1.setConsultationFee(BigDecimal.valueOf(100));
        doctor1.setIsActive(true);

        User docUser2 = new User();
        docUser2.setId(20L);
        docUser2.setName("Dr. Arun");

        doctor2 = new Doctor();
        doctor2.setId(200L);
        doctor2.setUser(docUser2);
        doctor2.setExperienceYears(3);
        doctor2.setConsultationFee(BigDecimal.valueOf(80));
        doctor2.setIsActive(true);

        User patUser = new User();
        patUser.setId(30L);
        patUser.setName("John Doe");

        patient1 = new Patient();
        patient1.setId(500L);
        patient1.setUser(patUser);
    }

    // ==========================================
    // 1. SMART SPECIALTY RECOMMENDATION TESTS
    // ==========================================
    @Test
    @DisplayName("Specialty Recommendation: Recognizes cardiology keyword")
    void testSpecialtyRecommendation_CardiologyKeyword() {
        when(specialtyService.getActiveSpecialties()).thenReturn(List.of(cardiology, dermatology, generalMedicine));

        var result = specialtyRecommendationService.recommend(500L, "I have severe chest pain and palpitations");

        assertTrue(result.recommendedSpecialty().isPresent());
        assertEquals("Cardiology", result.recommendedSpecialty().get().getName());
        assertTrue(result.confidence() >= 0.80);
        assertTrue(result.reason().contains("Cardiology"));
        verify(recommendationService).recordRecommendation(eq(500L), anyString(), eq(1L), anyString());
    }

    @Test
    @DisplayName("Specialty Recommendation: Recognizes dermatology keyword")
    void testSpecialtyRecommendation_DermatologyKeyword() {
        when(specialtyService.getActiveSpecialties()).thenReturn(List.of(cardiology, dermatology, generalMedicine));

        var result = specialtyRecommendationService.recommend(500L, "I have an itchy skin rash on my face");

        assertTrue(result.recommendedSpecialty().isPresent());
        assertEquals("Dermatology", result.recommendedSpecialty().get().getName());
    }

    @Test
    @DisplayName("Specialty Recommendation: Unknown keyword falls back to General Medicine")
    void testSpecialtyRecommendation_FallbackToGeneralMedicine() {
        when(specialtyService.getActiveSpecialties()).thenReturn(List.of(cardiology, dermatology, generalMedicine));

        var result = specialtyRecommendationService.recommend(500L, "Unspecified overall malaise and discomfort");

        assertTrue(result.recommendedSpecialty().isPresent());
        assertEquals("General Medicine", result.recommendedSpecialty().get().getName());
        assertTrue(result.confidence() <= 0.70);
    }

    // ==========================================
    // 2. EXPLAINABLE DOCTOR MATCHING TESTS
    // ==========================================
    @Test
    @DisplayName("Doctor Matching: Ranks doctors by specialty, mode, availability, experience")
    void testDoctorMatching_RankingAndExplainability() {
        when(doctorService.getActiveDoctors()).thenReturn(List.of(doctor1, doctor2));
        when(doctorSpecialtyService.isAssigned(100L, 1L)).thenReturn(true);
        when(doctorSpecialtyService.isAssigned(200L, 1L)).thenReturn(false);
        when(doctorSpecialtyService.getSpecialtiesForDoctor(100L)).thenReturn(List.of(cardiology));

        Slot slot = new Slot();
        slot.setId(1L);
        slot.setMode(AppointmentMode.ONLINE);
        slot.setStatus(SlotStatus.AVAILABLE);

        when(slotService.getAvailableSlotsForDoctorAndDate(eq(100L), any(LocalDate.class))).thenReturn(List.of(slot));
        when(slotService.getAvailableSlotsForDoctorAndDate(eq(200L), any(LocalDate.class))).thenReturn(List.of());
        when(waitTimeService.estimate(anyLong(), any(LocalDate.class), any())).thenReturn(new WaitTimeService.WaitTimeEstimate(10, "BASELINE", "Disclaimer"));

        List<DoctorMatchingService.DoctorMatchResult> results = doctorMatchingService.rankDoctors(1L, AppointmentMode.ONLINE);

        assertEquals(2, results.size());
        assertEquals(doctor1.getId(), results.get(0).doctor().getId());
        assertTrue(results.get(0).specialtyMatch());
        assertTrue(results.get(0).modeMatch());
        assertTrue(results.get(0).hasAvailableSlot());
        assertTrue(results.get(0).score() > results.get(1).score());
        assertNotNull(results.get(0).reasoning());
    }

    // ==========================================
    // 3. WAITING TIME PREDICTION TESTS
    // ==========================================
    @Test
    @DisplayName("Wait Time Prediction: Calculates queue delay accurately")
    void testWaitTime_QueueCalculation() {
        Appointment a1 = new Appointment();
        a1.setId(1L);
        Appointment a2 = new Appointment();
        a2.setId(2L);
        Appointment a3 = new Appointment();
        a3.setId(3L);

        when(appointmentRepository.findActiveByDoctorIdAndDate(eq(100L), any(LocalDate.class)))
                .thenReturn(List.of(a1, a2, a3));

        var estimate = waitTimeServiceImpl.estimate(100L, LocalDate.now(), 4L);

        // 3 appointments ahead: 3 * 15 min (45 min) + (3/3)*5 min buffer (5 min) = 50 min
        assertEquals(50, estimate.predictedMinutes());
        assertNotNull(estimate.disclaimer());
    }

    @Test
    @DisplayName("Wait Time Prediction: Zero appointments returns 0 wait time")
    void testWaitTime_EmptyQueue() {
        when(appointmentRepository.findActiveByDoctorIdAndDate(eq(100L), any(LocalDate.class)))
                .thenReturn(List.of());

        var estimate = waitTimeServiceImpl.estimate(100L, LocalDate.now(), 1L);
        assertEquals(0, estimate.predictedMinutes());
    }

    // ==========================================
    // 4. SMART WAITLIST RANKING TESTS
    // ==========================================
    @Test
    @DisplayName("Smart Waitlist: Ranks candidate patients with multi-factor scoring")
    void testSmartWaitlist_Ranking() {
        Waitlist w1 = new Waitlist();
        w1.setId(1L);
        w1.setSpecialty(cardiology);
        w1.setPreferredDoctor(doctor1);
        w1.setPreferredMode(AppointmentMode.ONLINE);
        w1.setPreferredDate(LocalDate.now());
        w1.setCreatedAt(LocalDateTime.now().minusDays(3));
        w1.setStatus(WaitlistStatus.WAITING);

        Waitlist w2 = new Waitlist();
        w2.setId(2L);
        w2.setSpecialty(cardiology);
        w2.setPreferredDoctor(null); // flexible
        w2.setPreferredMode(AppointmentMode.OFFLINE);
        w2.setPreferredDate(LocalDate.now().plusDays(5));
        w2.setCreatedAt(LocalDateTime.now().minusDays(1));
        w2.setStatus(WaitlistStatus.WAITING);

        when(waitlistRepository.findBySpecialtyIdAndStatus(1L, WaitlistStatus.WAITING)).thenReturn(List.of(w1, w2));

        var results = smartWaitlistService.rankWaitingPatients(1L, 100L, AppointmentMode.ONLINE, LocalDate.now());

        assertEquals(2, results.size());
        assertEquals(1L, results.get(0).waitlistEntry().getId());
        assertEquals(1, results.get(0).rank());
        assertTrue(results.get(0).matchScore() > results.get(1).matchScore());
        assertFalse(results.get(0).scoringFactors().isEmpty());
    }

    // ==========================================
    // 5. DOCTOR WORKLOAD INTELLIGENCE TESTS
    // ==========================================
    @Test
    @DisplayName("Workload Intelligence: Computes load levels and actionable recommendations")
    void testWorkloadIntelligence_Analysis() {
        Appointment a1 = new Appointment();
        a1.setStatus(AppointmentStatus.CONFIRMED);
        Slot s1 = new Slot();
        s1.setStartTime(LocalTime.of(17, 0)); // Peak hour
        a1.setSlot(s1);

        Appointment a2 = new Appointment();
        a2.setStatus(AppointmentStatus.BOOKED);
        Slot s2 = new Slot();
        s2.setStartTime(LocalTime.of(18, 0)); // Peak hour
        a2.setSlot(s2);

        when(appointmentRepository.findByDoctorIdAndSlotDate(eq(100L), any(LocalDate.class)))
                .thenReturn(List.of(a1, a2));

        var analysis = workloadIntelligenceService.analyzeDoctor(100L);

        assertEquals(100L, analysis.doctorId());
        assertEquals(2, analysis.appointmentCount());
        assertNotNull(analysis.loadLevel());
        assertNotNull(analysis.recommendation());
    }

    // ==========================================
    // 6. WHAT-IF SCHEDULING SIMULATION TESTS
    // ==========================================
    @Test
    @DisplayName("Scheduling Simulation: Strictly read-only, calculates impact and redistribution")
    void testSchedulingSimulation_ReadOnlyDisruption() {
        Appointment a1 = new Appointment();
        a1.setId(10L);
        a1.setStatus(AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findActiveByDoctorIdAndDate(eq(100L), any(LocalDate.class)))
                .thenReturn(List.of(a1));
        when(doctorSpecialtyService.getSpecialtiesForDoctor(100L)).thenReturn(List.of(cardiology));
        when(doctorService.getActiveDoctors()).thenReturn(List.of(doctor1, doctor2));
        when(doctorSpecialtyService.isAssigned(200L, 1L)).thenReturn(true);

        var result = schedulingSimulationService.simulateDoctorUnavailable(100L, LocalDate.now());

        assertTrue(result.readOnly(), "Simulation MUST strictly be read-only!");
        assertEquals(1, result.affectedAppointmentCount());
        assertEquals(1, result.candidateAlternativeDoctors().size());
        assertEquals(doctor2.getId(), result.candidateAlternativeDoctors().get(0).getId());
        assertTrue(result.redistributionSuggestion().contains("SIMULATION"));

        // Verify zero mutation calls to repository
        verify(appointmentRepository, never()).save(any());
        verify(appointmentRepository, never()).delete(any());
    }

    // ==========================================
    // 7. DISRUPTION RECOVERY TESTS
    // ==========================================
    @Test
    @DisplayName("Disruption Recovery: Proposes alternative patient reassignments for admin approval")
    void testDisruptionRecovery_Evaluation() {
        Appointment a1 = new Appointment();
        a1.setId(10L);
        a1.setPatient(patient1);
        a1.setSpecialty(cardiology);
        a1.setMode(AppointmentMode.ONLINE);
        Slot slot = new Slot();
        slot.setStartTime(LocalTime.of(14, 0));
        a1.setSlot(slot);

        when(doctorService.getDoctorById(100L)).thenReturn(doctor1);
        when(appointmentRepository.findActiveByDoctorIdAndDate(eq(100L), any(LocalDate.class))).thenReturn(List.of(a1));
        when(doctorSpecialtyService.getSpecialtiesForDoctor(100L)).thenReturn(List.of(cardiology));
        when(doctorService.getActiveDoctors()).thenReturn(List.of(doctor1, doctor2));
        when(doctorSpecialtyService.isAssigned(200L, 1L)).thenReturn(true);
        when(slotService.getAvailableSlotsForDoctorAndDate(eq(200L), any(LocalDate.class))).thenReturn(List.of());

        var result = disruptionRecoveryService.evaluateRecovery(100L, LocalDate.now());

        assertTrue(result.readOnly());
        assertEquals(1, result.affectedAppointmentCount());
        assertEquals(1, result.patientPlans().size());
        assertEquals(doctor2.getId(), result.patientPlans().get(0).suggestedDoctor().getId());
        assertNotNull(result.summaryRecommendation());

        // Zero database modifications
        verify(appointmentRepository, never()).save(any());
    }

    // ==========================================
    // 8. NO-SHOW RISK ESTIMATION TESTS
    // ==========================================
    @Test
    @DisplayName("No-Show Risk: Detects past no-show history and calculates attendance probability")
    void testNoShowPrediction_PastNoShowHistory() {
        Appointment currentAppt = new Appointment();
        currentAppt.setId(99L);
        currentAppt.setPatient(patient1);
        currentAppt.setStatus(AppointmentStatus.BOOKED);

        Appointment pastCompleted = new Appointment();
        pastCompleted.setStatus(AppointmentStatus.COMPLETED);

        Appointment pastNoShow = new Appointment();
        pastNoShow.setStatus(AppointmentStatus.NO_SHOW);

        when(appointmentRepository.findById(99L)).thenReturn(Optional.of(currentAppt));
        when(appointmentRepository.findByPatientId(500L)).thenReturn(List.of(pastCompleted, pastNoShow));

        var prediction = noShowPredictionService.predict(99L);

        assertEquals(99L, prediction.appointmentId());
        assertTrue(prediction.noShowProbability() >= 0.30, "No-show probability should be elevated due to past no-show history");
        assertNotNull(prediction.riskLevel());
        assertFalse(prediction.basis().isEmpty());
    }

    @Test
    @DisplayName("No-Show Risk: New patient gets baseline risk")
    void testNoShowPrediction_NewPatient() {
        Appointment currentAppt = new Appointment();
        currentAppt.setId(99L);
        currentAppt.setPatient(patient1);
        currentAppt.setStatus(AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findById(99L)).thenReturn(Optional.of(currentAppt));
        when(appointmentRepository.findByPatientId(500L)).thenReturn(List.of());

        var prediction = noShowPredictionService.predict(99L);

        assertEquals(0.10, prediction.noShowProbability());
        assertEquals("LOW", prediction.riskLevel());
    }
}
