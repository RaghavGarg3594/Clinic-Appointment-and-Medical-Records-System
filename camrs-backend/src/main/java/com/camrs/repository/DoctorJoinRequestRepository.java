package com.camrs.repository;

import com.camrs.entity.DoctorJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DoctorJoinRequestRepository extends JpaRepository<DoctorJoinRequest, Integer> {
    List<DoctorJoinRequest> findAllByOrderBySubmittedAtDesc();
}
