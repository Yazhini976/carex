package com.carex.service.impl;

import com.carex.entity.Patient;
import com.carex.entity.RecommendationLog;
import com.carex.entity.Specialty;
import com.carex.repository.RecommendationLogRepository;
import com.carex.service.PatientService;
import com.carex.service.RecommendationService;
import com.carex.service.SpecialtyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationLogRepository recommendationLogRepository;
    private final com.carex.repository.PatientRepository patientRepository;
    private final com.carex.repository.SpecialtyRepository specialtyRepository;

    public RecommendationServiceImpl(RecommendationLogRepository recommendationLogRepository,
                                      com.carex.repository.PatientRepository patientRepository,
                                      com.carex.repository.SpecialtyRepository specialtyRepository) {
        this.recommendationLogRepository = recommendationLogRepository;
        this.patientRepository = patientRepository;
        this.specialtyRepository = specialtyRepository;
    }

    @Override
    @Transactional
    public RecommendationLog recordRecommendation(Long patientId, String inputText,
                                                   Long recommendedSpecialtyId, String reason) {
        RecommendationLog log = new RecommendationLog(inputText);
        if (patientId != null) {
            patientRepository.findById(patientId).ifPresent(log::setPatient);
        }
        if (recommendedSpecialtyId != null) {
            specialtyRepository.findById(recommendedSpecialtyId).ifPresent(log::setRecommendedSpecialty);
        }
        log.setRecommendationReason(reason);
        return recommendationLogRepository.save(log);
    }

    @Override
    public List<RecommendationLog> getRecommendationsForPatient(Long patientId) {
        return recommendationLogRepository.findByPatientId(patientId);
    }

    @Override
    public List<RecommendationLog> getRecommendationsForSpecialty(Long specialtyId) {
        return recommendationLogRepository.findByRecommendedSpecialtyId(specialtyId);
    }
}
