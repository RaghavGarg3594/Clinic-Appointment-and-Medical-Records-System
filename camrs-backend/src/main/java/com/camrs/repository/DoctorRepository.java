package com.camrs.repository;

import com.camrs.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
    java.util.Optional<Doctor> findByUserId(Integer userId);
    java.util.List<Doctor> findByIsActiveTrue();
}
