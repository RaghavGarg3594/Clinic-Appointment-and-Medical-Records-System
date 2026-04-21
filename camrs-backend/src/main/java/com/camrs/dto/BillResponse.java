package com.camrs.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BillResponse {
    private Integer id;
    private Integer appointmentId;
    private String patientName;
    private String invoiceNumber;
    private LocalDateTime issueDate;
    private BigDecimal consultationCharge;
    private BigDecimal labCharge;
    private BigDecimal medicationCharge;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal totalAmount;
    private String status;
    private Boolean hasPendingLabTests;
    private Boolean hasLabReport;

    public BillResponse() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public LocalDateTime getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDateTime issueDate) { this.issueDate = issueDate; }
    public BigDecimal getConsultationCharge() { return consultationCharge; }
    public void setConsultationCharge(BigDecimal consultationCharge) { this.consultationCharge = consultationCharge; }
    public BigDecimal getLabCharge() { return labCharge; }
    public void setLabCharge(BigDecimal labCharge) { this.labCharge = labCharge; }
    public BigDecimal getMedicationCharge() { return medicationCharge; }
    public void setMedicationCharge(BigDecimal medicationCharge) { this.medicationCharge = medicationCharge; }
    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getHasPendingLabTests() { return hasPendingLabTests; }
    public void setHasPendingLabTests(Boolean hasPendingLabTests) { this.hasPendingLabTests = hasPendingLabTests; }
    public Boolean getHasLabReport() { return hasLabReport; }
    public void setHasLabReport(Boolean hasLabReport) { this.hasLabReport = hasLabReport; }
}
