package com.camrs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.camrs.dto.*;
import com.camrs.service.LabService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/lab")
public class LabController {

    private static final Logger log = LoggerFactory.getLogger(LabController.class);
    private final LabService labService;

    public LabController(LabService labService) {
        this.labService = labService;
    }

    @GetMapping("/orders/pending")
    @PreAuthorize("hasRole('LAB_STAFF')")
    public ResponseEntity<List<LabOrderResponse>> getPendingOrders() {
        try {
            return ResponseEntity.ok(labService.getPendingOrders());
        } catch (Exception e) {
            log.error("Error fetching pending lab orders", e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('LAB_STAFF')")
    public ResponseEntity<List<LabOrderResponse>> getAllOrders() {
        try {
            return ResponseEntity.ok(labService.getAllOrders());
        } catch (Exception e) {
            log.error("Error fetching all lab orders", e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @PutMapping("/orders/{id}/collect")
    @PreAuthorize("hasRole('LAB_STAFF')")
    public ResponseEntity<LabOrderResponse> markSampleCollected(@PathVariable Integer id) {
        return ResponseEntity.ok(labService.markSampleCollected(id));
    }
    
    @PostMapping("/orders/{id}/results")
    @PreAuthorize("hasRole('LAB_STAFF')")
    public ResponseEntity<LabOrderResponse> enterResults(@PathVariable Integer id, @RequestBody LabResultEntryRequest req) {
        return ResponseEntity.ok(labService.enterResults(id, req));
    }

    @GetMapping("/types")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN_STAFF')")
    public ResponseEntity<List<com.camrs.entity.LabTestType>> getTestTypes() {
        return ResponseEntity.ok(labService.getAllTestTypes());
    }

    @PostMapping("/orders")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<LabOrderResponse> orderLabTest(@RequestBody LabOrderRequest req) {
        String doctorEmail = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(labService.createLabOrder(doctorEmail, req));
    }
}
