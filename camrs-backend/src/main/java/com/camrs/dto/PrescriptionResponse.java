package com.camrs.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PrescriptionResponse {
    private Integer id;
    private Integer medicalRecordId;
    private LocalDateTime issueDate;
    private LocalDateTime printTimestamp;
    private List<PrescriptionItemResponse> items;

    public PrescriptionResponse() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getMedicalRecordId() { return medicalRecordId; }
    public void setMedicalRecordId(Integer medicalRecordId) { this.medicalRecordId = medicalRecordId; }
    public LocalDateTime getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDateTime issueDate) { this.issueDate = issueDate; }
    public LocalDateTime getPrintTimestamp() { return printTimestamp; }
    public void setPrintTimestamp(LocalDateTime printTimestamp) { this.printTimestamp = printTimestamp; }
    public List<PrescriptionItemResponse> getItems() { return items; }
    public void setItems(List<PrescriptionItemResponse> items) { this.items = items; }
}
