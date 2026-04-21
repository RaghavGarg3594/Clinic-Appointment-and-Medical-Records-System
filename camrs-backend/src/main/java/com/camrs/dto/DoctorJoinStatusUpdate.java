package com.camrs.dto;

public class DoctorJoinStatusUpdate {
    private String status;
    private String adminNotes;
    private String username;
    private String password;

    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String v) { this.adminNotes = v; }
    public String getUsername() { return username; }
    public void setUsername(String v) { this.username = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { this.password = v; }
}
