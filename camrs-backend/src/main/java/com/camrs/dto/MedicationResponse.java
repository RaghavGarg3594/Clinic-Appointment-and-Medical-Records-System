package com.camrs.dto;

import java.time.LocalDate;

public class MedicationResponse {
    private Integer id;
    private String name;
    private String category;
    private Integer stockQuantity;
    private Integer reorderLevel;
    private LocalDate expiryDate;
    private java.math.BigDecimal price;

    public MedicationResponse() {}

    public MedicationResponse(Integer id, String name, String category, Integer stockQuantity, Integer reorderLevel, LocalDate expiryDate, java.math.BigDecimal price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.stockQuantity = stockQuantity;
        this.reorderLevel = reorderLevel;
        this.expiryDate = expiryDate;
        this.price = price;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public java.math.BigDecimal getPrice() { return price; }
    public void setPrice(java.math.BigDecimal price) { this.price = price; }
}
