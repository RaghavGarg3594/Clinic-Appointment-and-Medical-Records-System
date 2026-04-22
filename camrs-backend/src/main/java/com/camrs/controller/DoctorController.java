package com.camrs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.camrs.dto.*;
import com.camrs.service.AdminService;
import com.camrs.service.ConsultationService;
import com.camrs.service.PdfGenerationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final AdminService adminService;
    private final PdfGenerationService pdfGenerationService;
    private final ConsultationService consultationService;
    private final com.camrs.service.LabService labService;

    public DoctorController(AdminService adminService, PdfGenerationService pdfGenerationService,
                            ConsultationService consultationService, com.camrs.service.LabService labService) {
        this.adminService = adminService;
        this.pdfGenerationService = pdfGenerationService;
        this.consultationService = consultationService;
        this.labService = labService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN_STAFF')")
    public ResponseEntity<List<DoctorResponse>> listAllDoctors() { 
        // Admin sees all doctors; patients see only active ones
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN_STAFF"));
        if (isAdmin) {
            return ResponseEntity.ok(adminService.getAllDoctors());
        }
        return ResponseEntity.ok(adminService.getActiveDoctors());
    }

    @GetMapping("/me/patient-records")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<MedicalRecordResponse>> getMyPatientRecords() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(consultationService.getDoctorRecords(email));
    }

    @GetMapping("/me/lab-results")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<LabOrderResponse>> getMyLabResults() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(labService.getDoctorLabResults(email));
    }

    @GetMapping("/prescriptions/{recordId}/pdf")
    @PreAuthorize("hasRole('DOCTOR')")
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

    @GetMapping("/appointments/{apptId}/prescription/pdf")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<byte[]> downloadPrescriptionPdfByAppt(@PathVariable Integer apptId) {
        try {
            byte[] pdf = pdfGenerationService.generatePrescriptionPdfByAppointment(apptId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=prescription-" + apptId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/lab-results/{orderId}/pdf")
    @PreAuthorize("hasRole('DOCTOR')")
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

    @GetMapping("/appointments/{apptId}/lab-report/pdf")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<byte[]> downloadLabReportPdfByAppt(@PathVariable Integer apptId) {
        try {
            byte[] pdf = pdfGenerationService.generateLabReportPdfByAppointment(apptId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=lab-report-" + apptId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
