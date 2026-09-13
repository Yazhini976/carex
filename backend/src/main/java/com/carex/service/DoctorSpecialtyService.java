package com.carex.service;

import com.carex.entity.DoctorSpecialty;
import com.carex.entity.Specialty;

import java.util.List;

public interface DoctorSpecialtyService {
    DoctorSpecialty assignSpecialty(Long doctorId, Long specialtyId);
    void removeSpecialty(Long doctorId, Long specialtyId);
    List<Specialty> getSpecialtiesForDoctor(Long doctorId);
    List<com.carex.entity.Doctor> getDoctorsForSpecialty(Long specialtyId);
    boolean isAssigned(Long doctorId, Long specialtyId);
}
