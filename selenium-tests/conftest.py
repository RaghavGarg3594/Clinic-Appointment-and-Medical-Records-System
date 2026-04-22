"""
CAMRS Selenium Test Suite — Shared Fixtures & Helpers
=====================================================
Provides browser setup, login helpers, API utilities, and shared test
configuration for the CAMRS (Clinic Appointment & Medical Records System).

Run:  pytest selenium-tests/test_camrs.py -v --tb=short
"""

import os
import pytest
import time
import requests
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException, NoSuchElementException

# ── Configuration ─────────────────────────────────────────────────
BASE_URL = os.getenv("CAMRS_FRONTEND_URL", "http://localhost:5173")
BACKEND_URL = os.getenv("CAMRS_BACKEND_URL", "http://localhost:8080")
IMPLICIT_WAIT = 10
EXPLICIT_WAIT = 15

# Default test credentials (from camrs-full-setup.sql / DataInitializer)
TEST_USERS = {
    "admin":   {"email": "admin@camrs.com",    "password": "password123", "role": "ADMIN_STAFF"},
    "doctor":  {"email": "doctor@camrs.com",   "password": "password123", "role": "DOCTOR"},
    "patient": {"email": "patient@camrs.com",  "password": "password123", "role": "PATIENT"},
    "patient2":{"email": "patient2@camrs.com", "password": "password123", "role": "PATIENT"},
    "lab":     {"email": "lab@camrs.com",       "password": "password123", "role": "LAB_STAFF"},
}

# Screenshots directory
SCREENSHOT_DIR = os.path.join(os.path.dirname(__file__), "screenshots")
os.makedirs(SCREENSHOT_DIR, exist_ok=True)


# ── Browser Fixtures ─────────────────────────────────────────────
@pytest.fixture(scope="function")
def driver():
    """Create a fresh Chrome browser instance for each test."""
    chrome_options = Options()
    chrome_options.add_argument("--no-sandbox")
    chrome_options.add_argument("--disable-dev-shm-usage")
    chrome_options.add_argument("--window-size=1440,900")
    chrome_options.add_argument("--disable-gpu")
    # Uncomment for headless execution:
    # chrome_options.add_argument("--headless=new")

    try:
        browser = webdriver.Chrome(options=chrome_options)
    except Exception:
        from webdriver_manager.chrome import ChromeDriverManager
        service = Service(ChromeDriverManager().install())
        browser = webdriver.Chrome(service=service, options=chrome_options)

    browser.implicitly_wait(IMPLICIT_WAIT)
    yield browser
    browser.quit()


@pytest.fixture(scope="function")
def headless_driver():
    """Create a headless Chrome browser instance for faster test execution."""
    chrome_options = Options()
    chrome_options.add_argument("--no-sandbox")
    chrome_options.add_argument("--disable-dev-shm-usage")
    chrome_options.add_argument("--headless=new")
    chrome_options.add_argument("--window-size=1440,900")
    chrome_options.add_argument("--disable-gpu")

    try:
        browser = webdriver.Chrome(options=chrome_options)
    except Exception:
        from webdriver_manager.chrome import ChromeDriverManager
        service = Service(ChromeDriverManager().install())
        browser = webdriver.Chrome(service=service, options=chrome_options)

    browser.implicitly_wait(IMPLICIT_WAIT)
    yield browser
    browser.quit()


# ── Login Helpers ────────────────────────────────────────────────
def login(driver, email, password):
    """
    Perform login via the CAMRS login page.
    Waits for the login form, fills credentials, clicks submit,
    and waits for navigation away from /login.
    Returns True if login succeeded, False otherwise.
    """
    driver.get(f"{BASE_URL}/login")
    wait = WebDriverWait(driver, EXPLICIT_WAIT)

    try:
        # Wait for the email input (#email id from Login.jsx)
        email_input = wait.until(
            EC.presence_of_element_located((By.CSS_SELECTOR, "#email, input[type='email']"))
        )
        email_input.clear()
        email_input.send_keys(email)

        password_input = driver.find_element(By.CSS_SELECTOR, "#password, input[type='password']")
        password_input.clear()
        password_input.send_keys(password)

        # Click 'Sign in' button
        submit_btn = driver.find_element(By.CSS_SELECTOR, "button[type='submit']")
        submit_btn.click()

        # Wait for redirect away from /login
        time.sleep(2)
        wait.until(lambda d: "/login" not in d.current_url)
        return True
    except (TimeoutException, NoSuchElementException) as e:
        print(f"Login failed for {email}: {e}")
        return False


