package com.camrs.dto;

import java.time.LocalDate;

public class ReportFilterRequest {
    private String reportType; // CONSULTATION_STATS, DISEASE_STATS, OUTSTANDING_PAYMENTS, REVENUE
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer doctorId;

    public ReportFilterRequest() {}

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getDoctorId() { return doctorId; }
    public void setDoctorId(Integer doctorId) { this.doctorId = doctorId; }
}
