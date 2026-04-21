package com.camrs.service;

import com.camrs.dto.*;
import com.camrs.entity.*;
import com.camrs.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final MedicationRepository medicationRepository;
    private final DoctorJoinRequestRepository doctorJoinRequestRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public AdminService(UserRepository userRepository, DoctorRepository doctorRepository,
                        DoctorScheduleRepository doctorScheduleRepository,
                        MedicationRepository medicationRepository,
                        DoctorJoinRequestRepository doctorJoinRequestRepository,
                        StaffRepository staffRepository,
                        PasswordEncoder passwordEncoder, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.medicationRepository = medicationRepository;
        this.doctorJoinRequestRepository = doctorJoinRequestRepository;
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public DoctorResponse addDoctor(DoctorRegistrationRequest request) {
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters long");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.DOCTOR);
        user.setIsActive(true);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user = userRepository.save(user);

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setQualification(request.getQualification());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setPhone(request.getPhone());
        doctor.setEmail(request.getEmail());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setIsActive(true);
        doctor = doctorRepository.save(doctor);

        auditLogService.logAction(null, "ADD_DOCTOR", "Doctor", doctor.getId(), "API");

        return mapToDoctorResponse(doctor);
    }

    @Transactional
    public DoctorResponse updateDoctor(Integer doctorId, DoctorRegistrationRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Update doctor profile fields
        if (request.getFirstName() != null) doctor.setFirstName(request.getFirstName());
        if (request.getLastName() != null) doctor.setLastName(request.getLastName());
        if (request.getSpecialization() != null) doctor.setSpecialization(request.getSpecialization());
        if (request.getQualification() != null) doctor.setQualification(request.getQualification());
        if (request.getLicenseNumber() != null) doctor.setLicenseNumber(request.getLicenseNumber());
        if (request.getPhone() != null) doctor.setPhone(request.getPhone());
        if (request.getConsultationFee() != null) doctor.setConsultationFee(request.getConsultationFee());

        // Update email on both doctor and user if changed
        Integer currentUserId = doctor.getUser().getId();
        if (request.getEmail() != null && !request.getEmail().equals(doctor.getEmail())) {
            // Check if new email is already taken by another user
            userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(currentUserId)) {
                    throw new RuntimeException("Email already in use by another account");
                }
            });
            doctor.setEmail(request.getEmail());
            User user = doctor.getUser();
            user.setEmail(request.getEmail());
            user.setUsername(request.getEmail());
            userRepository.save(user);
        }

        // Only update password if provided (non-empty)
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            if (request.getPassword().length() < 8) {
                throw new RuntimeException("Password must be at least 8 characters long");
            }
            User user = doctor.getUser();
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userRepository.save(user);
        }

        Doctor savedDoctor = doctorRepository.save(doctor);

        auditLogService.logAction(null, "UPDATE_DOCTOR", "Doctor", savedDoctor.getId(), "API");

        return mapToDoctorResponse(savedDoctor);
    }

    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll().stream()
            .map(this::mapToDoctorResponse)
            .collect(Collectors.toList());
    }

    public List<DoctorResponse> getActiveDoctors() {
        return doctorRepository.findAll().stream()
            .filter(doc -> Boolean.TRUE.equals(doc.getIsActive()))
            .map(this::mapToDoctorResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public DoctorResponse toggleDoctorActive(Integer doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        boolean newState = !Boolean.TRUE.equals(doctor.getIsActive());
        doctor.setIsActive(newState);
        // Also lock/unlock the linked user account
        User user = doctor.getUser();
        user.setIsActive(newState);
        userRepository.save(user);
        doctor = doctorRepository.save(doctor);

        auditLogService.logAction(null, "TOGGLE_DOCTOR_STATUS", "Doctor", doctor.getId(), "API");

        return mapToDoctorResponse(doctor);
    }

    @Transactional
    public void updateDoctorSchedule(Integer doctorId, DoctorScheduleRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        DoctorSchedule schedule = doctor.getSchedule();
        if (schedule == null) {
            schedule = new DoctorSchedule();
            schedule.setDoctor(doctor);
        }
        schedule.setWorkingDays(request.getWorkingDays());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setLeaveDate(request.getLeaveDate());
        doctorScheduleRepository.save(schedule);

        auditLogService.logAction(null, "UPDATE_DOCTOR_SCHEDULE", "DoctorSchedule", schedule.getId(), "API");
    }

    public MedicationResponse addMedication(MedicationRequest request) {
        Medication med = new Medication();
        med.setName(request.getName());
        med.setCategory(request.getCategory());
        med.setStockQuantity(request.getStockQuantity());
        med.setReorderLevel(request.getReorderLevel());
        med.setExpiryDate(request.getExpiryDate());
        med.setPrice(request.getPrice());
        med = medicationRepository.save(med);
        return mapToMedicationResponse(med);
    }

    public MedicationResponse updateMedication(Integer id, MedicationRequest request) {
        Medication med = medicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medication not found"));
        med.setName(request.getName());
        med.setCategory(request.getCategory());
        med.setStockQuantity(request.getStockQuantity());
        med.setReorderLevel(request.getReorderLevel());
        med.setExpiryDate(request.getExpiryDate());
        med.setPrice(request.getPrice());
        med = medicationRepository.save(med);
        return mapToMedicationResponse(med);
    }

    public List<MedicationResponse> getAllMedications() {
        return medicationRepository.findAll().stream()
                .map(this::mapToMedicationResponse)
                .collect(Collectors.toList());
    }

    private MedicationResponse mapToMedicationResponse(Medication med) {
        return new MedicationResponse(med.getId(), med.getName(), med.getCategory(),
                med.getStockQuantity(), med.getReorderLevel(), med.getExpiryDate(), med.getPrice());
    }

    private DoctorResponse mapToDoctorResponse(Doctor doc) {
        return new DoctorResponse(doc.getId(), doc.getUser().getId(), doc.getFirstName(),
                doc.getLastName(), doc.getSpecialization(), doc.getEmail(),
                doc.getPhone(), doc.getConsultationFee(), doc.getWorkingHours(),
                doc.getIsActive(), doc.getQualification(), doc.getLicenseNumber());
    }

    // --- Doctor Join Requests ---
    public List<DoctorJoinRequestResponse> getAllDoctorJoinRequests() {
        return doctorJoinRequestRepository.findAllByOrderBySubmittedAtDesc()
                .stream()
                .map(DoctorJoinRequestResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public DoctorJoinRequestResponse updateDoctorJoinRequest(Integer id, DoctorJoinStatusUpdate update) {
        DoctorJoinRequest req = doctorJoinRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        req.setStatus(DoctorJoinRequest.RequestStatus.valueOf(update.getStatus()));
        if (update.getAdminNotes() != null) {
            req.setAdminNotes(update.getAdminNotes());
        }
        req = doctorJoinRequestRepository.save(req);

        // When APPROVED and credentials provided, create doctor user + profile
        if ("APPROVED".equals(update.getStatus()) && update.getUsername() != null && update.getPassword() != null) {
            if (update.getPassword().length() < 8) {
                throw new RuntimeException("Password must be at least 8 characters long");
            }
            if (userRepository.findByEmail(update.getUsername()).isPresent()) {
                throw new RuntimeException("Email/username already exists");
            }

            // Create User
            User user = new User();
            user.setEmail(update.getUsername());
            user.setUsername(update.getUsername());
            user.setPassword(passwordEncoder.encode(update.getPassword()));
            user.setRole(User.Role.DOCTOR);
            user.setIsActive(true);
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);
            user = userRepository.save(user);

            // Create Doctor profile from join request data
            Doctor doctor = new Doctor();
            doctor.setUser(user);
            doctor.setFirstName(req.getFirstName());
            doctor.setLastName(req.getLastName());
            doctor.setSpecialization(req.getSpecialization());
            doctor.setQualification(req.getQualification());
            doctor.setLicenseNumber(req.getLicenseNumber());
            doctor.setPhone(req.getPhone());
            doctor.setEmail(update.getUsername());
            doctor.setConsultationFee(new java.math.BigDecimal("500"));
            doctor.setIsActive(true);
            doctor = doctorRepository.save(doctor);

            // Create default schedule: Mon-Fri, 9:00-18:00
            DoctorSchedule schedule = new DoctorSchedule();
            schedule.setDoctor(doctor);
            schedule.setWorkingDays("Mon,Tue,Wed,Thu,Fri");
            schedule.setStartTime(java.time.LocalTime.of(9, 0));
            schedule.setEndTime(java.time.LocalTime.of(18, 0));
            doctorScheduleRepository.save(schedule);

            auditLogService.logAction(null, "APPROVE_DOCTOR_JOIN_REQUEST", "Doctor", doctor.getId(), "API");
        }

        auditLogService.logAction(null, "UPDATE_DOCTOR_JOIN_REQUEST", "DoctorJoinRequest", req.getId(), "API");

        return new DoctorJoinRequestResponse(req);
    }

    public long getPendingDoctorRequestsCount() {
        return doctorJoinRequestRepository.findAll().stream()
                .filter(req -> req.getStatus() == DoctorJoinRequest.RequestStatus.PENDING)
                .count();
    }

    // --- Lab Staff Management ---
    public List<java.util.Map<String, Object>> getAllLabStaff() {
        return staffRepository.findByStaffType(Staff.StaffType.LAB).stream()
                .map(s -> {
                    java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
                    map.put("id", s.getId());
                    map.put("firstName", s.getFirstName());
                    map.put("lastName", s.getLastName());
                    map.put("department", s.getDepartment());
                    map.put("email", s.getUser().getEmail());
                    map.put("isActive", s.getIsActive());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public java.util.Map<String, Object> addLabStaff(java.util.Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        String firstName = request.get("firstName");
        String lastName = request.get("lastName");

        if (email == null || email.trim().isEmpty()) throw new RuntimeException("Email is required");
        if (password == null || password.length() < 8) throw new RuntimeException("Password must be at least 8 characters");
        if (firstName == null || firstName.trim().isEmpty()) throw new RuntimeException("First name is required");
        if (lastName == null || lastName.trim().isEmpty()) throw new RuntimeException("Last name is required");
        if (userRepository.findByEmail(email).isPresent()) throw new RuntimeException("Email already exists");

        User user = new User();
        user.setEmail(email);
        user.setUsername(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(User.Role.LAB_STAFF);
        user.setIsActive(true);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user = userRepository.save(user);

        Staff staff = new Staff();
        staff.setUser(user);
        staff.setFirstName(firstName);
        staff.setLastName(lastName);
        staff.setDepartment("Laboratory");
        staff.setStaffType(Staff.StaffType.LAB);
        staff.setIsActive(true);
        staff = staffRepository.save(staff);

        auditLogService.logAction(null, "ADD_LAB_STAFF", "Staff", staff.getId(), "API");

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("id", staff.getId());
        result.put("firstName", staff.getFirstName());
        result.put("lastName", staff.getLastName());
        result.put("email", email);
        result.put("department", "Laboratory");
        result.put("isActive", true);
        return result;
    }
}
