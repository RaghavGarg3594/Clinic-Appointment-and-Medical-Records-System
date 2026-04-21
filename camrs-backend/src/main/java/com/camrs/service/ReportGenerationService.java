package com.camrs.service;

import com.camrs.entity.*;
import com.camrs.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportGenerationService {

    private final AppointmentRepository appointmentRepository;
    private final BillRepository billRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final MedicationRepository medicationRepository;

    public ReportGenerationService(AppointmentRepository appointmentRepository,
                                   BillRepository billRepository,
                                   MedicalRecordRepository medicalRecordRepository,
                                   DoctorRepository doctorRepository,
                                   PatientRepository patientRepository,
                                   MedicationRepository medicationRepository) {
        this.appointmentRepository = appointmentRepository;
        this.billRepository = billRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.medicationRepository = medicationRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPatients", patientRepository.count());
        stats.put("totalDoctors", doctorRepository.count());

        List<Bill> allBills = billRepository.findAll();
        BigDecimal todayRevenue = allBills.stream()
                .filter(b -> b.getStatus() == Bill.BillStatus.PAID && b.getIssueDate() != null
                        && b.getIssueDate().toLocalDate().equals(LocalDate.now()))
                .map(Bill::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("todayRevenue", todayRevenue);

        long lowStock = medicationRepository.findAll().stream()
                .filter(m -> m.getStockQuantity() <= m.getReorderLevel())
                .count();
        stats.put("lowStockMedications", lowStock);

        long totalAppointmentsToday = appointmentRepository.findAll().stream()
                .filter(a -> a.getAppointmentDate().equals(LocalDate.now()))
                .count();
        stats.put("totalAppointmentsToday", totalAppointmentsToday);

        BigDecimal totalRevenue = allBills.stream()
                .filter(b -> b.getStatus() == Bill.BillStatus.PAID)
                .map(Bill::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalRevenue", totalRevenue);

        BigDecimal outstandingAmount = allBills.stream()
                .filter(b -> b.getStatus() != Bill.BillStatus.PAID)
                .map(Bill::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("outstandingAmount", outstandingAmount);

        return stats;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getConsultationStats() {
        return doctorRepository.findAll().stream().map(doctor -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("doctorId", doctor.getId());
            entry.put("doctorName", "Dr. " + doctor.getFirstName() + " " + doctor.getLastName());
            entry.put("specialization", doctor.getSpecialization());

            List<Appointment> appointments = appointmentRepository.findAll().stream()
                    .filter(a -> a.getDoctor() != null && a.getDoctor().getId().equals(doctor.getId()))
                    .collect(Collectors.toList());

            entry.put("totalAppointments", appointments.size());
            entry.put("completedAppointments", appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED).count());
            entry.put("cancelledAppointments", appointments.stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.CANCELLED).count());

            BigDecimal revenue = billRepository.findAll().stream()
                    .filter(b -> b.getAppointment() != null 
                              && b.getAppointment().getDoctor() != null 
                              && b.getAppointment().getDoctor().getId().equals(doctor.getId())
                              && b.getStatus() == Bill.BillStatus.PAID)
                    .map(b -> b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            entry.put("revenue", revenue);

            return entry;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDiseaseStats() {
        // FR-AR4: Group by ICD-10 code when available, fallback to free-text diagnosis
        List<MedicalRecord> records = medicalRecordRepository.findAll().stream()
                .filter(r -> r.getDiagnosis() != null && !r.getDiagnosis().isEmpty())
                .collect(Collectors.toList());

        Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();

        for (MedicalRecord r : records) {
            String key;
            String icd10Code = "";
            String diagnosisText = r.getDiagnosis();

            if (r.getIcd10Code() != null) {
                key = r.getIcd10Code().getCode();
                icd10Code = r.getIcd10Code().getCode();
                diagnosisText = r.getIcd10Code().getDescription();
            } else {
                key = diagnosisText;
            }

            grouped.computeIfAbsent(key, k -> {
                Map<String, Object> row = new HashMap<>();
                row.put("count", 0L);
                return row;
            });

            Map<String, Object> row = grouped.get(key);
            row.put("diagnosis", diagnosisText);
            row.put("icd10Code", icd10Code);
            row.put("count", (Long) row.get("count") + 1);
        }

        return grouped.values().stream()
                .sorted((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getOutstandingPayments() {
        return billRepository.findAll().stream()
                .filter(b -> b.getStatus() != Bill.BillStatus.PAID)
                .map(b -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("billId", b.getId());
                    row.put("invoiceNumber", b.getInvoiceNumber());
                    row.put("patientName", b.getPatient().getFirstName() + " " + b.getPatient().getLastName());
                    row.put("totalAmount", b.getTotalAmount());
                    row.put("status", b.getStatus().name());
                    row.put("issueDate", b.getIssueDate());
                    return row;
                })
                .collect(Collectors.toList());
    }
}
