package com.camrs.repository;

import com.camrs.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Integer> {
    java.util.Optional<DoctorSchedule> findByDoctorId(Integer doctorId);
}
