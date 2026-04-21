package com.camrs.dto;

public class PrescriptionRequest {
    private Integer medicalRecordId;

    public PrescriptionRequest() {}

    public Integer getMedicalRecordId() { return medicalRecordId; }
    public void setMedicalRecordId(Integer medicalRecordId) { this.medicalRecordId = medicalRecordId; }
}
