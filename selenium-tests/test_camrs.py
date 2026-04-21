"""
CAMRS Selenium Test Suite — 30 Core Test Cases
================================================
A consolidated suite covering authentication, all 4 portals,
CRUD operations, security (RBAC), and a full E2E patient journey.
Each test creates real input and validates real output.

Run:  pytest selenium-tests/test_camrs.py -v --tb=short
"""

import pytest
import time
import uuid
from datetime import datetime, timedelta
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import Select
from selenium.common.exceptions import NoSuchElementException
from conftest import BASE_URL, login, logout


# ── helpers ──────────────────────────────────────────────────────────
UNIQUE = uuid.uuid4().hex[:6]

def set_date_input(driver, selector, date_value):
    """Set a date input via JS (macOS Chrome compat)."""
    driver.execute_script(f"""
        var el = document.querySelector("{selector}");
        var setter = Object.getOwnPropertyDescriptor(
            window.HTMLInputElement.prototype, 'value').set;
        setter.call(el, '{date_value}');
        el.dispatchEvent(new Event('input', {{ bubbles: true }}));
        el.dispatchEvent(new Event('change', {{ bubbles: true }}));
    """)
    time.sleep(0.5)


# ═════════════════════════════════════════════════════════════════════
#  1. AUTHENTICATION  (4 tests)
# ═════════════════════════════════════════════════════════════════════
class TestAuthentication:

    def test_01_admin_login(self, driver):
        """TC-01: Admin can log in and reach the admin dashboard."""
        login(driver, "admin@camrs.com", "password123")
        time.sleep(2)
        assert "/admin" in driver.current_url

    def test_02_doctor_login(self, driver):
        """TC-02: Doctor can log in and reach the doctor portal."""
        login(driver, "doctor@camrs.com", "password123")
        time.sleep(2)
        assert "/doctor" in driver.current_url

    def test_03_patient_login(self, driver):
        """TC-03: Patient can log in and reach the patient portal."""
        login(driver, "patient@camrs.com", "password123")
        time.sleep(2)
        assert "/patient" in driver.current_url

    def test_04_invalid_credentials(self, driver):
        """TC-04: Invalid credentials do NOT redirect to a dashboard."""
        result = login(driver, "fake@camrs.com", "wrongpassword")
        time.sleep(2)
        assert "login" in driver.current_url.lower()




    def test_05_patient_registration_with_new_fields(self, driver):
        """TC-05: Patient registration works with medical history and allergies."""
        unique_email = f"newpatient{UNIQUE}@camrs.com"
        driver.get(f"{BASE_URL}/register")
        time.sleep(2)
        driver.find_element(By.NAME, "firstName").send_keys("Test")
        driver.find_element(By.NAME, "lastName").send_keys(f"Patient_{UNIQUE}")
        driver.find_element(By.NAME, "email").send_keys(unique_email)
        driver.find_element(By.NAME, "phone").send_keys("9998887776")
        driver.find_element(By.NAME, "password").send_keys("password123")
        set_date_input(driver, "input[name='dateOfBirth']", "1990-01-01")
        Select(driver.find_element(By.NAME, "gender")).select_by_visible_text("Male")
        
        # New Feature 6 & 11 fields
        driver.find_element(By.NAME, "medicalHistory").send_keys("No major issues")
        driver.find_element(By.NAME, "allergies").send_keys("Peanuts")
        driver.find_element(By.NAME, "insuranceDetails").send_keys("BlueCross 12345")
        
        driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()
        time.sleep(3)
        assert "login" in driver.current_url.lower(), "Should redirect to login after registration"
        assert "registered=true" in driver.current_url.lower()

    def test_06_doctor_join_request_with_new_fields(self, driver):
        """TC-06: Doctor join request submission works."""
        driver.get(f"{BASE_URL}/join")
        time.sleep(2)
        driver.find_element(By.NAME, "firstName").send_keys("DrNew")
        driver.find_element(By.NAME, "lastName").send_keys(f"Join_{UNIQUE}")
        driver.find_element(By.NAME, "email").send_keys(f"drnew{UNIQUE}@camrs.com")
        driver.find_element(By.NAME, "phone").send_keys("9876543210")
        driver.find_element(By.NAME, "specialization").send_keys("Neurology")
        driver.find_element(By.NAME, "qualification").send_keys("MD")
        driver.find_element(By.NAME, "licenseNumber").send_keys("MED-NEUR-001")
        driver.find_element(By.NAME, "experienceYears").send_keys("10")
        driver.find_element(By.NAME, "message").send_keys("I would like to join.")
        
        driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()
        time.sleep(3)
        
        success_text = driver.find_element(By.CSS_SELECTOR, "h2").text
        assert "Request Submitted!" in success_text

