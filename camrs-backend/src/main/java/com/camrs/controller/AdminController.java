package com.camrs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.camrs.dto.*;
import com.camrs.service.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN_STAFF')")
public class AdminController {

    private final AdminService adminService;
    private final BillingService billingService;
    private final ReportGenerationService reportGenerationService;
    private final PdfGenerationService pdfGenerationService;
    private final AuditLogService auditLogService;

    public AdminController(AdminService adminService, BillingService billingService,
                           ReportGenerationService reportGenerationService,
                           PdfGenerationService pdfGenerationService,
                           AuditLogService auditLogService) {
        this.adminService = adminService;
        this.billingService = billingService;
        this.reportGenerationService = reportGenerationService;
        this.pdfGenerationService = pdfGenerationService;
        this.auditLogService = auditLogService;
    }

    // --- Doctor Management ---
    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {
        return ResponseEntity.ok(adminService.getAllDoctors());
    }

    @PostMapping("/doctors")
    public ResponseEntity<DoctorResponse> addDoctor(@RequestBody DoctorRegistrationRequest request) {
        return ResponseEntity.ok(adminService.addDoctor(request));
    }

    @PutMapping("/doctors/{id}")
    public ResponseEntity<DoctorResponse> updateDoctor(@PathVariable Integer id, @RequestBody DoctorRegistrationRequest request) {
        return ResponseEntity.ok(adminService.updateDoctor(id, request));
    }

    @PutMapping("/doctors/{id}/schedule")
    public ResponseEntity<Void> updateDoctorSchedule(@PathVariable Integer id, @RequestBody DoctorScheduleRequest request) {
        adminService.updateDoctorSchedule(id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/doctors/{id}/toggle-active")
    public ResponseEntity<DoctorResponse> toggleDoctorActive(@PathVariable Integer id) {
        return ResponseEntity.ok(adminService.toggleDoctorActive(id));
    }

    // --- Doctor Join Requests ---
    @GetMapping("/doctor-requests")
    public ResponseEntity<List<DoctorJoinRequestResponse>> getDoctorJoinRequests() {
        return ResponseEntity.ok(adminService.getAllDoctorJoinRequests());
    }

    @GetMapping("/doctor-requests/pending-count")
    public ResponseEntity<Long> getPendingDoctorRequestsCount() {
        return ResponseEntity.ok(adminService.getPendingDoctorRequestsCount());
    }

    @PutMapping("/doctor-requests/{id}")
    public ResponseEntity<DoctorJoinRequestResponse> updateDoctorJoinRequest(
            @PathVariable Integer id,
            @RequestBody DoctorJoinStatusUpdate update) {
        return ResponseEntity.ok(adminService.updateDoctorJoinRequest(id, update));
    }

    // --- Inventory Management ---
    @GetMapping("/inventory")
    public ResponseEntity<List<MedicationResponse>> viewInventory() {
        return ResponseEntity.ok(adminService.getAllMedications());
    }

    @PostMapping("/inventory")
    public ResponseEntity<MedicationResponse> addInventory(@RequestBody MedicationRequest request) {
        return ResponseEntity.ok(adminService.addMedication(request));
    }

    @PutMapping("/inventory/{id}")
    public ResponseEntity<MedicationResponse> updateInventory(@PathVariable Integer id, @RequestBody MedicationRequest request) {
        return ResponseEntity.ok(adminService.updateMedication(id, request));
    }

    // --- Billing ---
    @GetMapping("/bills")
    public ResponseEntity<List<BillResponse>> manageAllBills() {
        return ResponseEntity.ok(billingService.getAllBills());
    }

    @PutMapping("/bills/{id}/mark-paid")
    public ResponseEntity<BillResponse> markPaid(@PathVariable Integer id) {
        return ResponseEntity.ok(billingService.markPaid(id));
    }

    @PutMapping("/bills/{id}/discount")
    public ResponseEntity<BillResponse> applyDiscount(@PathVariable Integer id, @RequestBody java.util.Map<String, java.math.BigDecimal> request) {
        return ResponseEntity.ok(billingService.applyDiscount(id, request.get("discountPercentage")));
    }

    // --- Dashboard ---
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(reportGenerationService.getDashboardStats());
    }

    // --- Reports ---
    @GetMapping("/reports/consultation-stats")
    public ResponseEntity<List<Map<String, Object>>> getConsultationStats() {
        return ResponseEntity.ok(reportGenerationService.getConsultationStats());
    }

    @GetMapping("/reports/disease-stats")
    public ResponseEntity<List<Map<String, Object>>> getDiseaseStats() {
        return ResponseEntity.ok(reportGenerationService.getDiseaseStats());
    }

    @GetMapping("/reports/outstanding-payments")
    public ResponseEntity<List<Map<String, Object>>> getOutstandingPayments() {
        return ResponseEntity.ok(reportGenerationService.getOutstandingPayments());
    }

    @GetMapping("/prescriptions/{recordId}/pdf")
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

    // --- Lab Staff Management ---
    @GetMapping("/lab-staff")
    public ResponseEntity<List<Map<String, Object>>> getAllLabStaff() {
        return ResponseEntity.ok(adminService.getAllLabStaff());
    }

    @PostMapping("/lab-staff")
    public ResponseEntity<Map<String, Object>> addLabStaff(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(adminService.addLabStaff(request));
    }

    // --- Audit Logs ---
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs() {
        return ResponseEntity.ok(auditLogService.getAllAuditLogs());
    }
}
