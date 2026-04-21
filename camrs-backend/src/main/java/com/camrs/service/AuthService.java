package com.camrs.service;

import com.camrs.dto.*;
import com.camrs.entity.*;
import com.camrs.repository.*;
import com.camrs.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final StaffRepository staffRepository;
    private final DoctorJoinRequestRepository doctorJoinRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final AuditLogService auditLogService;

    public AuthService(UserRepository userRepository, PatientRepository patientRepository,
                       DoctorRepository doctorRepository, StaffRepository staffRepository,
                       DoctorJoinRequestRepository doctorJoinRequestRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil, UserDetailsService userDetailsService,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.staffRepository = staffRepository;
        this.doctorJoinRequestRepository = doctorJoinRequestRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public PatientResponse registerPatient(PatientRegistrationRequest request) {
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters long");
        }
        if (request.getEmail() == null || !request.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new RuntimeException("Please provide a valid email address");
        }
        if (request.getPhone() != null && !request.getPhone().isEmpty() && !request.getPhone().matches("^\\d{10}$")) {
            throw new RuntimeException("Phone number must be exactly 10 digits");
        }
        if (request.getDateOfBirth() != null && request.getDateOfBirth().isAfter(java.time.LocalDate.now())) {
            throw new RuntimeException("Date of birth must be a past date");
        }
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new RuntimeException("First name is required");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new RuntimeException("Last name is required");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.PATIENT);
        user.setIsActive(true);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user = userRepository.save(user);

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());

        if (request.getDateOfBirth() != null) {
            patient.setDateOfBirth(request.getDateOfBirth());
            patient.setAge(java.time.Period.between(request.getDateOfBirth(), java.time.LocalDate.now()).getYears());
        } else {
            patient.setDateOfBirth(java.time.LocalDate.now());
            patient.setAge(0);
        }

        if (request.getGender() != null) {
            String g = request.getGender();
            if (g.equalsIgnoreCase("male")) patient.setGender(Patient.Gender.Male);
            else if (g.equalsIgnoreCase("female")) patient.setGender(Patient.Gender.Female);
            else patient.setGender(Patient.Gender.Other);
        } else {
            patient.setGender(Patient.Gender.Other);
        }

        patient.setPhone(request.getPhone() != null ? request.getPhone() : "");
        patient.setEmail(request.getEmail());
        patient.setMedicalHistory(request.getMedicalHistory());
        patient.setAllergies(request.getAllergies());
        patient.setInsuranceDetails(request.getInsuranceDetails());
        patient.setEmergencyContact(request.getEmergencyContact());
        patient = patientRepository.save(patient);

        auditLogService.logAction(user, "REGISTER_PATIENT", "Patient", patient.getId(), "API");

        return new PatientResponse(patient.getId(), user.getId(), patient.getFirstName(), patient.getLastName());
    }

    public LoginResponse login(LoginRequest request) {
        // SE-6: Check if account is locked before attempting authentication
        User userCheck = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (userCheck != null && Boolean.TRUE.equals(userCheck.getAccountLocked())) {
            throw new RuntimeException("Account is locked due to too many failed login attempts. Please contact the administrator.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            String token = jwtUtil.generateToken(userDetails);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Reset failed attempts on successful login
            user.setLastLogin(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            user.setAccountLocked(false);
            userRepository.save(user);

            auditLogService.logAction(user, "LOGIN", "User", user.getId(), "API");

            String fullName = resolveFullName(user);
            return new LoginResponse(token, user.getRole().name(), user.getId(), fullName);

        } catch (org.springframework.security.authentication.BadCredentialsException ex) {
            // SE-6: Increment failed login attempts
            if (userCheck != null) {
                int attempts = (userCheck.getFailedLoginAttempts() != null ? userCheck.getFailedLoginAttempts() : 0) + 1;
                userCheck.setFailedLoginAttempts(attempts);
                if (attempts >= 5) {
                    userCheck.setAccountLocked(true);
                    userRepository.save(userCheck);
                    throw new RuntimeException("Account has been locked after 5 failed login attempts. Please contact the administrator.");
                }
                userRepository.save(userCheck);
                throw new RuntimeException("Invalid credentials. " + (5 - attempts) + " attempt(s) remaining before account lockout.");
            }
            throw new RuntimeException("Invalid email or password.");
        }
    }

    private String resolveFullName(User user) {
        try {
            switch (user.getRole()) {
                case PATIENT:
                    return patientRepository.findByUserId(user.getId())
                            .map(p -> p.getFirstName() + " " + p.getLastName())
                            .orElse(user.getEmail());
                case DOCTOR:
                    return doctorRepository.findByUserId(user.getId())
                            .map(d -> "Dr. " + d.getFirstName() + " " + d.getLastName())
                            .orElse(user.getEmail());
                case ADMIN_STAFF:
                case LAB_STAFF:
                    return staffRepository.findByUserId(user.getId())
                            .map(s -> s.getFirstName() + " " + s.getLastName())
                            .orElse(user.getEmail());
                default:
                    return user.getEmail();
            }
        } catch (Exception e) {
            return user.getEmail();
        }
    }

    @Transactional
    public DoctorJoinRequestResponse submitDoctorJoinRequest(DoctorJoinRequestDTO dto) {
        DoctorJoinRequest req = new DoctorJoinRequest();
        req.setFirstName(dto.getFirstName());
        req.setLastName(dto.getLastName());
        req.setEmail(dto.getEmail());
        req.setPhone(dto.getPhone());
        req.setSpecialization(dto.getSpecialization());
        req.setQualification(dto.getQualification());
        req.setLicenseNumber(dto.getLicenseNumber());
        req.setExperienceYears(dto.getExperienceYears());
        req.setMessage(dto.getMessage());
        req.setStatus(DoctorJoinRequest.RequestStatus.PENDING);
        req = doctorJoinRequestRepository.save(req);
        return new DoctorJoinRequestResponse(req);
    }
}
