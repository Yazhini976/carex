package com.carex.service;

import com.carex.entity.Specialty;

import java.util.List;

public interface SpecialtyService {
    Specialty createSpecialty(String name, String description);
    Specialty getSpecialtyById(Long id);
    List<Specialty> getAllSpecialties();
    List<Specialty> getActiveSpecialties();
    Specialty updateSpecialty(Long id, String name, String description);
    Specialty setActiveStatus(Long id, boolean active);
    void deleteSpecialty(Long id);
}
