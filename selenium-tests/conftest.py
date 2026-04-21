"""
CAMRS Selenium Test Suite — Shared Fixtures & Helpers
=====================================================
Provides browser setup, login helpers, and shared test configuration.
"""

import pytest
import time
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException, NoSuchElementException

# ── Configuration ─────────────────────────────────────────────────
BASE_URL = "http://localhost:5173"
BACKEND_URL = "http://localhost:8080"
IMPLICIT_WAIT = 10
EXPLICIT_WAIT = 15

# Default test credentials (from demo-seed.sql / DataInitializer)
TEST_USERS = {
    "admin": {"email": "admin@camrs.com", "password": "password123", "role": "ADMIN_STAFF"},
    "doctor": {"email": "doctor@camrs.com", "password": "password123", "role": "DOCTOR"},
    "patient": {"email": "patient@camrs.com", "password": "password123", "role": "PATIENT"},
    "lab": {"email": "lab@camrs.com", "password": "password123", "role": "LAB_STAFF"},
}


# ── Browser Fixture ──────────────────────────────────────────────
@pytest.fixture(scope="function")
def driver():
    """Create a fresh Chrome browser instance for each test."""
    chrome_options = Options()
    chrome_options.add_argument("--no-sandbox")
    chrome_options.add_argument("--disable-dev-shm-usage")
    chrome_options.add_argument("--window-size=1440,900")
    # To run headless, uncomment:
    # chrome_options.add_argument("--headless=new")

    try:
        browser = webdriver.Chrome(options=chrome_options)
    except Exception:
        # Fallback: try with webdriver-manager
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

    try:
        browser = webdriver.Chrome(options=chrome_options)
    except Exception:
        from webdriver_manager.chrome import ChromeDriverManager
        service = Service(ChromeDriverManager().install())
        browser = webdriver.Chrome(service=service, options=chrome_options)

    browser.implicitly_wait(IMPLICIT_WAIT)
    yield browser
    browser.quit()


# ── Helper Functions ─────────────────────────────────────────────
def login(driver, email, password):
    """
    Perform login via the CAMRS login page.
    Returns True if login succeeded, False otherwise.
    """
    driver.get(f"{BASE_URL}/login")
    wait = WebDriverWait(driver, EXPLICIT_WAIT)

    try:
        # Wait for login form to be present
        email_input = wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, "input[type='email'], input[name='email']")))
        email_input.clear()
        email_input.send_keys(email)

        password_input = driver.find_element(By.CSS_SELECTOR, "input[type='password'], input[name='password']")
        password_input.clear()
        password_input.send_keys(password)

        # Click login button
        login_btn = driver.find_element(By.CSS_SELECTOR, "button[type='submit']")
        login_btn.click()

        # Wait for redirect away from login page
        time.sleep(2)  # Allow the page to load
        wait.until(lambda d: "/login" not in d.current_url)
        return True
    except (TimeoutException, NoSuchElementException) as e:
        print(f"Login failed for {email}: {e}")
        return False


def login_as(driver, role_key):
    """Login as a specific role (admin, doctor, patient, lab)."""
    user = TEST_USERS.get(role_key)
    if not user:
        raise ValueError(f"Unknown role key: {role_key}")
    return login(driver, user["email"], user["password"])


def logout(driver):
    """Perform logout by clearing localStorage and navigating to login."""
    driver.execute_script("localStorage.clear();")
    driver.get(f"{BASE_URL}/login")
    time.sleep(1)


def wait_for_element(driver, selector, by=By.CSS_SELECTOR, timeout=EXPLICIT_WAIT):
    """Wait for an element to be present and return it."""
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


def get_page_title(driver):
    """Get the main heading (h1) text from the page."""
    try:
        h1 = driver.find_element(By.TAG_NAME, "h1")
        return h1.text
    except NoSuchElementException:
        return ""


def take_screenshot(driver, name):
    """Take a screenshot for debugging."""
    driver.save_screenshot(f"selenium-tests/screenshots/{name}.png")
