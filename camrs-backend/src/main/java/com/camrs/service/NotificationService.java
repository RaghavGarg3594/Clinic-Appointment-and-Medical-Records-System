package com.camrs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendAppointmentConfirmation(String patientEmail, String doctorName, String date, String time, String token) {
        log.info("[NOTIFICATION] Appointment Confirmation → {} | Doctor: {} | Date: {} | Time: {} | Token: {}", 
                patientEmail, doctorName, date, time, token);
    }

    public void sendAppointmentCancellation(String patientEmail, String doctorName, String date, String reason) {
        log.info("[NOTIFICATION] Appointment Cancelled → {} | Doctor: {} | Date: {} | Reason: {}", 
                patientEmail, doctorName, date, reason);
    }

    public void sendAppointmentReminder(String patientEmail, String doctorName, String date, String time) {
        log.info("[NOTIFICATION] Appointment Reminder → {} | Doctor: {} | Date: {} | Time: {}", 
                patientEmail, doctorName, date, time);
    }

    public void sendLabResultReady(String patientEmail, String testType, boolean isCritical) {
        if (isCritical) {
            log.warn("[CRITICAL ALERT] Lab Result Critical → {} | Test: {}", patientEmail, testType);
        } else {
            log.info("[NOTIFICATION] Lab Result Ready → {} | Test: {}", patientEmail, testType);
        }
    }

    public void sendBillGenerated(String patientEmail, String invoiceNumber, String totalAmount) {
        log.info("[NOTIFICATION] Bill Generated → {} | Invoice: {} | Total: {}", 
                patientEmail, invoiceNumber, totalAmount);
    }

    public void sendPaymentReceipt(String patientEmail, String invoiceNumber, String amountPaid, String method) {
        log.info("[NOTIFICATION] Payment Receipt → {} | Invoice: {} | Amount: {} | Method: {}", 
                patientEmail, invoiceNumber, amountPaid, method);
    }

    public void sendLabOrderPlaced(String patientEmail, String testType) {
        log.info("[NOTIFICATION] Lab Order Placed → {} | Test: {}", 
                patientEmail, testType);
    }
}
