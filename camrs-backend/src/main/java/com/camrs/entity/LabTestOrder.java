package com.camrs.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "LabTestOrder")
public class LabTestOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_type_id", nullable = false)
    private LabTestType testTypeEntity;

    @Column(name = "test_type", nullable = false, length = 100)
    private String testType;

    @Enumerated(EnumType.STRING)
    private TestPriority priority = TestPriority.ROUTINE;

    @Enumerated(EnumType.STRING)
    private TestStatus status = TestStatus.ORDERED;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    @Column(name = "order_date")
    private LocalDateTime orderDate = LocalDateTime.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @OneToOne(mappedBy = "labTestOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private LabResult labResult;

    public enum TestPriority {
        ROUTINE, URGENT, STAT
    }

    public enum TestStatus {
        ORDERED, SAMPLE_COLLECTED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public LabTestOrder() {}

    public LabTestOrder(Integer id, Patient patient, Doctor doctor, MedicalRecord medicalRecord, String testType, TestPriority priority, TestStatus status, String specialInstructions, LocalDateTime orderDate, LabResult labResult) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.medicalRecord = medicalRecord;
        this.testType = testType;
        this.priority = priority;
        this.status = status;
        this.specialInstructions = specialInstructions;
        this.orderDate = orderDate;
        this.labResult = labResult;
    }

    public Integer getId() { return this.id; }
    public void setId(Integer id) { this.id = id; }
    public Patient getPatient() { return this.patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Doctor getDoctor() { return this.doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public MedicalRecord getMedicalRecord() { return this.medicalRecord; }
    public void setMedicalRecord(MedicalRecord medicalRecord) { this.medicalRecord = medicalRecord; }
    public LabTestType getTestTypeEntity() { return this.testTypeEntity; }
    public void setTestTypeEntity(LabTestType testTypeEntity) { this.testTypeEntity = testTypeEntity; }
    public String getTestType() { return this.testType; }
    public void setTestType(String testType) { this.testType = testType; }
    public TestPriority getPriority() { return this.priority; }
    public void setPriority(TestPriority priority) { this.priority = priority; }
    public TestStatus getStatus() { return this.status; }
    public void setStatus(TestStatus status) { this.status = status; }
    public String getSpecialInstructions() { return this.specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    public LocalDateTime getOrderDate() { return this.orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public LabResult getLabResult() { return this.labResult; }
    public void setLabResult(LabResult labResult) { this.labResult = labResult; }
    public User getCreatedBy() { return this.createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
}
