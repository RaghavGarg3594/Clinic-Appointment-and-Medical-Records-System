package com.camrs.repository;

import com.camrs.entity.Icd10Code;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Icd10CodeRepository extends JpaRepository<Icd10Code, Integer> {
    java.util.List<Icd10Code> findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String code, String desc);
}
