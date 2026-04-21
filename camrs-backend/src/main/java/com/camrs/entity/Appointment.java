package com.camrs.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
@Entity
@Table(name = "Appointment")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;
    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;
    @Column(name = "appointment_time", nullable = false)
    private LocalTime appointmentTime;
    @Column(name = "time_slot", nullable = false)
    private LocalTime timeSlot;
    @Column(name = "token_number", unique = true, length = 50)
    private String tokenNumber;
    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type")
    private AppointmentType appointmentType = AppointmentType.ROUTINE;
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;
    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
    private MedicalRecord medicalRecord;
    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
    private Bill bill;
    public enum AppointmentType {
        FIRST_VISIT, FOLLOW_UP, ROUTINE, EMERGENCY
    }
    public enum AppointmentStatus {
        APPROVAL_PENDING, SCHEDULED, CONFIRMED, CHECKED_IN, ONGOING, IN_PROGRESS, COMPLETED, CANCELLED, RESCHEDULED
    }
    public Appointment() {}
    public Integer getId() { return this.id; }
    public void setId(Integer id) { this.id = id; }
    public Patient getPatient() { return this.patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Doctor getDoctor() { return this.doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public LocalDate getAppointmentDate() { return this.appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }
    public LocalTime getAppointmentTime() { return this.appointmentTime; }
    public void setAppointmentTime(LocalTime appointmentTime) { this.appointmentTime = appointmentTime; }
    public LocalTime getTimeSlot() { return this.timeSlot; }
    public void setTimeSlot(LocalTime timeSlot) { this.timeSlot = timeSlot; }
    public String getTokenNumber() { return this.tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }
    public AppointmentType getAppointmentType() { return this.appointmentType; }
    public void setAppointmentType(AppointmentType appointmentType) { this.appointmentType = appointmentType; }
    public AppointmentStatus getStatus() { return this.status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
    public String getCancellationReason() { return this.cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    public MedicalRecord getMedicalRecord() { return this.medicalRecord; }
    public void setMedicalRecord(MedicalRecord medicalRecord) { this.medicalRecord = medicalRecord; }
    public Bill getBill() { return this.bill; }
    public void setBill(Bill bill) { this.bill = bill; }
}
