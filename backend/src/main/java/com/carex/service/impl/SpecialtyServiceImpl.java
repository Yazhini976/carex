package com.carex.service.impl;

import com.carex.entity.Specialty;
import com.carex.exception.DuplicateResourceException;
import com.carex.exception.ResourceNotFoundException;
import com.carex.repository.SpecialtyRepository;
import com.carex.service.SpecialtyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SpecialtyServiceImpl implements SpecialtyService {

    private static final Logger log = LoggerFactory.getLogger(SpecialtyServiceImpl.class);

    private final SpecialtyRepository specialtyRepository;

    public SpecialtyServiceImpl(SpecialtyRepository specialtyRepository) {
        this.specialtyRepository = specialtyRepository;
    }

    @Override
    @Transactional
    public Specialty createSpecialty(String name, String description) {
        if (specialtyRepository.existsByName(name)) {
            throw DuplicateResourceException.of("Specialty", "name", name);
        }
        Specialty specialty = new Specialty(name);
        specialty.setDescription(description);
        Specialty saved = specialtyRepository.save(specialty);
        log.info("Created specialty id={} name={}", saved.getId(), name);
        return saved;
    }

    @Override
    public Specialty getSpecialtyById(Long id) {
        return specialtyRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Specialty", id));
    }

    @Override
    public List<Specialty> getAllSpecialties() {
        return specialtyRepository.findAll();
    }

    @Override
    public List<Specialty> getActiveSpecialties() {
        return specialtyRepository.findByIsActive(true);
    }

    @Override
    @Transactional
    public Specialty updateSpecialty(Long id, String name, String description) {
        Specialty specialty = getSpecialtyById(id);
        if (name != null && !name.equals(specialty.getName())) {
            if (specialtyRepository.existsByName(name)) {
                throw DuplicateResourceException.of("Specialty", "name", name);
            }
            specialty.setName(name);
        }
        if (description != null) specialty.setDescription(description);
        return specialtyRepository.save(specialty);
    }

    @Override
    @Transactional
    public Specialty setActiveStatus(Long id, boolean active) {
        Specialty specialty = getSpecialtyById(id);
        specialty.setIsActive(active);
        return specialtyRepository.save(specialty);
    }

    @Override
    @Transactional
    public void deleteSpecialty(Long id) {
        getSpecialtyById(id);
        specialtyRepository.deleteById(id);
        log.info("Deleted specialty id={}", id);
    }
}
