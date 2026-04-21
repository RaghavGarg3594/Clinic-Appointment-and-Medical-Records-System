package com.camrs.dto;

public class PrescriptionItemRequest {
    private Integer medicationId;
    private String dosage;
    private String frequency;
    private String duration;
    private String route;
    private String mealInstruction;

    public PrescriptionItemRequest() {}

    public Integer getMedicationId() { return medicationId; }
    public void setMedicationId(Integer medicationId) { this.medicationId = medicationId; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public String getMealInstruction() { return mealInstruction; }
    public void setMealInstruction(String mealInstruction) { this.mealInstruction = mealInstruction; }
}