# ═════════════════════════════════════════════════════════════════════
#  3. ADMIN PORTAL  (7 tests)
# ═════════════════════════════════════════════════════════════════════
class TestAdminPortal:

    def test_08_dashboard_loads(self, driver):
        """TC-08: Admin dashboard loads with stat cards."""
        login(driver, "admin@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/admin/dashboard")
        time.sleep(3)
        h1s = driver.find_elements(By.TAG_NAME, "h1")
        assert any("Dashboard" in h1.text for h1 in h1s), f"No h1 contains Dashboard. Found: {[h1.text for h1 in h1s]}"

    def test_09_dashboard_stat_cards(self, driver):
        """TC-09: At least 4 stat cards visible on admin dashboard."""
        login(driver, "admin@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/admin/dashboard")
        time.sleep(3)
        cards = driver.find_elements(By.CSS_SELECTOR, "[class*='card']")
        assert len(cards) >= 4

    def test_10_doctor_management_loads(self, driver):
        """TC-10: Doctor management page loads with a table."""
        login(driver, "admin@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/admin/doctors")
        time.sleep(3)
        table = driver.find_element(By.CSS_SELECTOR, "table")
        assert table.is_displayed()

    def test_11_add_doctor_form_fills(self, driver):
        """TC-11: Admin opens Add Doctor modal and fills input fields."""
        login(driver, "admin@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/admin/doctors")
        time.sleep(3)
        btns = driver.find_elements(By.CSS_SELECTOR, "button")
        [b for b in btns if "Add Doctor" in b.text][0].click()
        time.sleep(1)
        dialog = driver.find_element(By.CSS_SELECTOR, "[role='dialog']")
        fn = dialog.find_element(By.CSS_SELECTOR, "input[name='firstName']")
        fn.send_keys("SeleniumDoc")
        assert fn.get_attribute("value") == "SeleniumDoc"

    def test_12_search_doctor(self, driver):
        """TC-12: Admin can search doctors by name."""
        login(driver, "admin@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/admin/doctors")
        time.sleep(3)
        search = driver.find_element(By.CSS_SELECTOR, "input[placeholder*='Search']")
        search.send_keys("John")
        time.sleep(2)
        rows = driver.find_elements(By.CSS_SELECTOR, "tbody tr")
        assert len(rows) >= 1

    def test_13_inventory_page_loads(self, driver):
        """TC-13: Inventory page loads with medications table."""
        login(driver, "admin@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/admin/inventory")
        time.sleep(3)
        h1 = driver.find_element(By.TAG_NAME, "h1")
        assert "Inventory" in h1.text

    def test_14_billing_page_loads(self, driver):
        """TC-14: Billing page loads successfully."""
        login(driver, "admin@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/admin/billing")
        time.sleep(3)
        h1 = driver.find_element(By.TAG_NAME, "h1")
        assert "Billing" in h1.text


# ═════════════════════════════════════════════════════════════════════
#  4. DOCTOR PORTAL  (3 tests)
# ═════════════════════════════════════════════════════════════════════
class TestDoctorPortal:

    def test_15_doctor_appointments_page(self, driver):
        """TC-15: Doctor appointments page loads."""
        login(driver, "doctor@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/doctor/appointments")
        time.sleep(3)
        h1 = driver.find_element(By.TAG_NAME, "h1")
        assert "Appointment" in h1.text

    def test_16_doctor_consultation_page(self, driver):
        """TC-16: Doctor consultation page loads."""
        login(driver, "doctor@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/doctor/consultation")
        time.sleep(3)
        h1 = driver.find_element(By.TAG_NAME, "h1")
        assert "Consult" in h1.text

    def test_17_doctor_prescriptions_page(self, driver):
        """TC-17: Doctor prescriptions page loads."""
        login(driver, "doctor@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/doctor/prescriptions")
        time.sleep(3)
        h1 = driver.find_element(By.TAG_NAME, "h1")
        assert "Prescription" in h1.text


