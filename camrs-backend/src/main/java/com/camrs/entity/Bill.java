package com.camrs.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Bill")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @Column(name = "issue_date")
    private LocalDateTime issueDate = LocalDateTime.now();

    @Column(name = "consultation_charge")
    private BigDecimal consultationCharge = BigDecimal.ZERO;

    @Column(name = "lab_charge")
    private BigDecimal labCharge = BigDecimal.ZERO;

    @Column(name = "medication_charge")
    private BigDecimal medicationCharge = BigDecimal.ZERO;

    @Column(name = "procedure_charge")
    private BigDecimal procedureCharge = BigDecimal.ZERO;

    private BigDecimal discount = BigDecimal.ZERO;

    private BigDecimal tax = BigDecimal.ZERO;

    @Column(name = "total_amount")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private BillStatus status = BillStatus.UNPAID;

    // Relationships
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments;

    public enum BillStatus {
        UNPAID, PARTIALLY_PAID, PAID
    }

    public Bill() {}

    public Bill(Integer id, Appointment appointment, Patient patient, String invoiceNumber, LocalDateTime issueDate, BigDecimal consultationCharge, BigDecimal labCharge, BigDecimal medicationCharge, BigDecimal procedureCharge, BigDecimal discount, BigDecimal tax, BigDecimal totalAmount, BillStatus status, List<Payment> payments) {
        this.id = id;
        this.appointment = appointment;
        this.patient = patient;
        this.invoiceNumber = invoiceNumber;
        this.issueDate = issueDate;
        this.consultationCharge = consultationCharge;
        this.labCharge = labCharge;
        this.medicationCharge = medicationCharge;
        this.procedureCharge = procedureCharge;
        this.discount = discount;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.status = status;
        this.payments = payments;
    }

    public Integer getId() { return this.id; }
    public void setId(Integer id) { this.id = id; }
    public Appointment getAppointment() { return this.appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }
    public Patient getPatient() { return this.patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public String getInvoiceNumber() { return this.invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public LocalDateTime getIssueDate() { return this.issueDate; }
    public void setIssueDate(LocalDateTime issueDate) { this.issueDate = issueDate; }
    public BigDecimal getConsultationCharge() { return this.consultationCharge; }
    public void setConsultationCharge(BigDecimal consultationCharge) { this.consultationCharge = consultationCharge; }
    public BigDecimal getLabCharge() { return this.labCharge; }
    public void setLabCharge(BigDecimal labCharge) { this.labCharge = labCharge; }
    public BigDecimal getMedicationCharge() { return this.medicationCharge; }
    public void setMedicationCharge(BigDecimal medicationCharge) { this.medicationCharge = medicationCharge; }
    public BigDecimal getProcedureCharge() { return this.procedureCharge; }
    public void setProcedureCharge(BigDecimal procedureCharge) { this.procedureCharge = procedureCharge; }
    public BigDecimal getDiscount() { return this.discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public BigDecimal getTax() { return this.tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }
    public BigDecimal getTotalAmount() { return this.totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BillStatus getStatus() { return this.status; }
    public void setStatus(BillStatus status) { this.status = status; }
    public List<Payment> getPayments() { return this.payments; }
    public void setPayments(List<Payment> payments) { this.payments = payments; }
}
