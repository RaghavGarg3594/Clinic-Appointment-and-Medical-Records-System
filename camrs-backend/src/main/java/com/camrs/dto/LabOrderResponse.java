package com.camrs.dto;

import java.time.LocalDateTime;

public class LabOrderResponse {
    private Integer id;
    private String patientName;
    private String doctorName;
    private String testType;
    private String priority;
    private String status;
    private String specialInstructions;
    private LocalDateTime orderDate;
    private String resultValue;
    private String unit;
    private String referenceRange;
    private Boolean isCritical;
    private String notes;
    private String resultFlag;
    private Boolean billPaid;

    public LabOrderResponse() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getTestType() { return testType; }
    public void setTestType(String testType) { this.testType = testType; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public String getResultValue() { return resultValue; }
    public void setResultValue(String resultValue) { this.resultValue = resultValue; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getReferenceRange() { return referenceRange; }
    public void setReferenceRange(String referenceRange) { this.referenceRange = referenceRange; }
    public Boolean getIsCritical() { return isCritical; }
    public void setIsCritical(Boolean isCritical) { this.isCritical = isCritical; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getResultFlag() { return resultFlag; }
    public void setResultFlag(String resultFlag) { this.resultFlag = resultFlag; }
    public Boolean getBillPaid() { return billPaid; }
    public void setBillPaid(Boolean billPaid) { this.billPaid = billPaid; }
}
