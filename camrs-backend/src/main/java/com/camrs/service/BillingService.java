package com.camrs.service;

import com.camrs.dto.*;
import com.camrs.entity.*;
import com.camrs.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillingService {

    private final BillRepository billRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    public BillingService(BillRepository billRepository, PatientRepository patientRepository,
                          UserRepository userRepository, AppointmentRepository appointmentRepository) {
        this.billRepository = billRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<BillResponse> getPatientBills(String patientEmail) {
        User user = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        return billRepository.findByPatientId(patient.getId())
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BillResponse> getAllBills() {
        return billRepository.findAllByOrderByIssueDateDesc()
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BillResponse markPaid(Integer billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        
        bill.setPaidAmount(bill.getTotalAmount());
        bill.setStatus(Bill.BillStatus.PAID);
        bill = billRepository.save(bill);

        // Transition appointment from APPROVAL_PENDING to ONGOING when bill is paid
        if (bill.getAppointment() != null) {
            Appointment appt = bill.getAppointment();
            if (appt.getStatus() == Appointment.AppointmentStatus.APPROVAL_PENDING) {
                appt.setStatus(Appointment.AppointmentStatus.ONGOING);
                appointmentRepository.save(appt);
            }
        }

        return mapToResponse(bill);
    }

    @Transactional
    public BillResponse applyDiscount(Integer billId, BigDecimal discountPercentage) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        
        if (bill.getStatus() == Bill.BillStatus.PAID) {
            throw new RuntimeException("Cannot apply discount to a paid bill");
        }

        // Subtotal + Tax is the pre-discount total
        BigDecimal consultation = bill.getConsultationCharge() != null ? bill.getConsultationCharge() : BigDecimal.ZERO;
        BigDecimal meds = bill.getMedicationCharge() != null ? bill.getMedicationCharge() : BigDecimal.ZERO;
        BigDecimal lab = bill.getLabCharge() != null ? bill.getLabCharge() : BigDecimal.ZERO;
        BigDecimal tax = bill.getTax() != null ? bill.getTax() : BigDecimal.ZERO;
        
        BigDecimal grossTotal = consultation.add(meds).add(lab).add(tax);
        
        // Calculate discount amount
        BigDecimal discountAmount = grossTotal.multiply(discountPercentage).divide(new BigDecimal("100"));
        
        bill.setDiscount(discountAmount);
        bill.setTotalAmount(grossTotal.subtract(discountAmount));
        
        bill = billRepository.save(bill);
        return mapToResponse(bill);
    }

    private BillResponse mapToResponse(Bill bill) {
        BillResponse resp = new BillResponse();
        resp.setId(bill.getId());
        resp.setAppointmentId(bill.getAppointment() != null ? bill.getAppointment().getId() : null);
        resp.setPatientName(bill.getPatient() != null ? bill.getPatient().getFirstName() + " " + bill.getPatient().getLastName() : "Unknown");
        resp.setInvoiceNumber(bill.getInvoiceNumber());
        resp.setIssueDate(bill.getIssueDate());
        resp.setConsultationCharge(bill.getConsultationCharge());
        resp.setLabCharge(bill.getLabCharge());
        resp.setMedicationCharge(bill.getMedicationCharge());
        resp.setTax(bill.getTax());
        resp.setDiscount(bill.getDiscount());
        resp.setTotalAmount(bill.getTotalAmount());
        resp.setStatus(bill.getStatus().name());
        resp.setPaidAmount(bill.getPaidAmount());
        BigDecimal total = bill.getTotalAmount() != null ? bill.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal paid = bill.getPaidAmount() != null ? bill.getPaidAmount() : BigDecimal.ZERO;
        resp.setDueAmount(total.subtract(paid).max(BigDecimal.ZERO));

        boolean hasLabReport = false;
        boolean hasPendingLabTests = false;
        
        if (bill.getAppointment() != null && bill.getAppointment().getMedicalRecord() != null) {
            java.util.List<LabTestOrder> labs = bill.getAppointment().getMedicalRecord().getLabTestOrders();
            if (labs != null && !labs.isEmpty()) {
                hasLabReport = labs.stream().anyMatch(l -> l.getStatus() == LabTestOrder.TestStatus.COMPLETED && l.getLabResult() != null);
                hasPendingLabTests = labs.stream().anyMatch(l -> l.getStatus() != LabTestOrder.TestStatus.COMPLETED && l.getStatus() != LabTestOrder.TestStatus.CANCELLED);
            }
        }
        resp.setHasLabReport(hasLabReport);
        resp.setHasPendingLabTests(hasPendingLabTests);

        return resp;
    }
}
