package com.camrs.dto;

import java.math.BigDecimal;

public class DoctorResponse {
    private Integer id;
    private Integer userId;
    private String firstName;
    private String lastName;
    private String specialization;
    private String email;
    private String phone;
    private BigDecimal consultationFee;
    private String workingHours;
    private Boolean isActive;
    private String qualification;
    private String licenseNumber;

    public DoctorResponse() {}

    public DoctorResponse(Integer id, Integer userId, String firstName, String lastName, String specialization, String email, String phone, BigDecimal consultationFee, String workingHours, Boolean isActive, String qualification, String licenseNumber) {
        this.id = id;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.email = email;
        this.phone = phone;
        this.consultationFee = consultationFee;
        this.workingHours = workingHours;
        this.isActive = isActive;
        this.qualification = qualification;
        this.licenseNumber = licenseNumber;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }
    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
}
