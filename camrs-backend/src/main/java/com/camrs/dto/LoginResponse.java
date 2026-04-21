package com.camrs.dto;


public class LoginResponse {
    private String token;
    private String role;
    private Integer userId;
    private String fullName;

    public LoginResponse() {}

    public LoginResponse(String token, String role, Integer userId, String fullName) {
        this.token = token;
        this.role = role;
        this.userId = userId;
        this.fullName = fullName;
    }

    public String getToken() { return this.token; }
    public void setToken(String token) { this.token = token; }
    public String getRole() { return this.role; }
    public void setRole(String role) { this.role = role; }
    public Integer getUserId() { return this.userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getFullName() { return this.fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
