package com.camrs.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    private Integer id;
    private Integer billId;
    private BigDecimal amount;
    private String method;
    private String transactionRef;
    private LocalDateTime paymentDate;

    public PaymentResponse() {}
    public PaymentResponse(Integer id, Integer billId, BigDecimal amount, String method, String transactionRef, LocalDateTime paymentDate) {
        this.id = id; this.billId = billId; this.amount = amount; this.method = method;
        this.transactionRef = transactionRef; this.paymentDate = paymentDate;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getBillId() { return billId; }
    public void setBillId(Integer billId) { this.billId = billId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
}
