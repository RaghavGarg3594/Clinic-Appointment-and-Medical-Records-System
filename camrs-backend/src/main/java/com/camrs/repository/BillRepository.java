package com.camrs.repository;

import com.camrs.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillRepository extends JpaRepository<Bill, Integer> {
    java.util.List<Bill> findByPatientId(Integer patientId);
    java.util.List<Bill> findAllByOrderByIssueDateDesc();
    java.util.Optional<Bill> findByAppointmentId(Integer appointmentId);
}
