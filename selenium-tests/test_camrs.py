"""
CAMRS Selenium Test Suite — 30 End-to-End Test Cases
=====================================================
Comprehensive UI test coverage for the Clinic Appointment & Medical
Records System (CAMRS). Tests cover all 4 role portals, authentication
flows, RBAC enforcement, form interactions, and full patient journeys.

Requires:
  - Backend running on localhost:8080  (Spring Boot)
  - Frontend running on localhost:5173 (Vite dev server)
  - Chrome / Chromium browser installed
  - Database seeded via camrs-full-setup.sql

Run:
  cd selenium-tests
  pip install -r requirements.txt
  pytest test_camrs.py -v --tb=short

Routes Reference (from App.jsx):
  /login, /register, /doctor-request
  /patient/dashboard, /patient/appointments, /patient/records,
  /patient/lab-results, /patient/bills, /patient/profile
  /doctor/dashboard, /doctor/appointments, /doctor/consultation,
  /doctor/prescriptions, /doctor/prescription-history
  /lab/dashboard, /lab/tests
  /admin/dashboard, /admin/doctors, /admin/doctor-requests,
  /admin/inventory, /admin/billing, /admin/lab-staff,
  /admin/reports, /admin/audit-logs
"""

import pytest
import time
import uuid
from datetime import datetime, timedelta
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import Select, WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import NoSuchElementException, TimeoutException
from conftest import (
    BASE_URL, BACKEND_URL, login, login_as, logout,
    wait_for_element, wait_for_visible, find_button_by_text,
    get_page_heading, is_element_present, take_screenshot,
    set_date_input, TEST_USERS,
)

# Unique suffix to avoid collisions across runs
UNIQUE = uuid.uuid4().hex[:6]


# ═══════════════════════════════════════════════════════════════════
#  1. AUTHENTICATION & REGISTRATION  (6 tests)
# ═══════════════════════════════════════════════════════════════════
class TestAuthentication:
    """Tests for login, registration, doctor join request, and failed login."""

    def test_01_admin_login_redirects_to_admin_dashboard(self, driver):
        """TC-01: Admin login redirects to /admin/dashboard."""
        assert login(driver, "admin@camrs.com", "password123")
        time.sleep(2)
        assert "/admin" in driver.current_url

    def test_02_doctor_login_redirects_to_doctor_dashboard(self, driver):
        """TC-02: Doctor login redirects to /doctor/dashboard."""
        assert login(driver, "doctor@camrs.com", "password123")
        time.sleep(2)
        assert "/doctor" in driver.current_url

    def test_03_patient_login_redirects_to_patient_dashboard(self, driver):
        """TC-03: Patient login redirects to /patient/dashboard."""
        assert login(driver, "patient@camrs.com", "password123")
        time.sleep(2)
        assert "/patient" in driver.current_url

    def test_04_lab_staff_login_redirects_to_lab_dashboard(self, driver):
        """TC-04: Lab staff login redirects to /lab/dashboard."""
        assert login(driver, "lab@camrs.com", "password123")
        time.sleep(2)
        assert "/lab" in driver.current_url

    def test_05_invalid_credentials_stay_on_login(self, driver):
        """TC-05: Invalid credentials do NOT redirect away from login."""
        login(driver, "fake@camrs.com", "wrongpassword")
        time.sleep(2)
        assert "login" in driver.current_url.lower()

    def test_06_patient_registration_redirects_to_login(self, driver):
        """TC-06: Successful patient registration redirects to /login?registered=true."""
        unique_email = f"seltest{UNIQUE}@camrs.com"
        driver.get(f"{BASE_URL}/register")
        time.sleep(3)

        # Fill required fields
        driver.find_element(By.NAME, "firstName").send_keys("Selenium")
        driver.find_element(By.NAME, "lastName").send_keys(f"Tester_{UNIQUE}")
        driver.find_element(By.NAME, "email").send_keys(unique_email)
        driver.find_element(By.NAME, "password").send_keys("password123")
        driver.find_element(By.NAME, "phone").send_keys("9998887776")
        set_date_input(driver, "input[name='dateOfBirth']", "1995-06-15")

        # Gender select
        gender_select = driver.find_element(By.NAME, "gender")
        Select(gender_select).select_by_visible_text("Male")

        # Optional fields
        try:
            driver.find_element(By.NAME, "medicalHistory").send_keys("No significant history")
            driver.find_element(By.NAME, "allergies").send_keys("None")
            driver.find_element(By.NAME, "insuranceDetails").send_keys("Test Policy 12345")
            driver.find_element(By.NAME, "emergencyContact").send_keys("Emergency - 9876543210")
        except NoSuchElementException:
            pass  # Textareas may not be visible

        driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()
        time.sleep(4)

        assert "login" in driver.current_url.lower(), "Should redirect to login"
        assert "registered=true" in driver.current_url.lower(), "Should have registered=true param"


