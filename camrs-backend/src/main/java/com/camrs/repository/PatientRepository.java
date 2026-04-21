package com.camrs.repository;

import com.camrs.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {
    java.util.Optional<Patient> findByUserId(Integer userId);
}
