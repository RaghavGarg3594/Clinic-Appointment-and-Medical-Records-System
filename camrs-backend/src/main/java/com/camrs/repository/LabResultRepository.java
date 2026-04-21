package com.camrs.repository;

import com.camrs.entity.LabResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabResultRepository extends JpaRepository<LabResult, Integer> {
    java.util.Optional<LabResult> findByLabTestOrderId(Integer orderId);
}
