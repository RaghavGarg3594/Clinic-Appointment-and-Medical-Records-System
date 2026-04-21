# ============================================================
#  CAMRS — Run Commands (Windows / VS Code Terminal)
# ============================================================
#
#  HOW TO USE:
#    1. Open VS Code in the project root folder (CAMRS_Swe)
#    2. Open a Terminal (Ctrl + `)
#    3. Copy-paste each STEP one at a time, in order
#    4. You need TWO terminals:
#         Terminal 1 → Steps 1–5 (Database + Backend)
#         Terminal 2 → Steps 6–8 (Frontend)
#
# ============================================================


# ─────────────────────────────────────────────────────────────
#  STEP 1: FIX POWERSHELL EXECUTION POLICY (run once, ever)
# ─────────────────────────────────────────────────────────────

Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned -Force


# ─────────────────────────────────────────────────────────────
#  STEP 2: REFRESH PATH (so node/npm are found)
# ─────────────────────────────────────────────────────────────

$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")


# ─────────────────────────────────────────────────────────────
#  STEP 3: CREATE DATABASE
#  (will prompt for MySQL password — type it and press Enter)
# ─────────────────────────────────────────────────────────────

& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "DROP DATABASE IF EXISTS camrs; CREATE DATABASE camrs;"


# ─────────────────────────────────────────────────────────────
#  STEP 4: BUILD BACKEND JAR (~1-2 minutes)
#  NOTE: Run this from the project root folder (CAMRS_Swe)
# ─────────────────────────────────────────────────────────────

& ".\maven\apache-maven-3.9.6\bin\mvn.cmd" -f ".\camrs-backend\pom.xml" clean package -DskipTests


# ─────────────────────────────────────────────────────────────
#  STEP 5: START BACKEND (keeps running — don't close this terminal)
#  *** REPLACE 'YOUR_MYSQL_PASSWORD' WITH YOUR ACTUAL PASSWORD ***
# ─────────────────────────────────────────────────────────────

java -DDB_PASS='Raghav@163594' -jar ".\camrs-backend\target\camrs-backend-1.0.0-SNAPSHOT.jar"


# ─────────────────────────────────────────────────────────────
#  >> WAIT until you see "Started CamrsApplication" in the output
#  >> Then OPEN A NEW TERMINAL (Terminal 2) and continue below
# ─────────────────────────────────────────────────────────────


# ═════════════════════════════════════════════════════════════
#  >>>>>>>  OPEN A NEW TERMINAL (Terminal 2)  <<<<<<<
# ═════════════════════════════════════════════════════════════


# ─────────────────────────────────────────────────────────────
#  STEP 6: REFRESH PATH (Terminal 2)
# ─────────────────────────────────────────────────────────────

$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")


# ─────────────────────────────────────────────────────────────
#  STEP 7: INSTALL FRONTEND DEPENDENCIES (Terminal 2)
# ─────────────────────────────────────────────────────────────

Set-Location ".\camrs-frontend"; npm install


# ─────────────────────────────────────────────────────────────
#  STEP 8: START FRONTEND (Terminal 2 — keeps running)
# ─────────────────────────────────────────────────────────────

npm run dev


# ─────────────────────────────────────────────────────────────
#  STEP 9: OPEN IN BROWSER
# ─────────────────────────────────────────────────────────────
#
#  Go to:  http://localhost:5173/login
#
#  Login with any of these (password for ALL: password123):
#
#    admin@camrs.com      → Admin Dashboard
#    doctor@camrs.com     → Doctor Dashboard
#    lab@camrs.com        → Lab Dashboard
#    patient@camrs.com    → Patient Dashboard
#    patient2@camrs.com   → Patient Dashboard
#
# ─────────────────────────────────────────────────────────────
