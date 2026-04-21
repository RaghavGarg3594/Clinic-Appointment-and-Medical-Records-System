# CAMRS — Project Demo Script

> **Group 29 | Clinic Appointment and Medical Records System**
> Estimated demo time: **15–20 minutes**

---

## Pre-Demo Checklist (do this 30 min before)

1. **Start MySQL** — make sure the `MySQL80` Windows service is running.
2. **Seed the database** (if fresh):
   ```powershell
   & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < camrs-full-setup.sql
   ```
3. **Build & start backend** (Terminal 1):
   ```powershell
   & ".\maven\apache-maven-3.9.6\bin\mvn.cmd" -f ".\camrs-backend\pom.xml" clean package -DskipTests
   java -DDB_PASS='YOUR_PASSWORD' -jar ".\camrs-backend\target\camrs-backend-1.0.0-SNAPSHOT.jar"
   ```
   Wait for `Started CamrsApplication`.
4. **Start frontend** (Terminal 2):
   ```powershell
   cd camrs-frontend
   npm run dev
   ```
5. Open **http://localhost:5173/login** in Chrome.
6. Keep a **notepad** open with the login credentials table (below).

### Login Credentials (password for all: `password123`)

| Role | Email | Name |
|------|-------|------|
| Admin | admin@camrs.com | Arjun Mehta |
| Doctor | doctor@camrs.com | Dr. Priya Sharma |
| Lab Staff | lab@camrs.com | Neha Verma |
| Patient 1 | patient@camrs.com | Rahul Kumar |
| Patient 2 | patient2@camrs.com | Ananya Iyer |

---

## Demo Flow (Step-by-Step)

### Part 1 — Introduction (1 min)
- Open the login page. Briefly explain:
  - "CAMRS is a full-stack clinic management system with 4 user roles."
  - "Built with Spring Boot + React + MySQL."
  - "Role-based JWT authentication. Every page is protected."

---

### Part 2 — Patient Registration (2 min)
**Goal:** Show UC1 — Register Patient

1. Click **"Register here"** on the login page.
2. Fill in the form:
   - Name: `Demo Patient`
   - Email: `demo@camrs.com`
   - Password: `password123`
   - Phone: `9876543210`
   - DOB: pick a past date
   - Gender: Male
3. **Highlight the new fields:**
   - Medical History → type `Asthma`
   - Allergies → type `Penicillin, Aspirin`
   - Insurance Details → type `Star Health - POL123`
4. Click **Register Now**.
5. **Talking point:** "The system validates email format, 10-digit phone, and ensures DOB is in the past — both client-side and server-side."
6. You'll be redirected to login. Log in with `demo@camrs.com`.

---

### Part 3 — Patient Portal (3 min)
**Goal:** Show UC3, UC4, UC5, UC6

1. **Dashboard** — point out the personalized greeting "Welcome, Demo Patient" and the stat cards.
2. **My Appointments** → Click **Book Appointment**.
   - Select a doctor, pick today's date.
   - **Talking point:** "Notice only future time slots are shown. Past slots on the current day are automatically filtered out."
   - Confirm booking.
3. **Profile** → Click **Edit Profile**.
   - Show the Medical History, Allergies, Insurance fields are pre-filled from registration.
   - Edit phone to an invalid number (e.g., `123`) → show validation error.
4. **Medical Records** → "Currently empty — records appear after a doctor consultation."
5. **Lab Results** → "Currently empty — results appear after lab processing."
6. **My Bills** → "Patients can view bills but cannot process payments — admin only."

---

### Part 4 — Admin Portal (3 min)
**Goal:** Show UC12 + Admin functions

1. **Log out**, log in as **admin@camrs.com**.
2. **Dashboard** — point out the stat cards (total patients, doctors, appointments, revenue).
3. **Doctor Requests** →
   - Show the dropdown: only **Pending / Approved / Rejected** (we removed Reviewed & Contacted).
   - If there's a pending request, approve it → show the credentials dialog.
4. **Lab Staff** (new page!) →
   - Show the table of lab staff.
   - Click **+ Add Lab Staff** → fill a quick form → submit.
   - **Talking point:** "Admins can now onboard Lab Staff directly from the portal."
5. **Inventory** → show the medication list with stock levels.
   - **Talking point:** "The database comes pre-seeded with 20+ common medications like Paracetamol, Metformin, Amoxicillin."
6. **Billing** → show the admin billing view with payment processing.

---

### Part 5 — Doctor Portal — Consultation (4 min) ⭐ KEY DEMO
**Goal:** Show UC7, UC8, UC9, UC10

