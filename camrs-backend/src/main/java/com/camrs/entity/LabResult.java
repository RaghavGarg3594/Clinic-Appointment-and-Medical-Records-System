package com.camrs.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "LabResult")
public class LabResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private LabTestOrder labTestOrder;

    @Column(name = "result_value", length = 100)
    private String resultValue;

    @Column(length = 50)
    private String unit;

    @Column(name = "reference_range", length = 100)
    private String referenceRange;

    @Column(name = "is_critical")
    private Boolean isCritical = false;

    @Column(name = "entry_date")
    private LocalDateTime entryDate = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String notes;

    public LabResult() {}

    public LabResult(Integer id, LabTestOrder labTestOrder, String resultValue, String unit, String referenceRange, Boolean isCritical, LocalDateTime entryDate, String notes) {
        this.id = id;
        this.labTestOrder = labTestOrder;
        this.resultValue = resultValue;
        this.unit = unit;
        this.referenceRange = referenceRange;
        this.isCritical = isCritical;
        this.entryDate = entryDate;
        this.notes = notes;
    }

    public Integer getId() { return this.id; }
    public void setId(Integer id) { this.id = id; }
    public LabTestOrder getLabTestOrder() { return this.labTestOrder; }
    public void setLabTestOrder(LabTestOrder labTestOrder) { this.labTestOrder = labTestOrder; }
    public String getResultValue() { return this.resultValue; }
    public void setResultValue(String resultValue) { this.resultValue = resultValue; }
    public String getUnit() { return this.unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getReferenceRange() { return this.referenceRange; }
    public void setReferenceRange(String referenceRange) { this.referenceRange = referenceRange; }
    public Boolean getIsCritical() { return this.isCritical; }
    public void setIsCritical(Boolean isCritical) { this.isCritical = isCritical; }
    public LocalDateTime getEntryDate() { return this.entryDate; }
    public void setEntryDate(LocalDateTime entryDate) { this.entryDate = entryDate; }
    public String getNotes() { return this.notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
