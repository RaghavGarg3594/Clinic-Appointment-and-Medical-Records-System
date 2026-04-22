<div align="center">

# 🏥 CAMRS — Clinic Appointment & Medical Records System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.2-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-4.2-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white)](https://tailwindcss.com)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

**A full-stack, role-based healthcare management platform for clinics — featuring appointment scheduling, electronic medical records, lab management, pharmacy inventory, billing, and automated email notifications.**

[Features](#-features) · [Tech Stack](#-tech-stack) · [Architecture](#-architecture) · [Getting Started](#-getting-started) · [API Reference](#-api-reference) · [Screenshots](#-screenshots) · [Testing](#-testing) · [Contributing](#-contributing)

---

</div>

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Database Schema](#-database-schema)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Running the Application](#running-the-application)
- [Default Credentials](#-default-credentials)
- [API Reference](#-api-reference)
- [Email Notifications](#-email-notifications)
- [Security](#-security)
- [Testing](#-testing)
- [Project Structure](#-project-structure)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

### 🔐 Authentication & Authorization
- JWT-based stateless authentication with configurable token expiration
- Role-based access control (RBAC) — 4 distinct roles with granular permissions
- Account lockout after 5 consecutive failed login attempts
- Session inactivity timeout with automatic logout
- Secure password reset via identity verification (name + DOB)

### 👨‍⚕️ Doctor Portal
- View and manage daily appointment queue with approval/rejection workflow
- Full clinical consultation interface — vitals, diagnosis (ICD-10), severity grading
- Prescription management with real-time **inventory stock validation**
- Order lab tests with priority levels (Routine / Urgent / STAT)
- Review lab results and patient medical history
- Download professional PDF prescriptions

### 🧑‍💼 Patient Portal
- Self-registration with email confirmation
- Book appointments — Routine, Follow-up, First Visit, and **Emergency** (auto slot insertion)
- Reschedule or cancel appointments with reason tracking
- View medical records, prescriptions, and lab results
- Download prescription and lab report PDFs
- Track billing and payment history
- Profile management with emergency contact support

### 🔬 Lab Staff Portal
- View all pending lab test orders with priority indicators
- Process sample collection (blocked until patient bill is paid)
- Enter results with critical flagging — **automatic email alert to patient**
- Reference range auto-population from test type catalog

### 🏢 Admin Dashboard
- Comprehensive system analytics with chart visualizations (Recharts)
- Doctor onboarding — review and approve/reject join requests
- Staff management (Doctors, Lab Staff)
- Medication inventory management with stock & expiry tracking
- Billing management — view, filter, and mark bills as paid
- Generate system reports (appointments, revenue, lab)
- Full audit log with user action tracking

### 📧 Automated Email Notifications
- **Registration** — Welcome email on patient account creation
- **Appointment Booked** — Confirmation with doctor details and token number
- **Appointment Rescheduled** — Updated schedule notification
- **Critical Lab Result** — Urgent email alert with result details
- **Password Reset** — Confirmation after successful password change

### 📄 PDF Generation
- Professional prescription PDFs with clinic branding, styled tables, and follow-up details
- Lab report PDFs with result flagging and reference ranges
- Built with Apache PDFBox 3.0 — no external template engine required

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | React 18 · Vite 5 · Tailwind CSS 4 · React Router 6 · Axios |
| **UI Components** | Radix UI · Shadcn/UI · Lucide Icons · Tabler Icons · Framer Motion |
| **Charts** | Recharts 3 |
| **Backend** | Java 17+ · Spring Boot 3.2.4 · Spring Security · Spring Data JPA |
| **Authentication** | JWT (jjwt 0.11.5) |
| **Database** | MySQL 8.0 · Hibernate 6.4 (auto DDL) |
| **Email** | Spring Boot Mail · Gmail SMTP (STARTTLS) |
| **PDF** | Apache PDFBox 3.0.1 |
| **Build** | Maven 3.9.6 · npm |
| **Testing** | Selenium WebDriver · Python (pytest) |

---

## 🏗 Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                          │
│   React 18 + Vite + Tailwind CSS + React Router v6           │
│   Axios HTTP Client (JWT in Authorization header)            │
└──────────────────────┬───────────────────────────────────────┘
                       │ REST API (JSON)
                       ▼
┌──────────────────────────────────────────────────────────────┐
│                     APPLICATION LAYER                        │
│   Spring Boot 3.2.4                                          │
│   ┌────────────────┐  ┌─────────────┐  ┌──────────────┐     │
│   │  Controllers   │  │  Security   │  │   Config     │     │
│   │  (7 REST APIs) │  │  JWT Filter │  │  CORS, Mail  │     │
│   └───────┬────────┘  └──────┬──────┘  └──────────────┘     │
│           │                  │                               │
│   ┌───────▼──────────────────▼───────────────────────┐       │
│   │              Service Layer (12 services)          │       │
│   │  Auth · Appointment · Consultation · Lab · Admin  │       │
│   │  Billing · Patient · Notification · Email · PDF   │       │
│   │  Report Generation · Audit Log                    │       │
│   └───────┬──────────────────────────────────────────┘       │
│           │                                                  │
│   ┌───────▼──────────────────────────────────────────┐       │
│   │          Repository Layer (18 JPA repos)          │       │
│   └───────┬──────────────────────────────────────────┘       │
└───────────┼──────────────────────────────────────────────────┘
            │ JDBC (HikariCP)
            ▼
┌──────────────────────────────────────────────────────────────┐
│                      DATA LAYER                              │
│   MySQL 8.0 — 18 tables · Indexed · Foreign Keys             │
└──────────────────────────────────────────────────────────────┘
```

---

## 🗃 Database Schema

The system uses **18 normalized tables** with proper foreign key relationships:

```
User ──┬── Patient ──── Appointment ──── MedicalRecord ──── Prescription ──── PrescriptionItem
       │                     │                  │                                     │
       ├── Doctor ───────────┘                  ├── LabTestOrder ── LabResult    Medication
       │      │                                 │
       ├── Staff                           icd10_codes
       │
       ├── DoctorJoinRequest          Bill ──── Payment
       │
       └── AuditLog               DoctorSchedule ── Doctor

                                  LabTestType ── LabTestOrder
```

> Full schema with seed data: [`camrs-full-setup.sql`](camrs-full-setup.sql)

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version | Download |
|-------------|---------|----------|
| **Java JDK** | 17 or higher | [Oracle](https://www.oracle.com/java/technologies/downloads/) / [Adoptium](https://adoptium.net/) |
| **MySQL Server** | 8.0+ | [MySQL Downloads](https://dev.mysql.com/downloads/installer/) |
| **Node.js** | 18+ (with npm) | [Node.js](https://nodejs.org/) |
| **Git** | Latest | [Git SCM](https://git-scm.com/) |

> **Note:** Maven 3.9.6 is bundled with the project — no separate installation required.

### Installation

```bash
# 1. Clone the repository
git clone https://github.com/RaghavGarg3594/Clinic-Appointment-and-Medical-Records-System.git
cd Clinic-Appointment-and-Medical-Records-System
```

### Running the Application

The project includes a step-by-step setup script. Open **two terminals** in the project root:

#### Terminal 1 — Database + Backend

```powershell
# Step 1: Fix PowerShell execution policy (one-time)
Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned -Force

# Step 2: Refresh PATH
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")

# Step 3: Create database (enter MySQL root password when prompted)
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "DROP DATABASE IF EXISTS camrs; CREATE DATABASE camrs;"

# Step 4: Build backend JAR (~1-2 minutes)
& ".\maven\apache-maven-3.9.6\bin\mvn.cmd" -f ".\camrs-backend\pom.xml" clean package -DskipTests

# Step 5: Start backend (replace YOUR_MYSQL_PASSWORD)
java -DDB_PASS='YOUR_MYSQL_PASSWORD' -jar ".\camrs-backend\target\camrs-backend-1.0.0-SNAPSHOT.jar"
```

> ⏳ Wait until you see **`Started CamrsApplication`** in the console.

#### Terminal 2 — Frontend

```powershell
# Step 6: Refresh PATH
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")

# Step 7: Install frontend dependencies
cd camrs-frontend
npm install

# Step 8: Start frontend dev server
npm run dev
```

#### Step 9: Open in Browser

Navigate to **http://localhost:5173/login**

---

## 🔑 Default Credentials

All default accounts use the password: **`password123`**

| Role | Email | Dashboard |
|------|-------|-----------|
| **Admin** | `admin@camrs.com` | System management, billing, reports |
| **Doctor** | `doctor@camrs.com` | Appointments, consultations, prescriptions |
| **Lab Staff** | `lab@camrs.com` | Sample collection, result entry |
| **Patient** | `patient@camrs.com` | Appointments, records, lab results |
| **Patient 2** | `patient2@camrs.com` | Second patient account for testing |

---

## 📡 API Reference

Base URL: `http://localhost:8080/api`

### Authentication
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/auth/register` | Register new patient | ❌ |
| `POST` | `/auth/login` | Login and receive JWT | ❌ |
| `POST` | `/auth/logout` | Logout | ❌ |
| `POST` | `/auth/forgot-password` | Reset password by name + DOB | ❌ |
| `POST` | `/auth/doctor-request` | Submit doctor join request | ❌ |

### Appointments
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/appointments/book` | Book new appointment | 🔒 Patient |
| `GET` | `/appointments/my` | Get patient's appointments | 🔒 Patient |
| `PUT` | `/appointments/{id}/reschedule` | Reschedule appointment | 🔒 Patient |
| `PUT` | `/appointments/{id}/cancel` | Cancel appointment | 🔒 Patient |
| `GET` | `/appointments/doctor/today` | Doctor's today appointments | 🔒 Doctor |
| `GET` | `/appointments/slots` | Available time slots | 🔒 Patient |

### Consultations
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/consultations/record` | Record consultation + prescription | 🔒 Doctor |
| `GET` | `/consultations/history/{patientId}` | Patient history | 🔒 Doctor |

### Lab Management
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/lab/orders` | Create lab test order | 🔒 Doctor |
| `GET` | `/lab/pending` | Get pending lab orders | 🔒 Lab |
| `PUT` | `/lab/orders/{id}/collect` | Mark sample collected | 🔒 Lab |
| `PUT` | `/lab/orders/{id}/results` | Enter lab results | 🔒 Lab |

### Patient Portal
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/patients/me` | Get profile | 🔒 Patient |
| `PUT` | `/patients/me` | Update profile | 🔒 Patient |
| `GET` | `/patients/me/medical-records` | Medical records | 🔒 Patient |
| `GET` | `/patients/me/lab-results` | Lab results | 🔒 Patient |
| `GET` | `/patients/me/prescriptions/{id}/pdf` | Download prescription PDF | 🔒 Patient |
| `GET` | `/patients/me/lab-results/{id}/pdf` | Download lab report PDF | 🔒 Patient |

### Admin
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/admin/stats` | Dashboard statistics | 🔒 Admin |
| `GET` | `/admin/doctors` | List all doctors | 🔒 Admin |
| `GET` | `/admin/billing` | All bills | 🔒 Admin |
| `PUT` | `/admin/billing/{id}/pay` | Mark bill paid | 🔒 Admin |
| `GET` | `/admin/audit-logs` | Audit trail | 🔒 Admin |
| `GET` | `/admin/reports/generate` | Generate reports | 🔒 Admin |

---

## 📧 Email Notifications

The system sends **5 types of automated emails** via Gmail SMTP:

| Trigger | Subject | Recipient |
|---------|---------|-----------|
| Patient registration | Welcome to CAMRS | New patient |
| Appointment booked | Appointment Confirmed \| Token: TK-XXXX | Patient |
| Appointment rescheduled | Appointment Rescheduled | Patient |
| Critical lab result | ⚠️ URGENT: Critical Lab Result | Patient |
| Password reset | Password Reset Successful | Patient |

### Configuration

Email is configured via `application.yml` with Gmail App Password:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enabled: true
```

> To use your own Gmail account, generate an [App Password](https://myaccount.google.com/apppasswords) and update the credentials in `.env`.

---

## 🔒 Security

| Feature | Implementation |
|---------|---------------|
| **Authentication** | JWT tokens (HS256) with 30-minute expiration |
| **Password Storage** | BCrypt hashing (cost factor 10) |
| **Account Lockout** | Auto-lock after 5 failed login attempts |
| **Session Timeout** | Frontend auto-logout after 15 minutes of inactivity |
| **CORS** | Configured for localhost development |
| **Input Validation** | Server-side validation on all endpoints |
| **SQL Injection** | Protected via JPA parameterized queries |
| **Audit Logging** | All critical actions recorded with timestamp and user ID |
| **Sensitive Data** | Credentials externalized to `.env` (excluded via `.gitignore`) |

---

## 🧪 Testing

### Selenium End-to-End Tests

The project includes comprehensive Selenium WebDriver tests:

```bash
cd selenium-tests
pip install -r requirements.txt
pytest test_camrs.py -v
```

**Test coverage includes:**
- Authentication flows (login, registration, lockout)
- Appointment booking and management
- Doctor consultation workflow
- Lab test ordering and result entry
- Billing and payment processing
- PDF download verification

---

## 📁 Project Structure

```
CAMRS_Swe/
├── camrs-backend/                  # Spring Boot backend
│   ├── src/main/java/com/camrs/
│   │   ├── config/                 # Security config, data initializer
│   │   ├── controller/             # 7 REST controllers
│   │   ├── dto/                    # Request/Response DTOs
│   │   ├── entity/                 # 18 JPA entity classes
│   │   ├── exception/              # Custom exception handlers
│   │   ├── repository/             # 18 Spring Data JPA repositories
│   │   ├── security/               # JWT util, filter, UserDetailsService
│   │   └── service/                # 12 business logic services
│   ├── src/main/resources/
│   │   └── application.yml         # App configuration
│   └── pom.xml                     # Maven dependencies
│
├── camrs-frontend/                 # React + Vite frontend
│   ├── src/
│   │   ├── components/             # Reusable UI components (Sidebar, etc.)
│   │   ├── context/                # React context (AuthContext)
│   │   ├── lib/                    # Utility functions
│   │   ├── pages/                  # 24 page components
│   │   ├── services/               # Axios API service layer
│   │   ├── App.jsx                 # Router + route guards
│   │   └── index.css               # Global styles
│   └── package.json
│
├── selenium-tests/                 # E2E test suite
│   ├── test_camrs.py               # Selenium test cases
│   ├── conftest.py                 # Test fixtures and config
│   └── requirements.txt            # Python dependencies
│
├── maven/                          # Bundled Maven 3.9.6
├── camrs-full-setup.sql            # Complete DB schema + seed data
├── RUN_COMMANDS.ps1                # Step-by-step setup script
├── .env.example                    # Environment variable template
└── .gitignore                      # Git exclusion rules
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Code Standards

- Backend: Follow standard Java/Spring Boot conventions
- Frontend: Use functional React components with hooks
- All PRs must pass the existing Selenium test suite
- Sensitive credentials must never be committed — use `.env`

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Built with ❤️ for healthcare**

</div>