1. **Log out**, log in as **doctor@camrs.com**.
2. **Appointments** → switch to "All Appointments" tab → find the demo patient's booking.
3. Click **Start Consultation** → the inline dialog opens.
4. Fill in:
   - Chief Complaint: `Persistent cough and fever`
   - Vital Signs: `BP 120/80, Temp 101°F`
   - **Diagnosis (ICD-10):** Start typing `fever` → show the **dropdown autocomplete** with ICD-10 codes → select one.
   - **Talking point:** "Diagnosis uses standardized ICD-10 codes with a live search, directly fulfilling FR-MR4 from the SRS."
   - Severity: Moderate
   - Advice: `Rest, fluids, follow-up in 5 days`
5. **Add Medication:**
   - Select **Amoxicillin** → Twice daily → 5 days → After meals.
   - **Watch what happens** → Because the demo patient has "Penicillin" in their allergies, and Amoxicillin is from the penicillin family:
   - Click **Complete Consultation** → the **⚠ Allergy Warning dialog** appears!
   - **Talking point:** "The system cross-references prescribed medications against the patient's recorded allergies and shows a soft warning. The doctor can proceed or go back and edit."
   - Click **Proceed Anyway**.
6. **Add a Lab Test** (before submitting, or show Order Lab Test separately):
   - Select CBC, Priority: Routine.
7. Consultation completes → show the success message.
8. Navigate to **Prescriptions** → show the generated prescription with download PDF button.

---

### Part 6 — Lab Staff Portal (2 min)
**Goal:** Show UC11

1. **Log out**, log in as **lab@camrs.com**.
2. **Pending Tests** → find the CBC order for the demo patient.
3. Click **Enter Results**:
   - Result Value: `18` (for Hemoglobin, if reference is 13-17 g/dL → this would be HIGH)
   - Unit: `g/dL`
4. Submit.
5. **Talking point:** "The system auto-flags results as HIGH/LOW/NORMAL by parsing the reference range. Doctors and patients see color-coded badges."

---

### Part 7 — Patient Views Results (1 min)
**Goal:** Close the loop

1. **Log out**, log in as **demo@camrs.com** (or patient@camrs.com).
2. **Lab Results** → show the result with a red **HIGH ↑** badge.
3. **Medical Records** → show the consultation record with ICD-10 diagnosis.
4. **My Bills** → show the auto-generated bill.

---

### Part 8 — Security & RBAC (1 min)

1. While logged in as patient, manually navigate to `/admin/dashboard` → show it redirects back (access denied).
2. **Talking point:** "Every route is protected by role-based access on both frontend (React router guards) and backend (Spring Security `@PreAuthorize`)."

---

### Part 9 — Testing (1 min)
**Goal:** Show quality assurance

1. Briefly show the `selenium-tests/test_camrs.py` file in VS Code.
2. **Talking point:** "We have 30+ automated Selenium test cases covering authentication, all 4 portals, RBAC security, and form submissions."
3. Optionally run: `pytest selenium-tests/test_camrs.py -v --tb=short` (if time allows).

---

### Part 10 — Wrap-Up (1 min)

**Summary slide points:**
- 4 user roles with full RBAC
- Complete clinical workflow: Registration → Appointment → Consultation → Prescription → Lab → Billing
- ICD-10 standardized diagnosis codes
- Allergy-checked prescriptions (soft warning system)
- Auto-flagged lab results (HIGH/LOW/NORMAL)
- PDF generation for prescriptions and lab reports
- 30+ automated Selenium test cases
- Pre-seeded database with realistic clinical data

**If asked about missing features**, refer to `missing_features.md` and explain:
- Email/SMS notifications, payment gateway, and advanced reporting are scoped for Release 2.
- The current system covers all 12 Use Cases from the Use Case Document.

---

## Emergency Talking Points (if evaluator asks)

| Question | Answer |
|----------|--------|
| "How do you handle security?" | JWT tokens, bcrypt password hashing, role-based `@PreAuthorize` on every endpoint, React route guards |
| "What about data validation?" | Client-side (regex, required fields) + server-side validation (email format, 10-digit phone, past DOB) |
| "How are prescriptions safe?" | Allergy cross-checking against patient profile before dispensing |
| "What database do you use?" | MySQL 8.0 with JPA/Hibernate auto-schema + manual seed script |
| "How do you test?" | Selenium browser tests (30+ cases), unit testing strategy documented |
| "What's the tech stack?" | Spring Boot 3.2 + React 18 + Vite + ShadCN UI + Tailwind CSS + MySQL |
