package com.camrs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.camrs.dto.*;
import com.camrs.entity.Icd10Code;
import com.camrs.repository.Icd10CodeRepository;
import com.camrs.service.ConsultationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    private final ConsultationService consultationService;
    private final com.camrs.service.AdminService adminService;
    private final Icd10CodeRepository icd10CodeRepository;

    public ConsultationController(ConsultationService consultationService, com.camrs.service.AdminService adminService,
                                  Icd10CodeRepository icd10CodeRepository) {
        this.consultationService = consultationService;
        this.adminService = adminService;
        this.icd10CodeRepository = icd10CodeRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalRecordResponse> recordConsultation(@RequestBody ConsultationRecordRequest req) {
        String doctorEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(consultationService.recordConsultation(doctorEmail, req));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    public ResponseEntity<MedicalRecordResponse> getRecord(@PathVariable Integer id) {
        return ResponseEntity.ok(consultationService.getRecordById(id));
    }

    @GetMapping("/medications")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<java.util.List<com.camrs.dto.MedicationResponse>> getMedications() {
        return ResponseEntity.ok(adminService.getAllMedications());
    }

    @GetMapping("/patient/{patientId}/records")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<MedicalRecordResponse>> getRecordsByPatientId(@PathVariable Integer patientId) {
        return ResponseEntity.ok(consultationService.getRecordsByPatientId(patientId));
    }

    @GetMapping("/patient/{patientId}/last-prescription")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalRecordResponse> getLastPrescription(@PathVariable Integer patientId) {
        MedicalRecordResponse resp = consultationService.getLastPrescriptionForPatient(patientId);
        if (resp == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/icd10")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<Icd10Code>> searchIcd10(@RequestParam(required = false, defaultValue = "") String q) {
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.ok(icd10CodeRepository.findAll());
        }
        return ResponseEntity.ok(icd10CodeRepository.findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(q, q));
    }

    @PostMapping("/check-allergies")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<String>> checkAllergies(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(consultationService.checkAllergies(body));
    }
}
