package com.camrs.service;

import com.camrs.dto.*;
import com.camrs.entity.*;
import com.camrs.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LabService {

    private final LabTestOrderRepository labTestOrderRepository;
    private final LabResultRepository labResultRepository;
    private final LabTestTypeRepository labTestTypeRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final NotificationService notificationService;
    private final MedicalRecordRepository medicalRecordRepository;
    private final BillRepository billRepository;

    public LabService(LabTestOrderRepository labTestOrderRepository, LabResultRepository labResultRepository,
                      LabTestTypeRepository labTestTypeRepository, AppointmentRepository appointmentRepository,
                      DoctorRepository doctorRepository,
                      UserRepository userRepository, PatientRepository patientRepository,
                      NotificationService notificationService,
                      MedicalRecordRepository medicalRecordRepository, BillRepository billRepository) {
        this.labTestOrderRepository = labTestOrderRepository;
        this.labResultRepository = labResultRepository;
        this.labTestTypeRepository = labTestTypeRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.notificationService = notificationService;
        this.medicalRecordRepository = medicalRecordRepository;
        this.billRepository = billRepository;
    }

    @Transactional
    public LabOrderResponse createLabOrder(String doctorEmail, LabOrderRequest request) {
        User user = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
                
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointment.getId()).orElse(null);

        LabTestType testType = labTestTypeRepository.findById(request.getTestTypeId())
                .orElseThrow(() -> new RuntimeException("Lab test type not found"));

        LabTestOrder order = new LabTestOrder();
        order.setPatient(appointment.getPatient());
        order.setDoctor(doctor);
        order.setMedicalRecord(record);
        order.setTestTypeEntity(testType);
        order.setTestType(testType.getTestName());
        order.setOrderDate(LocalDateTime.now());

        if (request.getPriority() != null) {
            try { order.setPriority(LabTestOrder.TestPriority.valueOf(request.getPriority())); }
            catch (Exception e) { order.setPriority(LabTestOrder.TestPriority.ROUTINE); }
        } else {
            order.setPriority(LabTestOrder.TestPriority.ROUTINE);
        }

        order.setSpecialInstructions(request.getSpecialInstructions());
        order.setStatus(LabTestOrder.TestStatus.ORDERED);
        order.setCreatedBy(user);

        labTestOrderRepository.save(order);

        billRepository.findByAppointmentId(appointment.getId()).ifPresent(bill -> {
            java.math.BigDecimal labCharge = bill.getLabCharge() != null ? bill.getLabCharge() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal newLabCharge = labCharge.add(testType.getCost() != null ? testType.getCost() : java.math.BigDecimal.ZERO);
            bill.setLabCharge(newLabCharge);
            
            java.math.BigDecimal tax = bill.getTax() != null ? bill.getTax() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal newTax = testType.getCost() != null ? testType.getCost().multiply(new java.math.BigDecimal("0.12")) : java.math.BigDecimal.ZERO;
            bill.setTax(tax.add(newTax));
            
            java.math.BigDecimal totalAmount = bill.getTotalAmount() != null ? bill.getTotalAmount() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal newCost = testType.getCost() != null ? testType.getCost().add(newTax) : java.math.BigDecimal.ZERO;
            bill.setTotalAmount(totalAmount.add(newCost));
            
            if (bill.getStatus() == Bill.BillStatus.PAID) {
                bill.setStatus(Bill.BillStatus.UNPAID);
            }
            
            billRepository.save(bill);
        });

        notificationService.sendLabOrderPlaced(
                appointment.getPatient().getEmail(), testType.getTestName());

        return mapToResponse(order);
    }

    public List<LabTestType> getAllTestTypes() {
        return labTestTypeRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<LabOrderResponse> getPendingOrders() {
        List<LabTestOrder> all = new java.util.ArrayList<>();
        try { all.addAll(labTestOrderRepository.findByStatus(LabTestOrder.TestStatus.ORDERED)); } catch (Exception ignored) {}
        try { all.addAll(labTestOrderRepository.findByStatus(LabTestOrder.TestStatus.SAMPLE_COLLECTED)); } catch (Exception ignored) {}
        try { all.addAll(labTestOrderRepository.findByStatus(LabTestOrder.TestStatus.IN_PROGRESS)); } catch (Exception ignored) {}

        List<LabOrderResponse> responses = new java.util.ArrayList<>();
        for (LabTestOrder order : all) {
            try {
                responses.add(mapToResponse(order));
            } catch (Exception e) {
                // Skip corrupt records
            }
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<LabOrderResponse> getAllOrders() {
        List<LabTestOrder> all;
        try {
            all = labTestOrderRepository.findAll();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }

        List<LabOrderResponse> responses = new java.util.ArrayList<>();
        for (LabTestOrder order : all) {
            try {
                responses.add(mapToResponse(order));
            } catch (Exception e) {
                // Skip corrupt records - log info
                LabOrderResponse fallback = new LabOrderResponse();
                fallback.setId(order.getId());
                fallback.setPatientName("Unknown");
                fallback.setDoctorName("Unknown");
                fallback.setTestType(order.getTestType() != null ? order.getTestType() : "Unknown");
                fallback.setStatus(order.getStatus() != null ? order.getStatus().name() : "ORDERED");
                fallback.setPriority(order.getPriority() != null ? order.getPriority().name() : "ROUTINE");
                fallback.setOrderDate(order.getOrderDate());
                responses.add(fallback);
            }
        }
        return responses;
    }

    @Transactional
    public LabOrderResponse markSampleCollected(Integer orderId) {
        LabTestOrder order = labTestOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Lab order not found"));
        order.setStatus(LabTestOrder.TestStatus.SAMPLE_COLLECTED);
        return mapToResponse(labTestOrderRepository.save(order));
    }

    @Transactional
    public LabOrderResponse enterResults(Integer orderId, LabResultEntryRequest request) {
        LabTestOrder order = labTestOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Lab order not found"));

        LabResult result = order.getLabResult();
        if (result == null) {
            result = new LabResult();
            result.setLabTestOrder(order);
        }

        result.setResultValue(request.getResultValue());
        result.setUnit(request.getUnit());

        // Auto-fill reference range from test type if not provided
        String refRange = request.getReferenceRange();
        if ((refRange == null || refRange.trim().isEmpty()) && order.getTestTypeEntity() != null) {
            String rangeFromType = order.getTestTypeEntity().getNormalRangeMale();
            if (rangeFromType != null && !rangeFromType.isEmpty()) {
                refRange = rangeFromType;
            }
        }
        result.setReferenceRange(refRange);

        result.setIsCritical(request.getIsCritical() != null ? request.getIsCritical() : false);
        result.setNotes(request.getNotes());
        result.setEntryDate(LocalDateTime.now());

        labResultRepository.save(result);
        order.setLabResult(result);

        order.setStatus(LabTestOrder.TestStatus.COMPLETED);
        labTestOrderRepository.save(order);

        // Send notification (FR-LM3)
        notificationService.sendLabResultReady(
                order.getPatient().getEmail(),
                order.getTestType(),
                result.getIsCritical() != null && result.getIsCritical());

        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<LabOrderResponse> getPatientLabResults(String patientEmail) {
        User user = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        List<LabTestOrder> orders;
        try {
            orders = labTestOrderRepository.findByPatientId(patient.getId());
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }

        List<LabOrderResponse> responses = new java.util.ArrayList<>();
        for (LabTestOrder order : orders) {
            try {
                responses.add(mapToResponse(order));
            } catch (Exception e) {
                // Skip corrupt records
            }
        }
        return responses;
    }

    private LabOrderResponse mapToResponse(LabTestOrder order) {
        LabOrderResponse resp = new LabOrderResponse();
        resp.setId(order.getId());
        try {
            resp.setPatientName(order.getPatient() != null ? order.getPatient().getFirstName() + " " + order.getPatient().getLastName() : "Unknown Patient");
        } catch (Exception e) {
            resp.setPatientName("Unknown Patient");
        }
        
        try {
            resp.setDoctorName(order.getDoctor() != null ? order.getDoctor().getFirstName() + " " + order.getDoctor().getLastName() : "Unknown Doctor");
        } catch (Exception e) {
            resp.setDoctorName("Unknown Doctor");
        }
        resp.setTestType(order.getTestType());
        resp.setPriority(order.getPriority() != null ? order.getPriority().name() : "ROUTINE");
        resp.setStatus(order.getStatus() != null ? order.getStatus().name() : "ORDERED");
        resp.setSpecialInstructions(order.getSpecialInstructions());
        resp.setOrderDate(order.getOrderDate());

        if (order.getLabResult() != null) {
            LabResult r = order.getLabResult();
            resp.setResultValue(r.getResultValue());
            resp.setUnit(r.getUnit());
            resp.setReferenceRange(r.getReferenceRange());
            resp.setIsCritical(r.getIsCritical());
            resp.setNotes(r.getNotes());

            // Auto-flag against reference range
            resp.setResultFlag(computeResultFlag(r.getResultValue(), r.getReferenceRange()));
        }

        return resp;
    }

    /**
     * Parse a reference range string like "70 - 100" or "< 200" and compare
     * to the numeric result value. Return "HIGH", "LOW", "NORMAL", or null.
     */
    private String computeResultFlag(String resultValue, String referenceRange) {
        if (resultValue == null || referenceRange == null) return null;
        try {
            double value = Double.parseDouble(resultValue.trim());
            String range = referenceRange.replaceAll("[^0-9.<>\\-\\s]", "").trim();

            // Handle "< X" format
            if (range.startsWith("<")) {
                double upper = Double.parseDouble(range.substring(1).trim());
                return value > upper ? "HIGH" : "NORMAL";
            }
            // Handle "> X" format
            if (range.startsWith(">")) {
                double lower = Double.parseDouble(range.substring(1).trim());
                return value < lower ? "LOW" : "NORMAL";
            }
            // Handle "X - Y" format
            if (range.contains("-")) {
                // Be careful with negative numbers; split on " - " pattern
                String[] parts = range.split("\\s*-\\s*");
                if (parts.length >= 2) {
                    double low = Double.parseDouble(parts[0].trim());
                    double high = Double.parseDouble(parts[parts.length - 1].trim());
                    if (value < low) return "LOW";
                    if (value > high) return "HIGH";
                    return "NORMAL";
                }
            }
        } catch (NumberFormatException ignored) {
            // Non-numeric result or range – cannot auto-flag
        }
        return null;
    }
}
