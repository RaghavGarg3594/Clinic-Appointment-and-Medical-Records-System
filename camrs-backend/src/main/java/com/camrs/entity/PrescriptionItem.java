package com.camrs.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "PrescriptionItem")
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;

    @Column(name = "medication_name", nullable = false, length = 100)
    private String medicationName;

    @Column(nullable = false, length = 50)
    private String dosage;

    @Column(nullable = false, length = 50)
    private String frequency;

    @Column(nullable = false, length = 50)
    private String duration;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(length = 50)
    private String route;

    @Column(name = "meal_instruction", length = 100)
    private String mealInstruction;

    public PrescriptionItem() {}

    public Integer getId() { return this.id; }
    public void setId(Integer id) { this.id = id; }
    public Prescription getPrescription() { return this.prescription; }
    public void setPrescription(Prescription prescription) { this.prescription = prescription; }
    public Medication getMedication() { return this.medication; }
    public void setMedication(Medication medication) { this.medication = medication; }
    public String getMedicationName() { return this.medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    public String getDosage() { return this.dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getFrequency() { return this.frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getDuration() { return this.duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public Integer getQuantity() { return this.quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getRoute() { return this.route; }
    public void setRoute(String route) { this.route = route; }
    public String getMealInstruction() { return this.mealInstruction; }
    public void setMealInstruction(String mealInstruction) { this.mealInstruction = mealInstruction; }
}
