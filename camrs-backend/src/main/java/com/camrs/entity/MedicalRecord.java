package com.camrs.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "MedicalRecord")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @OneToOne
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @Column(name = "visit_date")
    private LocalDateTime visitDate = LocalDateTime.now();

    @Column(name = "chief_complaint", columnDefinition = "TEXT")
    private String chiefComplaint;

    @Column(name = "vital_signs", columnDefinition = "TEXT")
    private String vitalSigns;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @ManyToOne
    @JoinColumn(name = "icd10_code_id")
    private Icd10Code icd10Code;

    @Enumerated(EnumType.STRING)
    private Severity severity = Severity.LOW;

    @Column(columnDefinition = "TEXT")
    private String advice;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    // Relationships
    @OneToOne(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private Prescription prescription;

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LabTestOrder> labTestOrders;

    public enum Severity {
        LOW, MODERATE, HIGH, CRITICAL
    }

    public MedicalRecord() {}

    public MedicalRecord(Integer id, Patient patient, Doctor doctor, Appointment appointment, LocalDateTime visitDate, String chiefComplaint, String vitalSigns, String diagnosis, Icd10Code icd10Code, Severity severity, String advice, LocalDate followUpDate, Prescription prescription, List<LabTestOrder> labTestOrders) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.appointment = appointment;
        this.visitDate = visitDate;
        this.chiefComplaint = chiefComplaint;
        this.vitalSigns = vitalSigns;
        this.diagnosis = diagnosis;
        this.icd10Code = icd10Code;
        this.severity = severity;
        this.advice = advice;
        this.followUpDate = followUpDate;
        this.prescription = prescription;
        this.labTestOrders = labTestOrders;
    }

    public Integer getId() { return this.id; }
    public void setId(Integer id) { this.id = id; }
    public Patient getPatient() { return this.patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Doctor getDoctor() { return this.doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public Appointment getAppointment() { return this.appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }
    public LocalDateTime getVisitDate() { return this.visitDate; }
    public void setVisitDate(LocalDateTime visitDate) { this.visitDate = visitDate; }
    public String getChiefComplaint() { return this.chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }
    public String getVitalSigns() { return this.vitalSigns; }
    public void setVitalSigns(String vitalSigns) { this.vitalSigns = vitalSigns; }
    public String getDiagnosis() { return this.diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public Icd10Code getIcd10Code() { return this.icd10Code; }
    public void setIcd10Code(Icd10Code icd10Code) { this.icd10Code = icd10Code; }
    public Severity getSeverity() { return this.severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public String getAdvice() { return this.advice; }
    public void setAdvice(String advice) { this.advice = advice; }
    public LocalDate getFollowUpDate() { return this.followUpDate; }
    public void setFollowUpDate(LocalDate followUpDate) { this.followUpDate = followUpDate; }
    public Prescription getPrescription() { return this.prescription; }
    public void setPrescription(Prescription prescription) { this.prescription = prescription; }
    public List<LabTestOrder> getLabTestOrders() { return this.labTestOrders; }
    public void setLabTestOrders(List<LabTestOrder> labTestOrders) { this.labTestOrders = labTestOrders; }
}
