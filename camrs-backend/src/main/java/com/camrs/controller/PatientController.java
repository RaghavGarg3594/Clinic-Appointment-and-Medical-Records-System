package com.camrs.controller;

import com.camrs.dto.*;
import com.camrs.service.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;
    private final BillingService billingService;
    private final ConsultationService consultationService;
    private final LabService labService;
    private final PdfGenerationService pdfGenerationService;

    public PatientController(PatientService patientService, BillingService billingService,
                             ConsultationService consultationService, LabService labService,
                             PdfGenerationService pdfGenerationService) {
        this.patientService = patientService;
        this.billingService = billingService;
        this.consultationService = consultationService;
        this.labService = labService;
        this.pdfGenerationService = pdfGenerationService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(patientService.getProfile(email));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> updates) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(patientService.updateProfile(email, updates));
    }

    @GetMapping("/me/medical-records")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<MedicalRecordResponse>> getMedicalRecords() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(consultationService.getPatientRecords(email));
    }

    @GetMapping("/me/bills")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<BillResponse>> getMyBills() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(billingService.getPatientBills(email));
    }

    // Patient pay endpoint removed — only admin can pay bills

    @GetMapping("/me/lab-results")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<LabOrderResponse>> getMyLabResults() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(labService.getPatientLabResults(email));
    }

    @GetMapping("/me/prescriptions/{recordId}/pdf")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<byte[]> downloadPrescriptionPdf(@PathVariable Integer recordId) {
        try {
            byte[] pdf = pdfGenerationService.generatePrescriptionPdf(recordId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=prescription-" + recordId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/me/lab-results/{orderId}/pdf")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<byte[]> downloadLabReportPdf(@PathVariable Integer orderId) {
        try {
            byte[] pdf = pdfGenerationService.generateLabReportPdf(orderId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=lab-report-" + orderId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/me/appointments/{appointmentId}/lab-report/pdf")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<byte[]> downloadLabReportPdfByAppointment(@PathVariable Integer appointmentId) {
        try {
            byte[] pdf = pdfGenerationService.generateLabReportPdfByAppointment(appointmentId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=lab-report-appt-" + appointmentId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
