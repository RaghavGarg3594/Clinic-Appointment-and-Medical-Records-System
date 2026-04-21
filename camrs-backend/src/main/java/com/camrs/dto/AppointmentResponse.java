package com.camrs.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentResponse {
    private Integer id;
    private Integer doctorId;
    private String doctorName;
    private String specialization;
    private Integer patientId;
    private String patientName;
    private LocalDate appointmentDate;
    private LocalTime timeSlot;
    private String tokenNumber;
    private String type;
    private String status;
    private Boolean hasLabReport;

    public AppointmentResponse() {}

    public AppointmentResponse(Integer id, Integer doctorId, String doctorName, String specialization, 
            Integer patientId, String patientName, LocalDate appointmentDate, LocalTime timeSlot, 
            String tokenNumber, String type, String status, Boolean hasLabReport) {
        this.id = id;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.specialization = specialization;
        this.patientId = patientId;
        this.patientName = patientName;
        this.appointmentDate = appointmentDate;
        this.timeSlot = timeSlot;
        this.tokenNumber = tokenNumber;
        this.type = type;
        this.status = status;
        this.hasLabReport = hasLabReport;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getDoctorId() { return doctorId; }
    public void setDoctorId(Integer doctorId) { this.doctorId = doctorId; }
    
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    
    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }
    
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }
    
    public LocalTime getTimeSlot() { return timeSlot; }
    public void setTimeSlot(LocalTime timeSlot) { this.timeSlot = timeSlot; }
    
    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getHasLabReport() { return hasLabReport; }
    public void setHasLabReport(Boolean hasLabReport) { this.hasLabReport = hasLabReport; }
}
