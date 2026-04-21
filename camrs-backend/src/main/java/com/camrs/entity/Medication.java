package com.camrs.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "Medication")
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String category;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Column(name = "reorder_level")
    private Integer reorderLevel = 10;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "price", precision = 10, scale = 2)
    private java.math.BigDecimal price;

    public Medication() {}

    public Medication(Integer id, String name, String category, Integer stockQuantity, Integer reorderLevel, LocalDate expiryDate, java.math.BigDecimal price) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.stockQuantity = stockQuantity;
        this.reorderLevel = reorderLevel;
        this.expiryDate = expiryDate;
        this.price = price;
    }

    public Integer getId() { return this.id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return this.category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getStockQuantity() { return this.stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public Integer getReorderLevel() { return this.reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }
    public LocalDate getExpiryDate() { return this.expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public java.math.BigDecimal getPrice() { return this.price; }
    public void setPrice(java.math.BigDecimal price) { this.price = price; }
}
