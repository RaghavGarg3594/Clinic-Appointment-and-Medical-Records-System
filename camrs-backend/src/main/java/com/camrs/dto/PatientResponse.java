package com.camrs.dto;


public class PatientResponse {
    private Integer id;
    private Integer userId;
    private String firstName;
    private String lastName;

    public PatientResponse() {}

    public PatientResponse(Integer id, Integer userId, String firstName, String lastName) {
        this.id = id;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Integer getId() { return this.id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return this.userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getFirstName() { return this.firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return this.lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}