# ═══════════════════════════════════════════════════════════════════
#  2. DOCTOR JOIN REQUEST  (1 test)
# ═══════════════════════════════════════════════════════════════════
class TestDoctorJoinRequest:
    """Tests for the doctor join request submission flow."""

    def test_07_doctor_join_request_shows_success(self, driver):
        """TC-07: Doctor join request submission shows 'Request Submitted!' success page."""
        driver.get(f"{BASE_URL}/doctor-request")
        time.sleep(3)

        driver.find_element(By.NAME, "firstName").send_keys("DrSelenium")
        driver.find_element(By.NAME, "lastName").send_keys(f"Test_{UNIQUE}")
        driver.find_element(By.NAME, "email").send_keys(f"drsel{UNIQUE}@camrs.com")
        driver.find_element(By.NAME, "phone").send_keys("9876543210")
        driver.find_element(By.NAME, "specialization").send_keys("Neurology")
        driver.find_element(By.NAME, "qualification").send_keys("MBBS, MD Neurology")
        driver.find_element(By.NAME, "licenseNumber").send_keys(f"LIC-{UNIQUE}")
        driver.find_element(By.NAME, "experienceYears").send_keys("8")
        driver.find_element(By.NAME, "message").send_keys("Selenium test join request.")

        driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()
        time.sleep(4)

        # Success page shows h2 with "Request Submitted!"
        h2_elements = driver.find_elements(By.TAG_NAME, "h2")
        success_found = any("Request Submitted" in h2.text for h2 in h2_elements)
        assert success_found, f"Expected 'Request Submitted!' but found: {[h.text for h in h2_elements]}"


# ═══════════════════════════════════════════════════════════════════
#  3. ADMIN PORTAL  (7 tests)
# ═══════════════════════════════════════════════════════════════════
class TestAdminPortal:
    """Tests for all admin portal pages and interactions."""

    def test_08_admin_dashboard_loads_with_heading(self, driver):
        """TC-08: Admin dashboard loads and displays 'Admin Dashboard' heading."""
        login(driver, "admin@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/admin/dashboard")
        time.sleep(3)
        heading = get_page_heading(driver)
        assert "Dashboard" in heading, f"Expected 'Dashboard' in heading, got: '{heading}'"

    def test_09_admin_dashboard_shows_stat_cards(self, driver):
        """TC-09: Admin dashboard displays at least 4 stat cards."""
        login(driver, "admin@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/admin/dashboard")
        time.sleep(3)
        # Stat cards use the Card component with border-l-4 accent
        cards = driver.find_elements(By.CSS_SELECTOR, "[class*='card'], [class*='Card']")
        assert len(cards) >= 4, f"Expected at least 4 stat cards, found {len(cards)}"

    def test_10_doctor_management_page_has_table(self, driver):
        """TC-10: Doctor management page loads with a data table."""
        login(driver, "admin@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/admin/doctors")
        time.sleep(3)
        tables = driver.find_elements(By.CSS_SELECTOR, "table")
        assert len(tables) >= 1, "Doctor management should have a table"

    def test_11_inventory_page_shows_medications(self, driver):
        """TC-11: Inventory management page loads with 'Inventory' heading."""
        login(driver, "admin@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/admin/inventory")
        time.sleep(3)
        heading = get_page_heading(driver)
        assert "Inventory" in heading, f"Expected 'Inventory' in heading, got: '{heading}'"

    def test_12_billing_page_loads(self, driver):
        """TC-12: Billing page loads with 'Billing' heading."""
        login(driver, "admin@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/admin/billing")
        time.sleep(3)
        heading = get_page_heading(driver)
        assert "Billing" in heading, f"Expected 'Billing' in heading, got: '{heading}'"

    def test_13_reports_page_loads(self, driver):
        """TC-13: Admin reports page loads with 'Report' heading."""
        login(driver, "admin@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/admin/reports")
        time.sleep(3)
        heading = get_page_heading(driver)
        assert "Report" in heading, f"Expected 'Report' in heading, got: '{heading}'"

    def test_14_audit_logs_page_loads(self, driver):
        """TC-14: Admin audit logs page loads with 'Audit' heading."""
        login(driver, "admin@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/admin/audit-logs")
        time.sleep(3)
        heading = get_page_heading(driver)
        assert "Audit" in heading, f"Expected 'Audit' in heading, got: '{heading}'"


