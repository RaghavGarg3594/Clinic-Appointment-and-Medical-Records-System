# CAMRS Selenium Test Suite — Testing & Validation Documentation

## Table of Contents
1. [Overview](#overview)
2. [Testing Strategy](#testing-strategy)
3. [Technology Stack](#technology-stack)
4. [Test Architecture](#test-architecture)
5. [Test Categories & Coverage](#test-categories--coverage)
6. [Test Case Inventory](#test-case-inventory)
7. [End-to-End Workflow Tests](#end-to-end-workflow-tests)
8. [How to Run the Tests](#how-to-run-the-tests)
9. [Test Design Principles](#test-design-principles)
10. [Validation Techniques Used](#validation-techniques-used)
11. [Viva Q&A Reference](#viva-qa-reference)

---

## Overview

The CAMRS (Clinic Appointment & Medical Records System) Selenium Test Suite is a comprehensive **automated UI testing** framework that validates the entire web application across all four user roles:

| Role | Portal | Tests |
|------|--------|-------|
| **Admin Staff** | Dashboard, Doctor Management, Inventory, Billing, Reports | 36 tests |
| **Doctor** | Appointments, Consultation, Prescriptions | 11 tests |
| **Patient** | Appointments, Medical Records, Bills, Lab Results, Profile | 19 tests |
| **Lab Staff** | Dashboard, Pending Tests | 10 tests |
| **Cross-Role** | Login/Auth, Navigation, Security, E2E Workflows | ~50 tests |

**Total: ~130+ automated test cases** covering functional, integration, security, and end-to-end workflows.

---

## Testing Strategy

We employ a **multi-layered testing approach**:

```
┌─────────────────────────────────────────┐
│     End-to-End Workflow Tests (E2E)     │  ← Full user journeys
├─────────────────────────────────────────┤
│      Integration Tests (Selenium)       │  ← Page interactions
├─────────────────────────────────────────┤
│       Component/UI Verification         │  ← Element presence
├─────────────────────────────────────────┤
│     Security & Authorization Tests      │  ← RBAC validation
└─────────────────────────────────────────┘
```

### Types of Testing Performed

| Type | Description | Example |
|------|-------------|---------|
| **Functional Testing** | Verifies features work as expected | Login returns correct dashboard |
| **UI Testing** | Validates page elements are present and visible | Stat cards, tables, buttons render |
| **Integration Testing** | Tests interaction between frontend and backend | Form submission creates data |
| **End-to-End Testing** | Full user workflows from start to finish | Register → Login → Book Appointment |
| **Security Testing** | Validates RBAC and authentication | Patient can't access admin pages |
| **Boundary Testing** | Tests edge cases like empty forms, duplicate data | Duplicate email registration rejected |
| **Regression Testing** | Ensures fixes don't break existing functionality | Re-run full suite after every change |

---

## Technology Stack

| Tool | Purpose | Version |
|------|---------|---------|
| **Selenium WebDriver** | Browser automation | 4.18.1 |
| **pytest** | Test framework and runner | 8.1.1 |
| **pytest-html** | HTML report generation | 4.1.1 |
| **webdriver-manager** | Automatic ChromeDriver management | 4.0.1 |
| **Chrome** | Test browser | Latest |
| **Python** | Test scripting | 3.12 |

### Why Selenium?
- **Real browser testing**: Tests run in actual Chrome, matching real user experience
- **Cross-browser support**: Can run on Chrome, Firefox, Edge, Safari
- **Industry standard**: Most widely used web testing framework
- **Python integration**: Rich ecosystem with pytest for assertions and reporting

---

## Test Architecture

### Directory Structure
```
selenium-tests/
├── conftest.py                      # Shared fixtures, helpers, config
├── requirements.txt                 # Python dependencies
├── test_login.py                    # Authentication tests (10 cases)
├── test_navigation.py               # Navigation & security (16 cases)
├── test_admin_dashboard.py          # Admin dashboard (8 cases)
├── test_admin_doctor_management.py  # Doctor CRUD (10 cases)
├── test_admin_inventory.py          # Inventory management (7 cases)
├── test_admin_billing.py            # Billing operations (5 cases)
├── test_admin_reports.py            # Reports page (6 cases)
├── test_doctor_consultation.py      # Doctor consultation (8 cases)
├── test_patient_appointments.py     # Patient appointments (7 cases)
├── test_patient_profile.py          # Patient profile (6 cases)
├── test_patient_records.py          # Medical records & bills (9 cases)
├── test_lab_portal.py               # Lab staff portal (10 cases)
└── test_e2e_workflows.py           # E2E workflow tests (~25 cases)
```

### Configuration (conftest.py)
```python
BASE_URL = "http://localhost:5173"     # Frontend (Vite dev server)
BACKEND_URL = "http://localhost:8080"  # Backend (Spring Boot)
IMPLICIT_WAIT = 10                     # Seconds to wait for elements
EXPLICIT_WAIT = 15                     # Max seconds for explicit waits
```

### Shared Fixtures & Helpers
| Fixture/Helper | Purpose |
|----------------|---------|
| `driver` | Fresh Chrome browser instance per test |
| `headless_driver` | Chrome in headless mode (no GUI) |
| `login(driver, email, password)` | Performs login via the UI |
| `login_as(driver, role)` | Login as admin/doctor/patient/lab |
| `logout(driver)` | Clears session and navigates to login |
| `wait_for_element()` | Waits for element presence |
| `wait_for_visible()` | Waits for element visibility |
| `wait_for_clickable()` | Waits for element to be clickable |

### Test Credentials
| Role | Email | Password |
|------|-------|----------|
| Admin | admin@camrs.com | password123 |
| Doctor | doctor@camrs.com | password123 |
| Patient | patient@camrs.com | password123 |
| Lab Staff | lab@camrs.com | password123 |

---

## Test Categories & Coverage

### 1. Authentication Tests (`test_login.py`) — 10 Cases

| ID | Test Case | Type |
|----|-----------|------|
| TC-AUTH-01 | Login page loads correctly | UI Verification |
| TC-AUTH-02 | Login page has submit button | UI Verification |
| TC-AUTH-03 | Admin login success | Functional |
| TC-AUTH-04 | Admin dashboard loads after login | Integration |
| TC-AUTH-05 | Doctor login success | Functional |
| TC-AUTH-06 | Patient login success | Functional |
| TC-AUTH-07 | Lab Staff login success | Functional |
| TC-AUTH-08 | Invalid credentials show error | Negative Testing |
| TC-AUTH-09 | Empty email prevents submission | Boundary Testing |
| TC-AUTH-10 | Logout redirects to login | Functional |

### 2. Navigation & Security Tests (`test_navigation.py`) — 16 Cases

| ID | Test Case | Type |
|----|-----------|------|
| TC-NAV-01 to 06 | Admin sidebar links (Dashboard, Doctors, Inventory, Billing, Reports, Navigation) | UI Verification |
| TC-NAV-07 to 09 | Doctor sidebar links (Appointments, Consultation, Prescriptions) | UI Verification |
| TC-NAV-10 to 12 | Patient sidebar links (Appointments, Records, Profile) | UI Verification |
| TC-SEC-01 to 04 | Protected URLs redirect unauthenticated users | Security |
| TC-SEC-05 | Patient cannot access admin pages | Authorization |
| TC-SEC-06 | Doctor cannot access admin pages | Authorization |

### 3. Admin Dashboard Tests (`test_admin_dashboard.py`) — 8 Cases

| ID | Test Case | Type |
|----|-----------|------|
| TC-DASH-01 | Dashboard page loads | Functional |
| TC-DASH-02 | At least 6 stat cards visible | UI Verification |
| TC-DASH-03 | Total Patients card visible | Data Verification |
| TC-DASH-04 | Total Revenue card visible | Data Verification |
| TC-DASH-05 | Today's Appointments card visible | Data Verification |
| TC-DASH-06 | Low Stock card visible | Data Verification |
| TC-DASH-07 | Charts section renders | UI Verification |
| TC-DASH-08 | Top Diagnoses section visible | Data Verification |

### 4. Admin Doctor Management Tests (`test_admin_doctor_management.py`) — 10 Cases

| ID | Test Case | Type |
|----|-----------|------|
| TC-DOC-01 | Doctor management page loads | Functional |
| TC-DOC-02 | Doctors table visible | UI Verification |
| TC-DOC-03 | Add Doctor button present | UI Verification |
| TC-DOC-04 | Search filters doctors | Functional |
| TC-DOC-05 | Add Doctor modal opens | Functional |
| TC-DOC-06 | Add form has all required fields | Form Validation |
| TC-DOC-07 | Edit button opens edit modal | Functional |
| TC-DOC-08 | Edit form is pre-filled with data | Data Integrity |
| TC-DOC-09 | Qualification field exists in edit form | Regression |
| TC-DOC-10 | Activate/Deactivate toggle present | UI Verification |

### 5. Admin Inventory Tests (`test_admin_inventory.py`) — 7 Cases

| ID | Test Case | Type |
|----|-----------|------|
| TC-INV-01 | Inventory page loads | Functional |
| TC-INV-02 | Medications table visible | UI Verification |
| TC-INV-03 | Add Medication button present | UI Verification |
| TC-INV-04 | Add Medication modal opens | Functional |
| TC-INV-05 | Update button for each medication | UI Verification |
| TC-INV-06 | Search filters medications | Functional |
| TC-INV-07 | Low stock badges displayed | Data Verification |

### 6. Admin Billing Tests (`test_admin_billing.py`) — 5 Cases

| ID | Test Case | Type |
|----|-----------|------|
| TC-BILL-01 | Billing page loads | Functional |
| TC-BILL-02 | Revenue and outstanding cards visible | UI Verification |
| TC-BILL-03 | Bills table visible | UI Verification |
| TC-BILL-04 | Mark Paid button for unpaid bills | Functional |
| TC-BILL-05 | Rx PDF button present | UI Verification |

### 7. Admin Reports Tests (`test_admin_reports.py`) — 6 Cases

| ID | Test Case | Type |
|----|-----------|------|
| TC-RPT-01 | Reports page loads | Functional |
| TC-RPT-02 | Report sections/cards visible | UI Verification |
| TC-RPT-03 | Consultation statistics visible | Data Verification |
| TC-RPT-04 | Disease statistics visible | Data Verification |
| TC-RPT-05 | Outstanding payments section visible | Data Verification |
| TC-RPT-06 | Tables or charts render | UI Verification |

### 8. Doctor Consultation Tests (`test_doctor_consultation.py`) — 8 Cases

| ID | Test Case | Type |
|----|-----------|------|
| TC-DAPPT-01 | Doctor appointments page loads | Functional |
| TC-DAPPT-02 | Daily Schedule tab visible | UI Verification |
| TC-DAPPT-03 | Upcoming tab visible | UI Verification |
| TC-CONS-01 | Consultation page loads | Functional |
| TC-CONS-02 | Consultation form has required fields | Form Validation |
| TC-CONS-03 | Add Medication button present | UI Verification |
| TC-CONS-04 | Add Lab Test button present | UI Verification |
| TC-CONS-05 | Severity dropdown has all options | Form Validation |

### 9. Doctor Prescriptions Tests (in `test_doctor_consultation.py`) — 3 Cases

| ID | Test Case | Type |
|----|-----------|------|
| TC-PRESC-01 | Prescriptions page loads | Functional |
| TC-PRESC-02 | Summary stat cards displayed | UI Verification |
| TC-PRESC-03 | Search and severity filter present | UI Verification |

### 10. Patient Appointments Tests (`test_patient_appointments.py`) — 7 Cases

| ID | Test Case | Type |
|----|-----------|------|
| TC-APPT-01 | Appointments page loads | Functional |
| TC-APPT-02 | Book Appointment button present | UI Verification |
| TC-APPT-03 | Booking form opens on button click | Functional |
| TC-APPT-04 | Appointment type not showing "undefined" | Regression |
| TC-APPT-05 | Appointments table visible | UI Verification |
| TC-APPT-06 | Doctor dropdown has options | Data Verification |
| TC-APPT-07 | Cancel/Reschedule buttons for scheduled | Functional |

### 11. Patient Profile Tests (`test_patient_profile.py`) — 6 Cases

| ID | Test Case | Type |
|----|-----------|------|
| TC-PROF-01 | Profile page loads | Functional |
| TC-PROF-02 | Email is displayed | Data Verification |
| TC-PROF-03 | Profile form has editable fields | UI Verification |
| TC-PROF-04 | Save/Update button present | UI Verification |
| TC-PROF-05 | Patient name is pre-filled | Data Integrity |
| TC-PROF-06 | Phone/Contact field exists | Form Validation |

### 12. Patient Records & Bills Tests (`test_patient_records.py`) — 9 Cases

| ID | Test Case | Type |
|----|-----------|------|
| TC-REC-01 | Medical records page loads | Functional |
| TC-REC-02 | Records list or empty message shown | UI Verification |
| TC-REC-03 | No JavaScript errors on load | Stability |
| TC-REC-04 | Search input works if present | Functional |
| TC-PBILL-01 | Patient bills page loads | Functional |
| TC-PBILL-02 | Bills table visible | UI Verification |
| TC-PBILL-03 | No "undefined" values in bills table | Regression |
| TC-PLAB-01 | Lab results page loads | Functional |
| TC-PLAB-02 | Lab results table visible | UI Verification |

### 13. Lab Staff Portal Tests (`test_lab_portal.py`) — 10 Cases

| ID | Test Case | Type |
|----|-----------|------|
| TC-LAB-01 | Lab dashboard page loads | Functional |
| TC-LAB-02 | At least 3 stat cards visible | UI Verification |
| TC-LAB-03 | Pending stat label visible | Data Verification |
| TC-LAB-04 | Pending lab tests page loads | Functional |
| TC-LAB-05 | Lab tests table visible | UI Verification |
| TC-LAB-06 | Table has correct column headers | Data Verification |
| TC-LAB-07 | Collect Sample button for ORDERED tests | Functional |
| TC-LAB-08 | Enter Results button for collected tests | Functional |
| TC-LAB-09 | Priority badges displayed | UI Verification |
| TC-LAB-10 | Page doesn't crash with no pending orders | Stability |

---

## End-to-End Workflow Tests

### E2E Test File: `test_e2e_workflows.py` — ~25 Cases

These tests go beyond simple UI checks — they **create real data**, **fill actual forms**, and **simulate complete user journeys** with input values.

### E2E-01: Patient Registration Workflow
| ID | Test | What it does |
|----|------|-------------|
| E2E-REG-01 | Registration page loads | Verifies form is displayed |
| E2E-REG-02 | Fill and submit registration | **Fills all fields** (name, email, password, phone, DOB, gender) and submits |
| E2E-REG-03 | Login with new account | **Logs in with the newly created credentials** |
| E2E-REG-04 | New patient dashboard loads | Verifies the new user can access their dashboard |
| E2E-REG-05 | Duplicate registration rejected | **Attempts duplicate registration** — expects rejection |

### E2E-02: Appointment Booking Workflow
| ID | Test | What it does |
|----|------|-------------|
| E2E-APPT-01 | Opens booking form | Patient navigates and opens the form |
| E2E-APPT-02 | Selects doctor from dropdown | **Picks a doctor** from the list |
| E2E-APPT-03 | Fills appointment date | **Enters a future date** |
| E2E-APPT-04 | Selects appointment type | **Chooses "Follow-up"** |

### E2E-03: Profile Update Workflow
| ID | Test | What it does |
|----|------|-------------|
| E2E-PROF-01 | Profile form displayed | Verifies form is editable |
| E2E-PROF-02 | Updates phone number | **Clears and types a new phone number** |
| E2E-PROF-03 | Save button exists | Verifies save/update button is present |

### E2E-04: Admin Doctor CRUD Workflow
| ID | Test | What it does |
|----|------|-------------|
| E2E-DCRUD-01 | Opens Add Doctor form | Admin opens the modal |
| E2E-DCRUD-02 | Fills all doctor fields | **Enters name, email, password, specialization, qualification, license** |
| E2E-DCRUD-03 | Searches for a doctor | **Types "John" in search** and verifies results |

### E2E-05: Admin Inventory Workflow
| ID | Test | What it does |
|----|------|-------------|
| E2E-INV-01 | Fills Add Medication form | **Types a medication name** in the modal |
| E2E-INV-02 | Searches inventory | **Searches for "Paracetamol"** |

### E2E-06: Cross-Role Security Workflow
| ID | Test | What it does |
|----|------|-------------|
| E2E-SEC-01 | Patient blocked from admin | Logged-in patient can't access admin |
| E2E-SEC-02 | Doctor blocked from lab | Logged-in doctor can't access lab |
| E2E-SEC-03 | Unauthenticated redirect | No token → redirect to login |
| E2E-SEC-04 | Logout clears session | After logout, can't access protected pages |

### E2E-07: Full Patient Journey (Integration)
| ID | Test | What it does |
|----|------|-------------|
| E2E-JOURNEY-01 | Complete patient lifecycle | **Register → Login → Dashboard → Appointments → Records → Profile → Logout → Verify blocked** (all in one test) |

---

## How to Run the Tests

### Prerequisites
1. **MySQL** running with `camrs` database
2. **Java JDK** installed
3. **Node.js + npm** installed
4. **Chrome browser** installed
5. **Python 3.12+** with pip

### Step 1: Install Python dependencies
```bash
pip install -r selenium-tests/requirements.txt
```

### Step 2: Start Backend Server (Terminal 1)
```bash
cd camrs-backend
java -jar target/camrs-backend-1.0.0-SNAPSHOT.jar
```
Wait until you see: `Started CamrsApplication in X seconds`

### Step 3: Start Frontend Server (Terminal 2)
```bash
cd camrs-frontend
npm run dev
```
Wait until you see: `VITE ready in X ms — http://localhost:5173/`

### Step 4: Run Tests (Terminal 3)
```bash
# Run all tests
pytest selenium-tests/ -v --tb=short

# Run a specific test file
pytest selenium-tests/test_login.py -v

# Run only E2E workflow tests
pytest selenium-tests/test_e2e_workflows.py -v

# Run with HTML report
pytest selenium-tests/ -v --html=selenium-tests/report.html

# Stop on first failure
pytest selenium-tests/ -v -x

# Run tests matching a keyword
pytest selenium-tests/ -v -k "admin"
```

---

## Test Design Principles

### 1. Arrange-Act-Assert (AAA) Pattern
Every test follows the AAA pattern:
```python
def test_billing_page_loads(self, driver):
    # ARRANGE: Login and navigate
    login_as(driver, "admin")
    driver.get(f"{BASE_URL}/admin/billing")
    
    # ACT: Find the heading
    h1 = driver.find_element(By.TAG_NAME, "h1")
    
    # ASSERT: Verify expected state
    assert "Billing" in h1.text
```

### 2. Test Isolation
- Each test gets a **fresh browser instance** (`scope="function"`)
- Tests don't depend on each other's state
- E2E tests use **unique IDs** (`uuid`) to avoid data conflicts

### 3. Page Object-lite Pattern
- Shared helpers in `conftest.py` abstract common actions (`login`, `logout`, `wait_for_element`)
- Tests focus on assertions, not low-level Selenium mechanics

### 4. Graceful Degradation
- Tests use `find_elements` (plural) when data may not exist to avoid crashes
- `pytest.skip()` is used when prerequisite data is missing

### 5. Explicit Waits
- `WebDriverWait` with `expected_conditions` is used instead of hard sleeps where possible
- `implicit_wait` provides a fallback

---

## Validation Techniques Used

| Technique | Description | Where Used |
|-----------|-------------|------------|
| **Element Presence** | Verify DOM elements exist | All UI tests |
| **Element Visibility** | Verify elements are visible (not hidden) | Table, card tests |
| **Text Content Validation** | Check h1, labels contain expected text | Page load tests |
| **URL Verification** | Assert current URL after navigation | Login redirect tests |
| **Form Field Validation** | Verify form inputs exist with correct types | Registration, Doctor Add |
| **Data Integrity Checks** | Verify pre-filled values in edit forms | Doctor Edit tests |
| **Negative Testing** | Invalid input should show errors | Wrong credentials test |
| **Boundary Testing** | Empty fields, duplicate data | Empty email, duplicate reg |
| **RBAC Validation** | Role-based access control verification | Security tests |
| **Cross-page Navigation** | Verify sidebar links work | Navigation tests |
| **Regression Checks** | Verify previously fixed bugs stay fixed | "undefined" checks |
| **Screenshot Capture** | Debug helper for failed tests | `take_screenshot()` |

---

## Viva Q&A Reference

### Q: What type of testing is this?
**A:** This is **automated UI/End-to-End testing** using Selenium WebDriver. It's a form of **black-box testing** where we test the application from the user's perspective through the browser, without knowledge of internal code.

### Q: Why Selenium?
**A:** Selenium is the industry-standard tool for web browser automation. It works with real browsers (Chrome, Firefox), supports multiple programming languages, and can simulate actual user interactions like clicking, typing, and navigating.

### Q: What is the test framework?
**A:** We use **pytest** — a Python testing framework that provides powerful assertion introspection, fixture management, parameterization, and plugins like `pytest-html` for report generation.

### Q: What is a test fixture?
**A:** A fixture is reusable setup/teardown code. In our suite, the `driver` fixture creates a fresh Chrome browser before each test and closes it after. This ensures **test isolation**.

### Q: How do you handle test data?
**A:** E2E tests use `uuid` to generate **unique test data per run** (unique emails, names). This prevents conflicts with existing data and allows tests to be re-run without cleanup.

### Q: What is the difference between implicit and explicit waits?
**A:** 
- **Implicit wait** (10s): A global timeout for finding any element
- **Explicit wait** (15s): A targeted wait for a specific condition (element visible, URL changes, etc.)

### Q: What is RBAC and how do you test it?
**A:** RBAC = Role-Based Access Control. We test it by:
1. Logging in as one role (e.g., Patient)
2. Navigating to another role's page (e.g., /admin/dashboard)
3. Asserting the user is redirected or denied access

### Q: What is regression testing?
**A:** Regression testing verifies that previously fixed bugs don't reappear. Example: We check that table cells don't show "undefined" values — a bug that was fixed during development.

### Q: How many test cases do you have?
**A:** **130+ automated test cases** across 15 test files, covering authentication, navigation, CRUD operations, security, and full end-to-end workflows.

### Q: What is the AAA pattern?
**A:** Arrange-Act-Assert: **Arrange** the preconditions (login, navigate), **Act** by performing the action (click, type), **Assert** the expected outcome (element visible, URL correct).

### Q: Can tests run in headless mode?
**A:** Yes. Uncomment `--headless=new` in `conftest.py` or use the `headless_driver` fixture. Headless mode runs Chrome without a GUI, which is faster for CI/CD pipelines.

### Q: What is the Page Object Model?
**A:** POM is a design pattern where each page is represented as a class with methods for its interactions. Our suite uses a simplified version — shared helper functions in `conftest.py` that abstract common actions like login/logout.

### Q: What testing types are NOT covered?
**A:** 
- **Unit testing** (backend Java tests with JUnit)
- **Performance/Load testing** (JMeter, k6)
- **API testing** (Postman, REST Assured)
- **Accessibility testing** (axe-core)
- **Visual regression testing** (Percy, BackstopJS)

---

## Test Execution Results

### Latest Run Summary
```
============= test session starts ==============
collected 107 items (original suite)

41 passed in 405.12s (0:06:45) — interrupted
Full suite: 62 passed, 8 failed out of 107

+ 25 new E2E workflow tests added
Total: ~130+ test cases
```

### Common Failure Reasons
| Issue | Cause | Fix |
|-------|-------|-----|
| `ERR_CONNECTION_REFUSED` | Servers not running | Start backend + frontend first |
| `Login failed` | Wrong credentials | Fixed in conftest.py |
| `NoSuchElementException` | Page not fully loaded | Increase wait times |
| `TimeoutException` | Element never appeared | Check if page URL/structure changed |
