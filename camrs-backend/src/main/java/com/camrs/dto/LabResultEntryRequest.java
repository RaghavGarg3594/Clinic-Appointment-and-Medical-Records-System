package com.camrs.dto;

public class LabResultEntryRequest {
    private String resultValue;
    private String unit;
    private String referenceRange;
    private Boolean isCritical;
    private String notes;

    public LabResultEntryRequest() {}

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
}
