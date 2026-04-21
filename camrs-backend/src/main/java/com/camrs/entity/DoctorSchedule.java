package com.camrs.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "DoctorSchedule")
public class DoctorSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "doctor_id", nullable = false, unique = true)
    private Doctor doctor;

    @Column(name = "working_days", nullable = false)
    private String workingDays; // e.g., "Mon,Tue,Wed,Thu,Fri"

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "leave_date")
    private LocalDate leaveDate;

    public DoctorSchedule() {}

    public DoctorSchedule(Integer id, Doctor doctor, String workingDays, LocalTime startTime, LocalTime endTime, LocalDate leaveDate) {
        this.id = id;
        this.doctor = doctor;
        this.workingDays = workingDays;
        this.startTime = startTime;
        this.endTime = endTime;
        this.leaveDate = leaveDate;
    }

    public Integer getId() { return this.id; }
    public void setId(Integer id) { this.id = id; }
    public Doctor getDoctor() { return this.doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public String getWorkingDays() { return this.workingDays; }
    public void setWorkingDays(String workingDays) { this.workingDays = workingDays; }
    public LocalTime getStartTime() { return this.startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return this.endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public LocalDate getLeaveDate() { return this.leaveDate; }
    public void setLeaveDate(LocalDate leaveDate) { this.leaveDate = leaveDate; }
}
