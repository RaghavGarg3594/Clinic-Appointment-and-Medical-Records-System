package com.camrs.dto;

import java.math.BigDecimal;

public class PaymentRequest {
    private Integer billId;
    private BigDecimal amount;
    private String method; // CASH, CREDIT_CARD, DEBIT_CARD, UPI, INSURANCE
    private String transactionRef;

    public PaymentRequest() {}

    public Integer getBillId() { return billId; }
    public void setBillId(Integer billId) { this.billId = billId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }
}
