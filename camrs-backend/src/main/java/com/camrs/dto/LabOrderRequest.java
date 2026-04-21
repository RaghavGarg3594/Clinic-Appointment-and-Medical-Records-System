package com.camrs.dto;

public class LabOrderRequest {
    private Integer appointmentId;
    private Integer testTypeId;
    private String testType;
    private String priority; // ROUTINE, URGENT, STAT
    private String specialInstructions;

    public LabOrderRequest() {}

    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }
    public Integer getTestTypeId() { return testTypeId; }
    public void setTestTypeId(Integer testTypeId) { this.testTypeId = testTypeId; }
    public String getTestType() { return testType; }
    public void setTestType(String testType) { this.testType = testType; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
}
