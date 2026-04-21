package com.camrs.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ReportResponse {
    private String reportType;
    private LocalDateTime generatedAt;
    private List<Map<String, Object>> data;
    private Map<String, Object> summary;

    public ReportResponse() {}

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public List<Map<String, Object>> getData() { return data; }
    public void setData(List<Map<String, Object>> data) { this.data = data; }
    public Map<String, Object> getSummary() { return summary; }
    public void setSummary(Map<String, Object> summary) { this.summary = summary; }
}