def login_as(driver, role_key):
    """Login as a specific role (admin, doctor, patient, patient2, lab)."""
    user = TEST_USERS.get(role_key)
    if not user:
        raise ValueError(f"Unknown role key: {role_key}")
    return login(driver, user["email"], user["password"])


def logout(driver):
    """Perform logout by clearing localStorage and navigating to login."""
    driver.execute_script("localStorage.clear(); sessionStorage.clear();")
    driver.get(f"{BASE_URL}/login")
    time.sleep(1)


# ── Wait Helpers ─────────────────────────────────────────────────
def wait_for_element(driver, selector, by=By.CSS_SELECTOR, timeout=EXPLICIT_WAIT):
    """Wait for an element to be present in the DOM and return it."""
    wait = WebDriverWait(driver, timeout)
    return wait.until(EC.presence_of_element_located((by, selector)))


def wait_for_visible(driver, selector, by=By.CSS_SELECTOR, timeout=EXPLICIT_WAIT):
    """Wait for an element to be visible and return it."""
    wait = WebDriverWait(driver, timeout)
    return wait.until(EC.visibility_of_element_located((by, selector)))


def wait_for_clickable(driver, selector, by=By.CSS_SELECTOR, timeout=EXPLICIT_WAIT):
    """Wait for an element to be clickable and return it."""
    wait = WebDriverWait(driver, timeout)
    return wait.until(EC.element_to_be_clickable((by, selector)))


def is_element_present(driver, selector, by=By.CSS_SELECTOR):
    """Check if an element exists on the page."""
    try:
        driver.find_element(by, selector)
        return True
    except NoSuchElementException:
        return False


def get_page_heading(driver, tag="h1"):
    """Get the main heading text from the page."""
    try:
        el = driver.find_element(By.TAG_NAME, tag)
        return el.text
    except NoSuchElementException:
        return ""


def take_screenshot(driver, name):
    """Save a screenshot for debugging."""
    filepath = os.path.join(SCREENSHOT_DIR, f"{name}.png")
    driver.save_screenshot(filepath)
    return filepath


def find_button_by_text(driver, text):
    """Find a button element containing the specified text."""
    buttons = driver.find_elements(By.CSS_SELECTOR, "button")
    for btn in buttons:
        if text in btn.text:
            return btn
    return None


# ── API Helpers ──────────────────────────────────────────────────
def api_login(email, password):
    """Login via the REST API and return the JWT token."""
    resp = requests.post(f"{BACKEND_URL}/api/auth/login", json={
        "email": email, "password": password
    })
    resp.raise_for_status()
    return resp.json().get("token")


def api_get(endpoint, token):
    """Make an authenticated GET request to the backend API."""
    resp = requests.get(f"{BACKEND_URL}/api{endpoint}", headers={
        "Authorization": f"Bearer {token}"
    })
    resp.raise_for_status()
    return resp.json()


def set_date_input(driver, selector, date_value):
    """Set a date input value via JavaScript (cross-platform compatible)."""
    driver.execute_script(f"""
        var el = document.querySelector("{selector}");
        if (!el) return;
        var setter = Object.getOwnPropertyDescriptor(
            window.HTMLInputElement.prototype, 'value').set;
        setter.call(el, '{date_value}');
        el.dispatchEvent(new Event('input', {{ bubbles: true }}));
        el.dispatchEvent(new Event('change', {{ bubbles: true }}));
    """)
    time.sleep(0.5)
