package com.camrs.dto;


public class PatientRegistrationRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private java.time.LocalDate dateOfBirth;
    private String gender;
    private String phone;
    private String medicalHistory;
    private String allergies;
    private String insuranceDetails;
    private String emergencyContact;

    public PatientRegistrationRequest() {}

    public PatientRegistrationRequest(String email, String password, String firstName, String lastName, java.time.LocalDate dateOfBirth, String gender, String phone) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
    }

    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }
    public String getFirstName() { return this.firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return this.lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getGender() { return this.gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPhone() { return this.phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public java.time.LocalDate getDateOfBirth() { return this.dateOfBirth; }
    public void setDateOfBirth(java.time.LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getMedicalHistory() { return this.medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }
    public String getAllergies() { return this.allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }
    public String getInsuranceDetails() { return this.insuranceDetails; }
    public void setInsuranceDetails(String insuranceDetails) { this.insuranceDetails = insuranceDetails; }
    public String getEmergencyContact() { return this.emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
}
