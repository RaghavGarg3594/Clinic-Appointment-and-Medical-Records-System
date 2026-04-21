package com.camrs.dto;

import com.camrs.entity.DoctorJoinRequest;
import com.camrs.entity.DoctorJoinRequest.RequestStatus;
import java.time.LocalDateTime;

public class DoctorJoinRequestResponse {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String specialization;
    private String qualification;
    private String licenseNumber;
    private Integer experienceYears;
    private String message;
    private RequestStatus status;
    private String adminNotes;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;

    public DoctorJoinRequestResponse(DoctorJoinRequest r) {
        this.id = r.getId();
        this.firstName = r.getFirstName();
        this.lastName = r.getLastName();
        this.email = r.getEmail();
        this.phone = r.getPhone();
        this.specialization = r.getSpecialization();
        this.qualification = r.getQualification();
        this.licenseNumber = r.getLicenseNumber();
        this.experienceYears = r.getExperienceYears();
        this.message = r.getMessage();
        this.status = r.getStatus();
        this.adminNotes = r.getAdminNotes();
        this.submittedAt = r.getSubmittedAt();
        this.updatedAt = r.getUpdatedAt();
    }

    public Integer getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getSpecialization() { return specialization; }
    public String getQualification() { return qualification; }
    public String getLicenseNumber() { return licenseNumber; }
    public Integer getExperienceYears() { return experienceYears; }
    public String getMessage() { return message; }
    public RequestStatus getStatus() { return status; }
    public String getAdminNotes() { return adminNotes; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
