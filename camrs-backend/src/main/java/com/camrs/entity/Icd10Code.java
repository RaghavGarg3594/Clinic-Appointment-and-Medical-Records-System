package com.camrs.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "icd10_codes")
public class Icd10Code {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 255)
    private String description;

    public Icd10Code() {}

    public Icd10Code(Integer id, String code, String description) {
        this.id = id;
        this.code = code;
        this.description = description;
    }

    public Integer getId() { return this.id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return this.code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }
}
