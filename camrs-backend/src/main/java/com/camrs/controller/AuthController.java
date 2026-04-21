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
}
