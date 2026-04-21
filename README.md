# CAMRS (Clinic Appointment and Medical Records System)

A fully integrated, end-to-end full-stack healthcare portal built with **Spring Boot**, **React (Vite)**, and **MySQL**.

## 📂 Project Structure
```
/
├── camrs-backend/
│   ├── src/main/java/com/camrs
│   │   ├── config/       (SecurityConfig, DataInitializer)
│   │   ├── controller/   (RESTful Web APIs)
│   │   ├── dto/          (Data Transfer Objects)
│   │   ├── entity/       (JPA Models reflecting DB tables)
│   │   ├── exception/    (Global Exception Handlers)
│   │   ├── repository/   (Spring Data JPA interfaces)
│   │   ├── security/     (JWT Utils, Custom Details, Filters)
│   │   └── service/      (Core business logic & PDF generators)
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── data.sql      (ICD-10 code seed data)
│   └── pom.xml
│
├── camrs-frontend/
│   ├── src/
│   │   ├── components/   (Layout, Sidebar, ProtectedRoute, ShadCN UI)
│   │   ├── context/      (AuthContext managing JWT & session state)
│   │   ├── pages/        (Login, Multi-Role Dashboards)
│   │   ├── services/     (Axios API client with JWT interceptors)
│   │   └── index.css     (Tailwind + custom design tokens)
│   ├── package.json
│   └── vite.config.js
│
├── maven/                (Bundled Apache Maven 3.9.6 — no install needed)
├── camrs-full-setup.sql  (Complete MySQL schema + seed data)
├── RUN_COMMANDS.ps1      (Step-by-step run script — copy-paste each step)
├── .env                  (Environment variables — DB credentials)
└── .gitignore
```

---

## ⚙️ Prerequisites (Install Before Running)