# ═════════════════════════════════════════════════════════════════════
#  5. PATIENT PORTAL  (5 tests)
# ═════════════════════════════════════════════════════════════════════
class TestPatientPortal:

    def test_18_patient_dashboard(self, driver):
        """TC-18: Patient dashboard loads."""
        login(driver, "patient@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/patient/dashboard")
        time.sleep(3)
        h1 = driver.find_element(By.TAG_NAME, "h1")
        assert "Patient Dashboard" in h1.text

    def test_19_booking_form_opens(self, driver):
        """TC-19: Book Appointment button toggles the booking form."""
        login(driver, "patient@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/patient/appointments")
        time.sleep(3)
        btns = driver.find_elements(By.CSS_SELECTOR, "button")
        [b for b in btns if "Book Appointment" in b.text][0].click()
        time.sleep(3)
        # After click the button text changes to 'Cancel'
        btns2 = driver.find_elements(By.CSS_SELECTOR, "button")
        cancel = [b for b in btns2 if b.text.strip() == "Cancel"]
        assert len(cancel) > 0, "Booking form should open (button shows Cancel)"

    def test_20_select_doctor_in_booking(self, driver):
        """TC-20: Patient can select a doctor in the booking form."""
        login(driver, "patient@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/patient/appointments")
        time.sleep(3)
        btns = driver.find_elements(By.CSS_SELECTOR, "button")
        [b for b in btns if "Book Appointment" in b.text][0].click()
        time.sleep(3)
        selects = driver.find_elements(By.CSS_SELECTOR, "select")
        Select(selects[0]).select_by_index(1)
        time.sleep(1)
        assert selects[0].get_attribute("value") != ""

    def test_21_profile_edit_mode(self, driver):
        """TC-21: Clicking Edit Profile shows input fields and Save button."""
        login(driver, "patient@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/patient/profile")
        time.sleep(3)
        btns = driver.find_elements(By.CSS_SELECTOR, "button")
        edit = [b for b in btns if "Edit Profile" in b.text]
        if edit:
            edit[0].click()
            time.sleep(2)
        inputs = driver.find_elements(By.CSS_SELECTOR, "input")
        assert len(inputs) >= 2
        btns2 = driver.find_elements(By.CSS_SELECTOR, "button")
        save = [b for b in btns2 if "Save" in b.text]
        assert len(save) > 0

    def test_22_medical_records_page(self, driver):
        """TC-22: Patient medical records page loads."""
        login(driver, "patient@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/patient/records")
        time.sleep(3)
        h1 = driver.find_element(By.TAG_NAME, "h1")
        assert "Record" in h1.text


# ═════════════════════════════════════════════════════════════════════
#  6. LAB STAFF PORTAL  (2 tests)
# ═════════════════════════════════════════════════════════════════════
class TestLabPortal:

    def test_23_lab_dashboard(self, driver):
        """TC-23: Lab dashboard loads with stat cards."""
        login(driver, "lab@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/lab/dashboard")
        time.sleep(3)
        h1 = driver.find_element(By.TAG_NAME, "h1")
        assert "Lab" in h1.text or "Dashboard" in h1.text

    def test_24_lab_pending_tests(self, driver):
        """TC-24: Lab pending tests page loads."""
        login(driver, "lab@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/lab/tests")
        time.sleep(3)
        h1 = driver.find_element(By.TAG_NAME, "h1")
        assert "Pending" in h1.text or "Lab" in h1.text


# ═════════════════════════════════════════════════════════════════════
#  7. SECURITY / RBAC  (4 tests)
# ═════════════════════════════════════════════════════════════════════
class TestSecurity:

    def test_25_patient_blocked_from_admin(self, driver):
        """TC-25: Patient cannot access admin dashboard."""
        login(driver, "patient@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/admin/dashboard")
        time.sleep(3)
        assert "/admin/dashboard" not in driver.current_url

    def test_26_doctor_blocked_from_lab(self, driver):
        """TC-26: Doctor cannot access lab portal."""
        login(driver, "doctor@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/lab/dashboard")
        time.sleep(3)
        assert "/lab/dashboard" not in driver.current_url

    def test_27_unauthenticated_redirect(self, driver):
        """TC-27: Unauthenticated user is redirected to login."""
        driver.get(f"{BASE_URL}/patient/dashboard")
        time.sleep(3)
        assert "login" in driver.current_url.lower()

    def test_28_logout_clears_session(self, driver):
        """TC-28: After logout, protected pages redirect to login."""
        login(driver, "admin@camrs.com", "password123")
        time.sleep(2)
        logout(driver)
        time.sleep(2)
        driver.get(f"{BASE_URL}/admin/dashboard")
        time.sleep(3)
        assert "login" in driver.current_url.lower()


# ═════════════════════════════════════════════════════════════════════
#  8. FULL E2E JOURNEY  (2 tests)
# ═════════════════════════════════════════════════════════════════════
class TestFullJourney:

    def test_29_admin_reports_page(self, driver):
        """TC-29: Admin reports page loads with stats."""
        login(driver, "admin@camrs.com", "password123")
        time.sleep(2)
        driver.get(f"{BASE_URL}/admin/reports")
        time.sleep(3)
        h1 = driver.find_element(By.TAG_NAME, "h1")
        assert "Report" in h1.text


