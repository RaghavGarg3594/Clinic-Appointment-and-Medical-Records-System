# Missing / Not Implemented Features (from SRS.txt)

This document lists functional and non-functional requirements from the SRS that are **NOT currently implemented** in the CAMRS codebase.

---

## Patient Registration & Profile (Section 3.1)

> Emergency contact, medical history, allergies, insurance, data validation — all implemented.

---

## Appointment Scheduling (Section 3.2)

| SRS ID | Feature | Status |
|--------|---------|--------|
| FR-AM7 | Email/SMS reminders 24 hours before appointment | ❌ Not implemented |
| FR-AM8 | Emergency appointment slots outside regular hours | ❌ Not implemented |

> Booking, doctor schedule view, token generation, reschedule, cancel — all implemented.

---

## Medical Records (Section 3.3)

| SRS ID | Feature | Status |
|--------|---------|--------|
| FR-MR5 | Search records by date or diagnosis keyword | ❌ Not implemented (only by patient) |
| FR-MR6 | Field-level audit trail on record modifications | ❌ Not implemented (basic audit log exists) |

> Record consultation, prescription generation, medical history access, ICD-10 coding — all implemented.

---

## Laboratory Management (Section 3.4)

| SRS ID | Feature | Status |
|--------|---------|--------|
| FR-LM3 | Immediate email/SMS critical-value alerts to doctors | ❌ Not implemented (flag displayed in UI only) |
| FR-LM6 | Granular status tracking (Ordered → Sample Collected → In Progress → Completed) | ❌ Partial (only PENDING / COMPLETED tracked) |

> Test orders, result entry, reference range flagging, report PDF download, test history — all implemented.

---

## Billing & Payments (Section 3.5)

| SRS ID | Feature | Status |
|--------|---------|--------|
| FR-BM3 | Real payment gateway integration (Card/UPI/Insurance) | ❌ Not implemented (payment recorded manually by admin) |
| FR-BM4 | Overdue bill alerts and due-date tracking | ❌ Not implemented |
| FR-BM5 | Automated email/SMS bill delivery | ❌ Not implemented |

> Auto bill generation, itemized billing, payment recording, payment history — all implemented.

---

## Administration & Reporting (Section 3.6)

| SRS ID | Feature | Status |
|--------|---------|--------|
| FR-AR3 | Doctor-wise consultation statistics with revenue | ❌ Partial (dashboard counts exist, not detailed drill-down) |
| FR-AR5 | Outstanding payments report with patient list | ❌ Not implemented |
| FR-AR6 | Monthly/quarterly revenue analysis with breakdowns | ❌ Not implemented |
| FR-AR7 | Appointment analytics (completed/cancelled/no-show rates) | ❌ Not implemented |

> Doctor management, inventory management, lab staff management — all implemented.

---

## Non-Functional Requirements (Sections 4–5)

| SRS ID | Requirement | Status |
|--------|-------------|--------|
| UI-3 | Breadcrumb navigation | ❌ Not implemented |
| CI-3 | Real-time WebSocket/SSE notifications | ❌ Not implemented |
| DE-2 | SMTP email service integration | ❌ Not implemented |
| DE-3 | Payment gateway integration | ❌ Not implemented |

> Role-based access, password hashing, HTTPS-ready architecture, responsive design — all implemented.