| Tool | Minimum Version | Check Installed | Download |
|------|-----------------|-----------------|----------|
| **Java JDK** | 17+ | `java --version` | [Adoptium](https://adoptium.net/) |
| **MySQL Server** | 8.0+ | `mysql --version` | [MySQL](https://dev.mysql.com/downloads/installer/) |
| **Node.js** | 18+ | `node --version` | [Node.js](https://nodejs.org/) |

> **Maven is bundled** inside the `maven/` folder. You do NOT need to install Maven separately.

---

## 🚀 Quick Start (Copy-Paste — Step by Step)

> Open **VS Code** → **Terminal** → Paste each command **one at a time**, in order.  
> You will need **TWO terminals**: Terminal 1 for Backend, Terminal 2 for Frontend.

### ── Terminal 1 ──

**Step 1: Fix PowerShell Execution Policy (run once, ever)**
```powershell
Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned -Force
```

**Step 2: Refresh PATH (so node/npm are found if just installed)**
```powershell
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
```

**Step 3: Create the MySQL Database**
```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "DROP DATABASE IF EXISTS camrs; CREATE DATABASE camrs;"
```
> It will prompt for your MySQL root password. Type it and press Enter.  
> If your MySQL is installed at a different path, adjust accordingly.

**Step 4: Build the Backend JAR (~1–2 minutes)**
```powershell
& ".\maven\apache-maven-3.9.6\bin\mvn.cmd" -f ".\camrs-backend\pom.xml" clean package -DskipTests
```
> Run this from the project root folder (`CAMRS_Swe`).  
> Wait until you see `BUILD SUCCESS`.

**Step 5: Start the Backend Server (port 8080)**
```powershell
java -DDB_PASS='YOUR_MYSQL_PASSWORD' -jar ".\camrs-backend\target\camrs-backend-1.0.0-SNAPSHOT.jar"
```
> Replace `YOUR_MYSQL_PASSWORD` with your actual MySQL root password.  
> Wait until you see `Started CamrsApplication in X seconds`.  
> **Keep this terminal running. Do NOT close it.**

### ── Terminal 2 (Open a NEW terminal) ──

**Step 6: Refresh PATH again**
```powershell
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
```

**Step 7: Install Frontend Dependencies & Start**
```powershell
cd camrs-frontend
npm install
npm run dev
```
> Wait until you see `Local: http://localhost:5173/`.  
> **Keep this terminal running. Do NOT close it.**

### ── Step 8: Open in Browser ──

Go to: **http://localhost:5173/login**

---

## 🔐 Default Login Credentials

All passwords are: **`password123`**

| Role        | Email               | Name             | Dashboard        |
|-------------|---------------------|------------------|------------------|
| Admin Staff | admin@camrs.com     | Arjun Mehta      | Admin Dashboard  |
| Doctor      | doctor@camrs.com    | Dr. Priya Sharma | Doctor Dashboard |
| Lab Staff   | lab@camrs.com       | Neha Verma       | Lab Dashboard    |
| Patient 1   | patient@camrs.com   | Rahul Kumar      | Patient Dashboard|
| Patient 2   | patient2@camrs.com  | Ananya Iyer      | Patient Dashboard|

---

## 🔧 Troubleshooting

### `npm` or `node` not found
Run Step 2 to refresh the PATH. If still not found, close VS Code and reopen it (Node.js PATH change needs a restart).

### `mysql.exe` not found
Your MySQL may be installed elsewhere. Check:
```powershell
Get-ChildItem "C:\Program Files\MySQL" -Recurse -Filter "mysql.exe" | Select-Object -ExpandProperty FullName
```
Use the path it returns instead.

### `BUILD FAILURE` during Maven build
- Make sure Java is installed: `java --version`
- Make sure you're in the project root folder when running the build command

### Backend fails to start
- Make sure MySQL is running (check Windows Services → `MySQL80`)
- Make sure the database `camrs` exists
- Make sure the password in `-DDB_PASS='...'` matches your MySQL root password

### Port already in use
- Backend (8080): `netstat -ano | findstr :8080` — kill the PID shown
- Frontend (5173): `netstat -ano | findstr :5173` — kill the PID shown

---

## 🧑‍💻 Architecture & Feature Summary

### Core Architecture
- **Backend:** Spring Boot 3.2.4 with JPA (Hibernate), JWT authentication, role-based access control
- **Frontend:** React 18 + Vite + ShadCN UI + Tailwind CSS v4 + Framer Motion animations
- **Database:** MySQL 8.0 with auto-generated schema (Hibernate `update` strategy)
- **PDF:** Apache PDFBox generates prescription/lab PDFs as byte streams

### Key Features
- **Personalized Welcome:** Shows user's real name (e.g., "Welcome, Dr. Priya Sharma")
- **Comprehensive Patient Registration:** Support for capturing detailed medical history, allergies, and insurance details upfront.
- **Appointment Time Validation:** Strictly filters out past time slots on the current day during booking.
- **Doctor Consultation & ICD-10 Search:** Integrated searchable ICD-10 component for selecting standardized diagnoses.
- **Allergy Check Soft Warning:** Proactively maps prescribed medications against a patient's recorded allergies and displays clinical warnings.
- **Smart Result Flagging:** Lab results are intelligently flagged as HIGH/LOW/NORMAL with visual badges based on auto-parsing of reference ranges.
- **Admin Resource Management:** UI and APIs to directly manage Lab Staff profiles and Doctor access.
- **Appointment Workflow:** Book → Approval Pending → Admin Pays → Ongoing → Doctor Consults → Completed
- **30-Min Slots:** 9:00 AM – 6:00 PM, Monday–Friday only (weekends blocked).
- **Billing:** Flat appointment fee at booking; Patient Billing is view-only (only admin processes payments).
- **Prescription & Lab PDFs:** Beautifully formatted PDFs available for download.
- **Admin Doctor Approval:** Approving Doctor join requests automatically populates backend.
- **Default Database Seed:** Pre-seeds database with robust drug lists (20+ common medications) and typical lab test profiles.
