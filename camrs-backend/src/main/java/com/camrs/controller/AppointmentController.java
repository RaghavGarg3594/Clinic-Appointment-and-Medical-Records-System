package com.camrs.controller;

import com.camrs.dto.*;
import com.camrs.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/book")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> bookAppointment(@RequestBody AppointmentBookingRequest request) {
        String patientEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(appointmentService.bookAppointment(patientEmail, request));
    }

    @GetMapping("/patient")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentResponse>> getMyPatientAppointments() {
        String patientEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(appointmentService.getPatientAppointments(patientEmail));
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentResponse>> getMyDoctorAppointments(
            @RequestParam(required = false) String date) {
        String doctorEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        LocalDate searchDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(doctorEmail, searchDate));
    }

    @GetMapping("/doctor/upcoming")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentResponse>> getDoctorUpcomingAppointments() {
        String doctorEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(appointmentService.getDoctorUpcomingAppointments(doctorEmail));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN_STAFF')")
    public ResponseEntity<AppointmentResponse> updateStatus(@PathVariable Integer id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, body.get("status")));
    }

    @GetMapping("/slots")
    public ResponseEntity<List<String>> getAvailableSlots(@RequestParam Integer doctorId, @RequestParam String date) {
        List<java.time.LocalTime> times = appointmentService.getAvailableSlots(doctorId, java.time.LocalDate.parse(date));
        List<String> stringSlots = times.stream().map(java.time.LocalTime::toString).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(stringSlots);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN_STAFF')")
    public ResponseEntity<AppointmentResponse> cancelAppointment(@PathVariable Integer id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity
                .ok(appointmentService.cancelAppointment(id, body.getOrDefault("reason", "No reason provided")));
    }

    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN_STAFF')")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(@PathVariable Integer id,
            @RequestBody Map<String, String> body) {
        LocalDate newDate = LocalDate.parse(body.get("appointmentDate"));
        String newTimeSlot = body.get("timeSlot");
        return ResponseEntity.ok(appointmentService.rescheduleAppointment(id, newDate, newTimeSlot));
    }
}