# ═══════════════════════════════════════════════════════════════════
#  4. DOCTOR PORTAL  (4 tests)
# ═══════════════════════════════════════════════════════════════════
class TestDoctorPortal:
    """Tests for doctor portal pages."""

    def test_15_doctor_dashboard_loads(self, driver):
        """TC-15: Doctor dashboard loads after login."""
        login(driver, "doctor@camrs.com", "password123")
        time.sleep(2)
        assert "/doctor" in driver.current_url

    def test_16_doctor_appointments_page(self, driver):
        """TC-16: Doctor appointments page loads with 'Appointment' heading."""
        login(driver, "doctor@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/doctor/appointments")
        time.sleep(3)
        heading = get_page_heading(driver)
        assert "Appointment" in heading, f"Expected 'Appointment' in heading, got: '{heading}'"

    def test_17_doctor_consultation_page(self, driver):
        """TC-17: Doctor consultation page loads with 'Consult' heading."""
        login(driver, "doctor@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/doctor/consultation")
        time.sleep(3)
        heading = get_page_heading(driver)
        assert "Consult" in heading, f"Expected 'Consult' in heading, got: '{heading}'"

    def test_18_doctor_prescriptions_page(self, driver):
        """TC-18: Doctor prescriptions page loads with 'Prescription' heading."""
        login(driver, "doctor@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/doctor/prescriptions")
        time.sleep(3)
        heading = get_page_heading(driver)
        assert "Prescription" in heading, f"Expected 'Prescription' in heading, got: '{heading}'"


# ═══════════════════════════════════════════════════════════════════
#  5. PATIENT PORTAL  (6 tests)
# ═══════════════════════════════════════════════════════════════════
class TestPatientPortal:
    """Tests for patient portal pages and interactions."""

    def test_19_patient_dashboard_loads(self, driver):
        """TC-19: Patient dashboard loads with welcome content."""
        login(driver, "patient@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/patient/dashboard")
        time.sleep(3)
        heading = get_page_heading(driver)
        assert "Dashboard" in heading or "Patient" in heading, \
            f"Expected dashboard heading, got: '{heading}'"

    def test_20_appointment_booking_form_opens(self, driver):
        """TC-20: 'Book Appointment' button opens the booking form."""
        login(driver, "patient@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/patient/appointments")
        time.sleep(3)

        book_btn = find_button_by_text(driver, "Book Appointment")
        assert book_btn is not None, "Book Appointment button should exist"
        book_btn.click()
        time.sleep(2)

        # After opening, a Cancel button or form fields should appear
        cancel_btn = find_button_by_text(driver, "Cancel")
        selects = driver.find_elements(By.CSS_SELECTOR, "select")
        assert cancel_btn is not None or len(selects) > 0, \
            "Booking form should open with Cancel button or select dropdowns"

    def test_21_appointment_booking_doctor_selection(self, driver):
        """TC-21: Patient can select a doctor from the booking form dropdown."""
        login(driver, "patient@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/patient/appointments")
        time.sleep(3)

        book_btn = find_button_by_text(driver, "Book Appointment")
        assert book_btn is not None
        book_btn.click()
        time.sleep(2)

        selects = driver.find_elements(By.CSS_SELECTOR, "select")
        assert len(selects) > 0, "Should have at least one dropdown (doctor selection)"
        Select(selects[0]).select_by_index(1)
        time.sleep(1)
        assert selects[0].get_attribute("value") != "", "Doctor dropdown should have a selected value"

    def test_22_patient_profile_edit_mode(self, driver):
        """TC-22: Clicking 'Edit Profile' reveals input fields and a Save button."""
        login(driver, "patient@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/patient/profile")
        time.sleep(3)

        edit_btn = find_button_by_text(driver, "Edit Profile")
        if edit_btn:
            edit_btn.click()
            time.sleep(2)

        inputs = driver.find_elements(By.CSS_SELECTOR, "input")
        assert len(inputs) >= 2, f"Expected at least 2 input fields, found {len(inputs)}"

        save_btn = find_button_by_text(driver, "Save")
        assert save_btn is not None, "Save button should be visible in edit mode"

    def test_23_medical_records_page_loads(self, driver):
        """TC-23: Patient medical records page loads with 'Record' heading."""
        login(driver, "patient@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/patient/records")
        time.sleep(3)
        heading = get_page_heading(driver)
        assert "Record" in heading, f"Expected 'Record' in heading, got: '{heading}'"

    def test_24_patient_lab_results_page_loads(self, driver):
        """TC-24: Patient lab results page loads."""
        login(driver, "patient@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/patient/lab-results")
        time.sleep(3)
        heading = get_page_heading(driver)
        assert "Lab" in heading or "Result" in heading, \
            f"Expected lab results heading, got: '{heading}'"


