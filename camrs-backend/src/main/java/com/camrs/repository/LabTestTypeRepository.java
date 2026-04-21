package com.camrs.repository;

import com.camrs.entity.LabTestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabTestTypeRepository extends JpaRepository<LabTestType, Integer> {
    List<LabTestType> findByIsActiveTrue();
}
