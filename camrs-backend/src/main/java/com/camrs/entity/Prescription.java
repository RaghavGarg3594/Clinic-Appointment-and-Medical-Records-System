package com.camrs.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Prescription")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "medical_record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "issue_date")
    private LocalDateTime issueDate = LocalDateTime.now();

    @Column(name = "print_timestamp")
    private LocalDateTime printTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrescriptionItem> items;

    public Prescription() {}

    public Integer getId() { return this.id; }
    public void setId(Integer id) { this.id = id; }
    public MedicalRecord getMedicalRecord() { return this.medicalRecord; }
    public void setMedicalRecord(MedicalRecord medicalRecord) { this.medicalRecord = medicalRecord; }
    public Patient getPatient() { return this.patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Doctor getDoctor() { return this.doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public LocalDateTime getIssueDate() { return this.issueDate; }
    public void setIssueDate(LocalDateTime issueDate) { this.issueDate = issueDate; }
    public LocalDateTime getPrintTimestamp() { return this.printTimestamp; }
    public void setPrintTimestamp(LocalDateTime printTimestamp) { this.printTimestamp = printTimestamp; }
    public User getCreatedBy() { return this.createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public List<PrescriptionItem> getItems() { return this.items; }
    public void setItems(List<PrescriptionItem> items) { this.items = items; }
}
