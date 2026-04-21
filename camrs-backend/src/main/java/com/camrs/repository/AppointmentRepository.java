package com.camrs.repository;

import com.camrs.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findByDoctorIdAndAppointmentDate(Integer doctorId, LocalDate date);
    List<Appointment> findByPatientIdOrderByAppointmentDateDescTimeSlotDesc(Integer patientId);
    List<Appointment> findByDoctorIdAndAppointmentDateGreaterThanEqualOrderByAppointmentDateAscTimeSlotAsc(Integer doctorId, LocalDate fromDate);
}
