package com.camrs.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "LabTestType")
public class LabTestType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "test_name", nullable = false, length = 100)
    private String testName;

    @Column(name = "test_code", nullable = false, unique = true, length = 20)
    private String testCode;

    @Column(length = 50)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "normal_range_male", length = 100)
    private String normalRangeMale;

    @Column(name = "normal_range_female", length = 100)
    private String normalRangeFemale;

    @Column(length = 30)
    private String unit;

    @Column(precision = 10, scale = 2)
    private java.math.BigDecimal cost;

    @Enumerated(EnumType.STRING)
    @Column(name = "sample_type", nullable = false)
    private SampleType sampleType;

    @Column(name = "turnaround_time")
    private Integer turnaroundTime;

    @Column(name = "preparation_instructions", columnDefinition = "TEXT")
    private String preparationInstructions;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public enum SampleType {
        Blood, Urine, Stool, Saliva, Sputum, Tissue, Swab, Other
    }

    public LabTestType() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }
    public String getTestCode() { return testCode; }
    public void setTestCode(String testCode) { this.testCode = testCode; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getNormalRangeMale() { return normalRangeMale; }
    public void setNormalRangeMale(String normalRangeMale) { this.normalRangeMale = normalRangeMale; }
    public String getNormalRangeFemale() { return normalRangeFemale; }
    public void setNormalRangeFemale(String normalRangeFemale) { this.normalRangeFemale = normalRangeFemale; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public java.math.BigDecimal getCost() { return cost; }
    public void setCost(java.math.BigDecimal cost) { this.cost = cost; }
    public SampleType getSampleType() { return sampleType; }
    public void setSampleType(SampleType sampleType) { this.sampleType = sampleType; }
    public Integer getTurnaroundTime() { return turnaroundTime; }
    public void setTurnaroundTime(Integer turnaroundTime) { this.turnaroundTime = turnaroundTime; }
    public String getPreparationInstructions() { return preparationInstructions; }
    public void setPreparationInstructions(String preparationInstructions) { this.preparationInstructions = preparationInstructions; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
