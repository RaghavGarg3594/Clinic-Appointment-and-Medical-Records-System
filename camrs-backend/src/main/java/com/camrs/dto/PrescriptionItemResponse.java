package com.camrs.dto;

public class PrescriptionItemResponse {
    private Integer id;
    private String medicationName;
    private String dosage;
    private String frequency;
    private String duration;
    private String route;
    private String mealInstruction;

    public PrescriptionItemResponse() {}

    public PrescriptionItemResponse(Integer id, String medicationName, String dosage, String frequency, String duration, String route, String mealInstruction) {
        this.id = id;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.route = route;
        this.mealInstruction = mealInstruction;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
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