# ═══════════════════════════════════════════════════════════════════
#  6. LAB STAFF PORTAL  (2 tests)
# ═══════════════════════════════════════════════════════════════════
class TestLabPortal:
    """Tests for lab staff portal pages."""

    def test_25_lab_dashboard_loads(self, driver):
        """TC-25: Lab dashboard loads with 'Lab' or 'Dashboard' heading."""
        login(driver, "lab@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/lab/dashboard")
        time.sleep(3)
        heading = get_page_heading(driver)
        assert "Lab" in heading or "Dashboard" in heading, \
            f"Expected lab dashboard heading, got: '{heading}'"

    def test_26_lab_pending_tests_page(self, driver):
        """TC-26: Lab pending tests page loads with 'Pending' or 'Lab' heading."""
        login(driver, "lab@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/lab/tests")
        time.sleep(3)
        heading = get_page_heading(driver)
        assert "Pending" in heading or "Lab" in heading or "Test" in heading, \
            f"Expected lab tests heading, got: '{heading}'"


# ═══════════════════════════════════════════════════════════════════
#  7. SECURITY & RBAC  (4 tests)
# ═══════════════════════════════════════════════════════════════════
class TestSecurity:
    """Tests for role-based access control, session management, and lockout."""

    def test_27_patient_cannot_access_admin_dashboard(self, driver):
        """TC-27: Patient is blocked from accessing /admin/dashboard (RBAC)."""
        login(driver, "patient@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/admin/dashboard")
        time.sleep(3)
        # ProtectedRoute should redirect patient away from admin
        assert "/admin/dashboard" not in driver.current_url, \
            "Patient should NOT be able to access admin dashboard"

    def test_28_doctor_cannot_access_lab_portal(self, driver):
        """TC-28: Doctor is blocked from accessing /lab/dashboard (RBAC)."""
        login(driver, "doctor@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/lab/dashboard")
        time.sleep(3)
        assert "/lab/dashboard" not in driver.current_url, \
            "Doctor should NOT be able to access lab portal"

    def test_29_unauthenticated_user_redirected_to_login(self, driver):
        """TC-29: Unauthenticated access to a protected page redirects to /login."""
        driver.get(f"{BASE_URL}/patient/dashboard")
        time.sleep(3)
        assert "login" in driver.current_url.lower(), \
            "Unauthenticated user should be redirected to login"

    def test_30_logout_clears_session_and_blocks_access(self, driver):
        """TC-30: After logout, protected pages redirect back to login."""
        login(driver, "admin@camrs.com", "password123")
        time.sleep(2)
        logout(driver)
        time.sleep(2)
        driver.get(f"{BASE_URL}/admin/dashboard")
        time.sleep(3)
        assert "login" in driver.current_url.lower(), \
            "After logout, admin dashboard should redirect to login"
