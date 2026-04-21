package com.camrs.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate = LocalDateTime.now();

    public enum PaymentMethod {
        CASH, CREDIT_CARD, DEBIT_CARD, UPI, INSURANCE
    }

    public Payment() {}

    public Payment(Integer id, Bill bill, BigDecimal amount, PaymentMethod method, String transactionRef, LocalDateTime paymentDate) {
        this.id = id;
        this.bill = bill;
        this.amount = amount;
        this.method = method;
        this.transactionRef = transactionRef;
        this.paymentDate = paymentDate;
    }

    public Integer getId() { return this.id; }
    public void setId(Integer id) { this.id = id; }
    public Bill getBill() { return this.bill; }
    public void setBill(Bill bill) { this.bill = bill; }
    public BigDecimal getAmount() { return this.amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public PaymentMethod getMethod() { return this.method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
    public String getTransactionRef() { return this.transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }
    public LocalDateTime getPaymentDate() { return this.paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
}
