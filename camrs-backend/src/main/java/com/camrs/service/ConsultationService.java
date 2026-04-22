package com.camrs.service;

import com.camrs.dto.*;
import com.camrs.entity.*;
import com.camrs.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConsultationService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicationRepository medicationRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final BillRepository billRepository;
    private final NotificationService notificationService;
    private final Icd10CodeRepository icd10CodeRepository;

    public ConsultationService(AppointmentRepository appointmentRepository, DoctorRepository doctorRepository,
                               MedicalRecordRepository medicalRecordRepository, PrescriptionRepository prescriptionRepository,
                               MedicationRepository medicationRepository, UserRepository userRepository,
                               PatientRepository patientRepository, BillRepository billRepository,
                               NotificationService notificationService, Icd10CodeRepository icd10CodeRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.medicationRepository = medicationRepository;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.billRepository = billRepository;
        this.notificationService = notificationService;
        this.icd10CodeRepository = icd10CodeRepository;
    }

    /**
     * Parses frequency string like "1x daily", "2x daily", "3x daily", "4x daily"
     * and returns the numeric times-per-day value.
     */
    private int parseFrequency(String frequency) {
        if (frequency == null) return 1;
        String f = frequency.trim().toLowerCase();
        if (f.startsWith("4")) return 4;
        if (f.startsWith("3")) return 3;
        if (f.startsWith("2")) return 2;
        return 1;
    }

    /**
     * Parses duration string like "3 days", "7 days", "14 days", "30 days"
     * and returns the numeric number of days.
     */
    private int parseDuration(String duration) {
        if (duration == null) return 1;
        try {
            return Integer.parseInt(duration.trim().split("\\s+")[0]);
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * Calculates stock units to deduct.
     * Each stock unit = 1 individual tablet/dose.
     * Formula: (frequencyPerDay * durationDays)
     */
    private int calculateStockDeduction(String frequency, String duration) {
        int timesPerDay = parseFrequency(frequency);
        int days = parseDuration(duration);
        return timesPerDay * days;
    }

    @Transactional
    public MedicalRecordResponse recordConsultation(String doctorEmail, ConsultationRecordRequest request) {
        User user = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Validate follow-up date must be in the future
        if (request.getFollowUpDate() != null && !request.getFollowUpDate().isAfter(java.time.LocalDate.now())) {
            throw new RuntimeException("Follow-up date must be in the future");
        }

        // Validate stock levels for all prescribed medications before proceeding
        if (request.getPrescriptionItems() != null) {
            for (PrescriptionItemRequest itemReq : request.getPrescriptionItems()) {
                Medication med = medicationRepository.findById(itemReq.getMedicationId())
                        .orElseThrow(() -> new RuntimeException("Medication not found: " + itemReq.getMedicationId()));
                int requiredQty = calculateStockDeduction(itemReq.getFrequency(), itemReq.getDuration());
                int currentStock = med.getStockQuantity() != null ? med.getStockQuantity() : 0;
                if (requiredQty > currentStock) {
                    throw new RuntimeException("Insufficient stock for " + med.getName()
                            + ": required " + requiredQty + " units, but only " + currentStock + " available");
                }
            }
        }

        MedicalRecord record = new MedicalRecord();
        record.setPatient(appointment.getPatient());
        record.setDoctor(doctor);
        record.setAppointment(appointment);
        record.setVisitDate(LocalDateTime.now());
        record.setChiefComplaint(request.getChiefComplaint());
        record.setVitalSigns(request.getVitalSigns());
        record.setDiagnosis(request.getDiagnosis());
        record.setAdvice(request.getAdvice());
        record.setFollowUpDate(request.getFollowUpDate());

        // Resolve ICD-10 code if provided
        if (request.getIcd10Code() != null && !request.getIcd10Code().trim().isEmpty()) {
            icd10CodeRepository.findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    request.getIcd10Code(), request.getIcd10Code())
                    .stream().findFirst().ifPresent(record::setIcd10Code);
        }

        if (request.getSeverity() != null) {
            try {
                record.setSeverity(MedicalRecord.Severity.valueOf(request.getSeverity()));
            } catch (Exception e) {
                record.setSeverity(MedicalRecord.Severity.LOW);
            }
        }

        record = medicalRecordRepository.save(record);

        if (request.getPrescriptionItems() != null && !request.getPrescriptionItems().isEmpty()) {
            Prescription prescription = new Prescription();
            prescription.setMedicalRecord(record);
            prescription.setPatient(appointment.getPatient());
            prescription.setDoctor(doctor);
            prescription.setCreatedBy(user);
            prescription.setIssueDate(LocalDateTime.now());
            prescription = prescriptionRepository.save(prescription);

            List<PrescriptionItem> items = new ArrayList<>();
            for (PrescriptionItemRequest itemReq : request.getPrescriptionItems()) {
                Medication med = medicationRepository.findById(itemReq.getMedicationId())
                        .orElseThrow(() -> new RuntimeException("Medication not found: " + itemReq.getMedicationId()));

                PrescriptionItem item = new PrescriptionItem();
                item.setPrescription(prescription);
                item.setMedication(med);
                item.setMedicationName(med.getName());
                item.setFrequency(itemReq.getFrequency());
                item.setDuration(itemReq.getDuration());
                item.setDosage(itemReq.getDosage() != null ? itemReq.getDosage() : "-");
                item.setRoute(itemReq.getRoute() != null ? itemReq.getRoute() : "Oral");
                item.setMealInstruction(itemReq.getMealInstruction() != null ? itemReq.getMealInstruction() : "After meals");

                // Calculate and store correct stock deduction quantity
                int stockUnits = calculateStockDeduction(itemReq.getFrequency(), itemReq.getDuration());
                item.setQuantity(stockUnits);

                // Deduct inventory immediately
                if (med.getStockQuantity() != null) {
                    med.setStockQuantity(Math.max(0, med.getStockQuantity() - stockUnits));
                    medicationRepository.save(med);
                }

                items.add(item);
            }
            prescription.setItems(items);
            prescription = prescriptionRepository.save(prescription);
            record.setPrescription(prescription);
        }
        
        record.setLabTestOrders(new ArrayList<>());

        appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        // Option B: Create a SEPARATE post-consultation bill for meds + labs (appointment fee bill was already created at booking)
        BigDecimal medicationCharge = BigDecimal.ZERO;
        
        if (record.getPrescription() != null && record.getPrescription().getItems() != null) {
            for (PrescriptionItem item : record.getPrescription().getItems()) {
                if (item.getMedication() != null && item.getMedication().getPrice() != null) {
                    int qty = item.getQuantity() != null ? item.getQuantity() : 1;
                    medicationCharge = medicationCharge.add(item.getMedication().getPrice().multiply(new BigDecimal(qty)));
                }
            }
        }
        
        BigDecimal labCharge = BigDecimal.ZERO; // Lab charges added later via lab order endpoint

        // Only update/create the supplementary bill if there are charges
        if (medicationCharge.compareTo(BigDecimal.ZERO) > 0 || labCharge.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal subTotal = medicationCharge.add(labCharge);
            BigDecimal medLabTax = subTotal.multiply(new BigDecimal("0.12")); // 12% TAX on new charges only

            // Check if a bill already exists for this appointment (created at booking time)
            java.util.Optional<Bill> existingBillOpt = billRepository.findByAppointmentId(appointment.getId());
            Bill bill;
            if (existingBillOpt.isPresent()) {
                bill = existingBillOpt.get();
                bill.setMedicationCharge(medicationCharge);
                bill.setLabCharge(labCharge);
                // Tax only on medication + lab charges (consultation fee was already taxed/paid)
                bill.setTax(medLabTax);
                // Total = already-paid consultation charge + new med/lab charges + tax on new charges
                bill.setTotalAmount(bill.getConsultationCharge().add(subTotal).add(medLabTax));
                bill.setStatus(Bill.BillStatus.UNPAID);
            } else {
                bill = new Bill();
                bill.setAppointment(appointment);
                bill.setPatient(appointment.getPatient());
                bill.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                bill.setIssueDate(LocalDateTime.now());
                bill.setConsultationCharge(BigDecimal.ZERO);
                bill.setMedicationCharge(medicationCharge);
                bill.setLabCharge(labCharge);
                bill.setTax(medLabTax);
                bill.setDiscount(BigDecimal.ZERO);
                bill.setTotalAmount(subTotal.add(medLabTax));
                bill.setStatus(Bill.BillStatus.UNPAID);
            }
            billRepository.save(bill);

            notificationService.sendBillGenerated(
                    appointment.getPatient().getEmail(),
                    bill.getInvoiceNumber(),
                    bill.getTotalAmount().toPlainString());
        }

        return mapToResponse(record);
    }

    public List<MedicalRecordResponse> getPatientRecords(String patientEmail) {
        User user = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        return medicalRecordRepository.findByPatientIdOrderByVisitDateDesc(patient.getId())
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MedicalRecordResponse> getDoctorRecords(String doctorEmail) {
        User user = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));

        return medicalRecordRepository.findByDoctorIdOrderByVisitDateDesc(doctor.getId())
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MedicalRecordResponse getRecordById(Integer id) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical record not found"));
        return mapToResponse(record);
    }

    public List<MedicalRecordResponse> getRecordsByPatientId(Integer patientId) {
        return medicalRecordRepository.findByPatientIdOrderByVisitDateDesc(patientId)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MedicalRecordResponse getLastPrescriptionForPatient(Integer patientId) {
        List<MedicalRecord> records = medicalRecordRepository.findByPatientIdOrderByVisitDateDesc(patientId);
        for (MedicalRecord r : records) {
            if (r.getPrescription() != null && r.getPrescription().getItems() != null && !r.getPrescription().getItems().isEmpty()) {
                return mapToResponse(r);
            }
        }
        return null;
    }

    private MedicalRecordResponse mapToResponse(MedicalRecord record) {
        MedicalRecordResponse resp = new MedicalRecordResponse();
        resp.setId(record.getId());
        resp.setAppointmentId(record.getAppointment().getId());
        resp.setDoctorName(record.getDoctor().getFirstName() + " " + record.getDoctor().getLastName());
        resp.setSpecialization(record.getDoctor().getSpecialization());
        resp.setPatientName(record.getPatient().getFirstName() + " " + record.getPatient().getLastName());
        resp.setVisitDate(record.getVisitDate());
        resp.setChiefComplaint(record.getChiefComplaint());
        resp.setVitalSigns(record.getVitalSigns());
        resp.setDiagnosis(record.getDiagnosis());
        resp.setIcd10Code(record.getIcd10Code() != null ? record.getIcd10Code().getCode() : null);
        resp.setSeverity(record.getSeverity() != null ? record.getSeverity().name() : "LOW");
        resp.setAdvice(record.getAdvice());
        resp.setFollowUpDate(record.getFollowUpDate());

        if (record.getPrescription() != null && record.getPrescription().getItems() != null) {
            resp.setPrescriptionItems(record.getPrescription().getItems().stream().map(item ->
                    new PrescriptionItemResponse(
                            item.getId(),
                            item.getMedication().getName(),
                            item.getDosage(),
                            item.getFrequency(),
                            item.getDuration(),
                            item.getRoute(),
                            item.getMealInstruction()
                    )
            ).collect(Collectors.toList()));
        } else {
            resp.setPrescriptionItems(Collections.emptyList());
        }

        return resp;
    }

    /**
     * Check prescribed medications against patient allergies.
     * Returns a list of warning strings for any matches.
     */
    @SuppressWarnings("unchecked")
    public List<String> checkAllergies(Map<String, Object> body) {
        Integer patientId = null;
        if (body.get("patientId") instanceof Number) {
            patientId = ((Number) body.get("patientId")).intValue();
        }
        List<Integer> medicationIds = new ArrayList<>();
        if (body.get("medicationIds") instanceof List) {
            for (Object o : (List<?>) body.get("medicationIds")) {
                if (o instanceof Number) {
                    medicationIds.add(((Number) o).intValue());
                }
            }
        }

        if (patientId == null || medicationIds.isEmpty()) {
            return Collections.emptyList();
        }

        Patient patient = patientRepository.findById(patientId).orElse(null);
        if (patient == null || patient.getAllergies() == null || patient.getAllergies().trim().isEmpty()) {
            return Collections.emptyList();
        }

        String allergiesLower = patient.getAllergies().toLowerCase();
        // Split by common delimiters
        String[] allergyParts = allergiesLower.split("[,;/|]+");

        List<String> warnings = new ArrayList<>();
        for (Integer medId : medicationIds) {
            Medication med = medicationRepository.findById(medId).orElse(null);
            if (med == null) continue;
            String medNameLower = med.getName().toLowerCase();
            for (String allergyPart : allergyParts) {
                String allergy = allergyPart.trim();
                if (allergy.isEmpty()) continue;
                if (medNameLower.contains(allergy) || allergy.contains(medNameLower.split(" ")[0])) {
                    warnings.add("⚠ " + med.getName() + " may conflict with patient allergy: " + allergyPart.trim());
                    break;
                }
            }
        }
        return warnings;
    }
}
