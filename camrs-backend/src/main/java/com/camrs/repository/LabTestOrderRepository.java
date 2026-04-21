package com.camrs.repository;

import com.camrs.entity.LabTestOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabTestOrderRepository extends JpaRepository<LabTestOrder, Integer> {
    java.util.List<LabTestOrder> findByPatientId(Integer patientId);
    java.util.List<LabTestOrder> findByStatus(com.camrs.entity.LabTestOrder.TestStatus status);
    java.util.List<LabTestOrder> findByMedicalRecordId(Integer medicalRecordId);

    @Query("SELECT DISTINCT o FROM LabTestOrder o " +
           "LEFT JOIN FETCH o.patient " +
           "LEFT JOIN FETCH o.doctor " +
           "LEFT JOIN FETCH o.labResult")
    List<LabTestOrder> findAllWithDetails();

    @Query("SELECT DISTINCT o FROM LabTestOrder o " +
           "LEFT JOIN FETCH o.patient " +
           "LEFT JOIN FETCH o.doctor " +
           "LEFT JOIN FETCH o.labResult " +
           "WHERE o.status = :status")
    List<LabTestOrder> findByStatusWithDetails(@Param("status") LabTestOrder.TestStatus status);

    @Query("SELECT DISTINCT o FROM LabTestOrder o " +
           "LEFT JOIN FETCH o.patient " +
           "LEFT JOIN FETCH o.doctor " +
           "LEFT JOIN FETCH o.labResult " +
           "WHERE o.patient.id = :patientId")
    List<LabTestOrder> findByPatientIdWithDetails(@Param("patientId") Integer patientId);
}
