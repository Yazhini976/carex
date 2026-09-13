package com.carex.repository;

import com.carex.entity.DoctorSpecialty;
import com.carex.entity.DoctorSpecialtyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorSpecialtyRepository extends JpaRepository<DoctorSpecialty, DoctorSpecialtyId> {
    List<DoctorSpecialty> findByDoctorId(Long doctorId);
    List<DoctorSpecialty> findBySpecialtyId(Long specialtyId);
    boolean existsByDoctorIdAndSpecialtyId(Long doctorId, Long specialtyId);

    @Query("SELECT ds FROM DoctorSpecialty ds WHERE ds.doctor.id = :doctorId AND ds.specialty.id = :specialtyId")
    java.util.Optional<DoctorSpecialty> findByDoctorIdAndSpecialtyId(
            @Param("doctorId") Long doctorId, @Param("specialtyId") Long specialtyId);
}
