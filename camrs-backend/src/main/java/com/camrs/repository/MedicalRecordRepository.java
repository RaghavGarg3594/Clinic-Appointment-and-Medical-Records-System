package com.camrs.repository;
import com.camrs.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Integer> {
    java.util.List<MedicalRecord> findByPatientIdOrderByVisitDateDesc(Integer patientId);
    Optional<MedicalRecord> findByAppointmentId(Integer appointmentId);
    java.util.List<MedicalRecord> findByDoctorIdOrderByVisitDateDesc(Integer doctorId);
}
