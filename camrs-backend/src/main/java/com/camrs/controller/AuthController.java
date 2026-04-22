package com.camrs.controller;

import com.camrs.dto.*;
import com.camrs.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<PatientResponse> registerPatient(@RequestBody PatientRegistrationRequest request) {
        return ResponseEntity.ok(authService.registerPatient(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/doctor-request")
    public ResponseEntity<DoctorJoinRequestResponse> submitDoctorRequest(
            @RequestBody DoctorJoinRequestDTO request) {
        return ResponseEntity.ok(authService.submitDoctorJoinRequest(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<java.util.Map<String, String>> forgotPassword(@RequestBody java.util.Map<String, String> body) {
        try {
            String firstName = body.get("firstName");
            String lastName = body.get("lastName");
            String dobStr = body.get("dateOfBirth");
            String newPassword = body.get("newPassword");
            if (firstName == null || lastName == null || dobStr == null || newPassword == null) {
                return ResponseEntity.badRequest().body(java.util.Map.of("message", "First name, last name, date of birth, and new password are required."));
            }
            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(java.util.Map.of("message", "Password must be at least 6 characters."));
            }
            java.time.LocalDate dob = java.time.LocalDate.parse(dobStr);
            String msg = authService.resetPasswordByNameAndDob(firstName, lastName, dob, newPassword);
            return ResponseEntity.ok(java.util.Map.of("message", msg));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }
}
