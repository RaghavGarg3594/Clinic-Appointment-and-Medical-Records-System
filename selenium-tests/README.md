# CAMRS Selenium Test Suite

Automated end-to-end tests for the **Clinic Appointment & Medical Records System** using Selenium WebDriver + Python.

## Test Coverage — 30 Test Cases

| #    | Module                  | Tests | What's Tested |
|------|-------------------------|-------|---------------|
| 1–6  | Authentication          | 6     | Login for all 4 roles, invalid credentials, patient registration |
| 7    | Doctor Join Request     | 1     | Join request submission and success page |
| 8–14 | Admin Portal            | 7     | Dashboard stats, doctor management, inventory, billing, reports, audit logs |
| 15–18| Doctor Portal           | 4     | Dashboard, appointments, consultation, prescriptions |
| 19–24| Patient Portal          | 6     | Dashboard, booking form, doctor selection, profile edit, medical records, lab results |
| 25–26| Lab Staff Portal        | 2     | Lab dashboard, pending tests |
| 27–30| Security & RBAC         | 4     | Cross-role blocking, unauthenticated redirect, logout session clearing |

## Prerequisites

- **Python 3.8+**
- **Google Chrome** (or Chromium)
- **ChromeDriver** (auto-managed by `webdriver-manager`)
- CAMRS Backend running on `http://localhost:8080`
- CAMRS Frontend running on `http://localhost:5173`
- Database seeded via `camrs-full-setup.sql`

## Setup

```bash
cd selenium-tests
pip install -r requirements.txt
```

## Running Tests

```bash
# Run all tests with verbose output
pytest test_camrs.py -v --tb=short

# Run a specific test class
pytest test_camrs.py::TestAuthentication -v

# Run a single test
pytest test_camrs.py::TestAdminPortal::test_08_admin_dashboard_loads_with_heading -v

# Generate HTML report
pytest test_camrs.py -v --html=report.html --self-contained-html
```

## Configuration

Override default URLs via environment variables:

```bash
set CAMRS_FRONTEND_URL=http://localhost:5173
set CAMRS_BACKEND_URL=http://localhost:8080
pytest test_camrs.py -v
```

## Headless Mode

To run without a visible browser (CI/CD), uncomment in `conftest.py`:

```python
chrome_options.add_argument("--headless=new")
```

Or use the `headless_driver` fixture instead of `driver`.

## Test Accounts

| Role       | Email               | Password     |
|------------|---------------------|-------------|
| Admin      | admin@camrs.com     | password123 |
| Doctor     | doctor@camrs.com    | password123 |
| Patient    | patient@camrs.com   | password123 |
| Patient 2  | patient2@camrs.com  | password123 |
| Lab Staff  | lab@camrs.com       | password123 |

## File Structure

```
selenium-tests/
├── conftest.py        # Fixtures, login helpers, wait utilities
├── test_camrs.py      # 30 test cases organized by module
├── requirements.txt   # Python dependencies
├── screenshots/       # Auto-created for debugging screenshots
└── README.md          # This file
```
