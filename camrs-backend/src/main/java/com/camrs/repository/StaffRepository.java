package com.camrs.repository;

import com.camrs.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {
    java.util.Optional<Staff> findByUserId(Integer userId);
    java.util.List<Staff> findByStaffType(Staff.StaffType staffType);
}
