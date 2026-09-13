package com.carex.service.intelligence.impl;

import com.carex.entity.Specialty;
import com.carex.service.RecommendationService;
import com.carex.service.SpecialtyService;
import com.carex.service.intelligence.SpecialtyRecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Deterministic keyword-based specialty recommendation implementation.
 *
 * <p>Maps patient-described symptoms/keywords to specialties using a curated keyword map.
 * This is purely an appointment navigation aid and NOT a medical diagnosis tool.</p>
 */
@Service
@Transactional(readOnly = true)
public class SpecialtyRecommendationServiceImpl implements SpecialtyRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(SpecialtyRecommendationServiceImpl.class);

    private static final Map<String, String> KEYWORD_TO_SPECIALTY;
    static {
        KEYWORD_TO_SPECIALTY = new LinkedHashMap<>();
        // Cardiology
        KEYWORD_TO_SPECIALTY.put("heart", "Cardiology");
        KEYWORD_TO_SPECIALTY.put("chest pain", "Cardiology");
        KEYWORD_TO_SPECIALTY.put("palpitation", "Cardiology");
        KEYWORD_TO_SPECIALTY.put("blood pressure", "Cardiology");
        KEYWORD_TO_SPECIALTY.put("cardiovascular", "Cardiology");
        KEYWORD_TO_SPECIALTY.put("hypertension", "Cardiology");

        // Dermatology
        KEYWORD_TO_SPECIALTY.put("skin", "Dermatology");
        KEYWORD_TO_SPECIALTY.put("rash", "Dermatology");
        KEYWORD_TO_SPECIALTY.put("acne", "Dermatology");
        KEYWORD_TO_SPECIALTY.put("eczema", "Dermatology");
        KEYWORD_TO_SPECIALTY.put("itch", "Dermatology");
        KEYWORD_TO_SPECIALTY.put("mole", "Dermatology");

        // Orthopedics
        KEYWORD_TO_SPECIALTY.put("bone", "Orthopedics");
        KEYWORD_TO_SPECIALTY.put("joint", "Orthopedics");
        KEYWORD_TO_SPECIALTY.put("fracture", "Orthopedics");
        KEYWORD_TO_SPECIALTY.put("back pain", "Orthopedics");
        KEYWORD_TO_SPECIALTY.put("knee", "Orthopedics");
        KEYWORD_TO_SPECIALTY.put("spine", "Orthopedics");
        KEYWORD_TO_SPECIALTY.put("arthritis", "Orthopedics");

        // Pediatrics
        KEYWORD_TO_SPECIALTY.put("child", "Pediatrics");
        KEYWORD_TO_SPECIALTY.put("kid", "Pediatrics");
        KEYWORD_TO_SPECIALTY.put("infant", "Pediatrics");
        KEYWORD_TO_SPECIALTY.put("baby", "Pediatrics");
        KEYWORD_TO_SPECIALTY.put("newborn", "Pediatrics");
        KEYWORD_TO_SPECIALTY.put("pediatric", "Pediatrics");

        // Ophthalmology
        KEYWORD_TO_SPECIALTY.put("eye", "Ophthalmology");
        KEYWORD_TO_SPECIALTY.put("vision", "Ophthalmology");
        KEYWORD_TO_SPECIALTY.put("sight", "Ophthalmology");
        KEYWORD_TO_SPECIALTY.put("cataract", "Ophthalmology");
        KEYWORD_TO_SPECIALTY.put("glaucoma", "Ophthalmology");

        // Dentistry
        KEYWORD_TO_SPECIALTY.put("tooth", "Dentistry");
        KEYWORD_TO_SPECIALTY.put("teeth", "Dentistry");
        KEYWORD_TO_SPECIALTY.put("dental", "Dentistry");
        KEYWORD_TO_SPECIALTY.put("gum", "Dentistry");

        // Gastroenterology
        KEYWORD_TO_SPECIALTY.put("stomach", "Gastroenterology");
        KEYWORD_TO_SPECIALTY.put("digestion", "Gastroenterology");
        KEYWORD_TO_SPECIALTY.put("acidity", "Gastroenterology");
        KEYWORD_TO_SPECIALTY.put("abdomen", "Gastroenterology");
        KEYWORD_TO_SPECIALTY.put("bowel", "Gastroenterology");
        KEYWORD_TO_SPECIALTY.put("gastric", "Gastroenterology");

        // Neurology
        KEYWORD_TO_SPECIALTY.put("headache", "Neurology");
        KEYWORD_TO_SPECIALTY.put("migraine", "Neurology");
        KEYWORD_TO_SPECIALTY.put("seizure", "Neurology");
        KEYWORD_TO_SPECIALTY.put("dizzy", "Neurology");
        KEYWORD_TO_SPECIALTY.put("nerve", "Neurology");
        KEYWORD_TO_SPECIALTY.put("numbness", "Neurology");

        // Psychiatry
        KEYWORD_TO_SPECIALTY.put("mental", "Psychiatry");
        KEYWORD_TO_SPECIALTY.put("anxiety", "Psychiatry");
        KEYWORD_TO_SPECIALTY.put("depression", "Psychiatry");
        KEYWORD_TO_SPECIALTY.put("stress", "Psychiatry");
        KEYWORD_TO_SPECIALTY.put("insomnia", "Psychiatry");

        // Urology
        KEYWORD_TO_SPECIALTY.put("urine", "Urology");
        KEYWORD_TO_SPECIALTY.put("kidney", "Urology");
        KEYWORD_TO_SPECIALTY.put("bladder", "Urology");
        KEYWORD_TO_SPECIALTY.put("prostate", "Urology");

        // Endocrinology
        KEYWORD_TO_SPECIALTY.put("diabetes", "Endocrinology");
        KEYWORD_TO_SPECIALTY.put("thyroid", "Endocrinology");
        KEYWORD_TO_SPECIALTY.put("hormone", "Endocrinology");

        // ENT / Pulmonology
        KEYWORD_TO_SPECIALTY.put("ear", "ENT");
        KEYWORD_TO_SPECIALTY.put("throat", "ENT");
        KEYWORD_TO_SPECIALTY.put("nose", "ENT");
        KEYWORD_TO_SPECIALTY.put("lung", "Pulmonology");
        KEYWORD_TO_SPECIALTY.put("breath", "Pulmonology");
        KEYWORD_TO_SPECIALTY.put("asthma", "Pulmonology");

        // General Medicine
        KEYWORD_TO_SPECIALTY.put("fever", "General Medicine");
        KEYWORD_TO_SPECIALTY.put("cold", "General Medicine");
        KEYWORD_TO_SPECIALTY.put("cough", "General Medicine");
        KEYWORD_TO_SPECIALTY.put("flu", "General Medicine");
        KEYWORD_TO_SPECIALTY.put("general", "General Medicine");
        KEYWORD_TO_SPECIALTY.put("routine", "General Medicine");
        KEYWORD_TO_SPECIALTY.put("checkup", "General Medicine");
    }

    private final SpecialtyService specialtyService;
    private final RecommendationService recommendationService;
    private final com.carex.service.intelligence.llm.LlmSpecialtyRouterService llmSpecialtyRouterService;

    public SpecialtyRecommendationServiceImpl(SpecialtyService specialtyService,
                                                RecommendationService recommendationService) {
        this(specialtyService, recommendationService, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public SpecialtyRecommendationServiceImpl(SpecialtyService specialtyService,
                                                RecommendationService recommendationService,
                                                com.carex.service.intelligence.llm.LlmSpecialtyRouterService llmSpecialtyRouterService) {
        this.specialtyService = specialtyService;
        this.recommendationService = recommendationService;
        this.llmSpecialtyRouterService = llmSpecialtyRouterService;
    }

    @Override
    @Transactional
    public RecommendationResult recommend(Long patientId, String inputText) {
        if (inputText == null || inputText.trim().isEmpty()) {
            return new RecommendationResult(
                    Optional.empty(),
                    "Please provide clinical details or describe your symptoms for appointment routing.",
                    0.0
            );
        }

        String matchedSpecialtyName = null;
        String reason = null;
        double confidence = 0.85;

        // 1. Query the Hybrid LLM / Semantic AI Router
        if (llmSpecialtyRouterService != null) {
            var llmOpt = llmSpecialtyRouterService.routeWithLlm(inputText);
            if (llmOpt.isPresent()) {
                var llmRes = llmOpt.get();
                matchedSpecialtyName = llmRes.specialtyName();
                reason = llmRes.reasoning();
                confidence = llmRes.confidence();
            }
        }

        // 2. Deterministic keyword fallback if LLM returned null
        if (matchedSpecialtyName == null) {
            String lower = inputText.toLowerCase();
            String matchedKeyword = null;
            for (Map.Entry<String, String> entry : KEYWORD_TO_SPECIALTY.entrySet()) {
                if (lower.contains(entry.getKey())) {
                    matchedKeyword = entry.getKey();
                    matchedSpecialtyName = entry.getValue();
                    confidence = 0.85;
                    reason = "Based on symptom keywords ('" + matchedKeyword + "'), consultation with " + matchedSpecialtyName +
                             " is recommended for scheduling guidance.";
                    break;
                }
            }
        }

        Optional<Specialty> recommended = Optional.empty();
        Long recommendedId = null;

        if (matchedSpecialtyName != null) {
            String specialtyName = matchedSpecialtyName;
            Optional<Specialty> found = specialtyService.getActiveSpecialties()
                    .stream()
                    .filter(s -> s.getName().equalsIgnoreCase(specialtyName))
                    .findFirst();
            
            // If not found by exact active specialty, try fallback to General Medicine or any matching
            if (found.isEmpty()) {
                found = specialtyService.getActiveSpecialties().stream()
                        .filter(s -> s.getName().toLowerCase().contains(specialtyName.toLowerCase()))
                        .findFirst();
            }
            if (found.isEmpty()) {
                found = specialtyService.getActiveSpecialties().stream()
                        .filter(s -> s.getName().equalsIgnoreCase("General Medicine"))
                        .findFirst();
            }

            recommended = found;
            recommendedId = found.map(Specialty::getId).orElse(null);
            if (reason == null) {
                reason = "Consultation with " + matchedSpecialtyName + " is recommended for scheduling guidance.";
            }
        } else {
            // Default fallback to General Medicine if available
            Optional<Specialty> genMed = specialtyService.getActiveSpecialties().stream()
                    .filter(s -> s.getName().equalsIgnoreCase("General Medicine"))
                    .findFirst();
            recommended = genMed;
            recommendedId = genMed.map(Specialty::getId).orElse(null);
            confidence = 0.50;
            reason = "No specific sub-specialty keyword detected. General Medicine is recommended for initial assessment and triage.";
        }

        // Persist the recommendation event safely
        try {
            if (patientId != null) {
                recommendationService.recordRecommendation(patientId, inputText, recommendedId, reason);
            }
        } catch (Exception e) {
            log.warn("Could not persist recommendation log for patient={}: {}", patientId, e.getMessage());
        }

        log.info("Specialty recommendation for patient={}: specialty={} confidence={}", patientId, matchedSpecialtyName, confidence);
        return new RecommendationResult(recommended, reason, confidence);
    }

    @Override
    public List<String> getSupportedKeywords() {
        return List.copyOf(KEYWORD_TO_SPECIALTY.keySet());
    }
}
