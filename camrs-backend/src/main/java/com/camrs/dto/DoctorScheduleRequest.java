package com.camrs.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class DoctorScheduleRequest {
    private String workingDays;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate leaveDate;

    public DoctorScheduleRequest() {}

    public String getWorkingDays() { return workingDays; }
    public void setWorkingDays(String workingDays) { this.workingDays = workingDays; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    
    public LocalDate getLeaveDate() { return leaveDate; }
    public void setLeaveDate(LocalDate leaveDate) { this.leaveDate = leaveDate; }
}
