package com.camrs.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;
@Entity
@Table(name = "Doctor")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;
    @Column(nullable = false, length = 100)
    private String specialization;
    @Column(nullable = false, length = 150)
    private String qualification;
    @Column(name = "license_number", nullable = false, unique = true, length = 50)
    private String licenseNumber;
    @Column(length = 15)
    private String phone;
    @Column(length = 100)
    private String email;
    @Column(name = "consultation_fee")
    private BigDecimal consultationFee;
    @Column(name = "working_hours", columnDefinition = "TEXT")
    private String workingHours;
    @Column(name = "is_active")
    private Boolean isActive = true;
    @OneToOne(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private DoctorSchedule schedule;
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointments;
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedicalRecord> medicalRecords;
    public Doctor() {}
    public Integer getId() { return this.id; }
    public void setId(Integer id) { this.id = id; }
    public User getUser() { return this.user; }
    public void setUser(User user) { this.user = user; }
    public String getFirstName() { return this.firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return this.lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getSpecialization() { return this.specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getQualification() { return this.qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    public String getLicenseNumber() { return this.licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public String getPhone() { return this.phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }
    public BigDecimal getConsultationFee() { return this.consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }
    public String getWorkingHours() { return this.workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }
    public Boolean getIsActive() { return this.isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public DoctorSchedule getSchedule() { return this.schedule; }
    public void setSchedule(DoctorSchedule schedule) { this.schedule = schedule; }
    public List<Appointment> getAppointments() { return this.appointments; }
    public void setAppointments(List<Appointment> appointments) { this.appointments = appointments; }
    public List<MedicalRecord> getMedicalRecords() { return this.medicalRecords; }
    public void setMedicalRecords(List<MedicalRecord> medicalRecords) { this.medicalRecords = medicalRecords; }
}
