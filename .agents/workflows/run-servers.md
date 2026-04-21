---
description: How to run the CAMRS frontend and backend servers
---

# Running CAMRS Servers (Windows)

## Prerequisites
- **Java JDK 17+** installed and `JAVA_HOME` configured (verify: `java -version`)
- **Apache Maven 3.9+** installed or use the bundled `maven\` folder
- **MySQL Server 8.0+** running on `localhost:3306`
- **Node.js 18+** installed with npm (verify: `node -v && npm -v`)
- `.env` file created at the project root (copy `.env.example` and fill in values)

## Step 0: Database Setup

Open a MySQL client (or MySQL Workbench) and run the full setup script:

```powershell
# // turbo
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < camrs-full-setup.sql
```

This creates the `camrs` database, all 18 tables, and seeds users/medications/lab-test-types/ICD-10 codes.

> **NOTE:** If you prefer to let JPA auto-create tables, you can skip this step — the backend uses `ddl-auto: update` and the `DataInitializer` class will seed users on first start.

## Step 1: Set Environment Variables

Load the variables from your `.env` file:

```powershell
# // turbo
Get-Content .env | ForEach-Object { if ($_ -match '^([^#]\S+?)=(.*)$') { [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2], "Process") } }
```

Or set manually:

```powershell
$env:DB_PASS = "YOUR_MYSQL_PASSWORD"
$env:DB_USER = "root"
```

## Step 2: Build & Start the Backend (Spring Boot — port 8080)

```powershell
# Build the JAR (skip tests for speed)
# // turbo
Set-Location camrs-backend; & "$env:JAVA_HOME\bin\java.exe" -version
```

```powershell
# Using the bundled Maven
# // turbo
& "..\maven\apache-maven-3.9.6\bin\mvn.cmd" clean package -DskipTests
```

```powershell
# Run the JAR
& "$env:JAVA_HOME\bin\java.exe" -jar target\camrs-backend-1.0.0-SNAPSHOT.jar
```

The backend will start on **http://localhost:8080**.

## Step 3: Start the Frontend (Vite + React — port 5173)

Open a **separate** terminal:

```powershell
# // turbo
Set-Location camrs-frontend; npm install
```

```powershell
npm run dev
```

The frontend will start on **http://localhost:5173**.

## Default Login Credentials

All seed users share the same password: `password123`

| Role        | Email               | Name           |
|-------------|---------------------|----------------|
| Admin Staff | admin@camrs.com     | Arjun Mehta    |
| Doctor      | doctor@camrs.com    | Dr. Priya Sharma |
| Lab Staff   | lab@camrs.com       | Neha Verma     |
| Patient 1   | patient@camrs.com   | Rahul Kumar    |
| Patient 2   | patient2@camrs.com  | Ananya Iyer    |

## Notes
- The Vite dev server proxies all `/api` requests to `http://localhost:8080`.
- Always start the **backend first**, then the frontend.
- If you get a PowerShell execution policy error with npm, use `cmd /c "npm run dev"` instead.
