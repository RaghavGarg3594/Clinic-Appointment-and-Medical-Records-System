-- ============================================================
-- CAMRS Full Setup: Schema + Seed Data
-- Run this to completely reset and initialise the database.
-- All user passwords: password123
-- ============================================================

DROP DATABASE IF EXISTS camrs;
CREATE DATABASE camrs;
USE camrs;

-- ============================================================
-- 1. USER
-- ============================================================
CREATE TABLE User (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('PATIENT', 'DOCTOR', 'ADMIN_STAFF', 'LAB_STAFF') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    failed_login_attempts INT DEFAULT 0,
    account_locked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================================
-- 2. PATIENT
-- ============================================================
CREATE TABLE Patient (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    age INT NOT NULL,
    gender ENUM('Male', 'Female', 'Other') NOT NULL,
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(100),
    address TEXT,
    medical_history TEXT,
    allergies TEXT,
    insurance_details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE
);

-- ============================================================
-- 3. DOCTOR (includes qualification & license_number required by entity)
-- ============================================================
CREATE TABLE Doctor (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    qualification VARCHAR(150) NOT NULL,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(15),
    email VARCHAR(100),
    consultation_fee DECIMAL(10, 2) DEFAULT 0.00,
    working_hours TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE
);

-- ============================================================
-- 4. DOCTOR SCHEDULE
-- ============================================================
CREATE TABLE DoctorSchedule (
    id INT PRIMARY KEY AUTO_INCREMENT,
    doctor_id INT NOT NULL UNIQUE,
    working_days VARCHAR(255) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    leave_date DATE NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (doctor_id) REFERENCES Doctor(id) ON DELETE CASCADE
);

-- ============================================================
-- 5. STAFF (Admin & Lab)
-- ============================================================
CREATE TABLE Staff (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    staff_type ENUM('ADMIN', 'LAB') NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    department VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE
);

-- ============================================================
-- 6. APPOINTMENT
-- ============================================================
CREATE TABLE Appointment (
    id INT PRIMARY KEY AUTO_INCREMENT,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    time_slot TIME NOT NULL,
    token_number VARCHAR(50) UNIQUE,
    appointment_type ENUM('FIRST_VISIT', 'FOLLOW_UP', 'ROUTINE', 'EMERGENCY') DEFAULT 'ROUTINE',
    status ENUM('SCHEDULED', 'CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'RESCHEDULED') DEFAULT 'SCHEDULED',
    cancellation_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patient(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES Doctor(id) ON DELETE CASCADE,
    INDEX idx_appointment_date (appointment_date),
    INDEX idx_appointment_doctor_date (doctor_id, appointment_date)
);

-- ============================================================
-- 7. ICD-10 CODES
-- ============================================================
CREATE TABLE icd10_codes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) UNIQUE NOT NULL,
    description VARCHAR(255) NOT NULL
);

-- ============================================================
-- 8. MEDICAL RECORD
-- ============================================================
CREATE TABLE MedicalRecord (
    id INT PRIMARY KEY AUTO_INCREMENT,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_id INT UNIQUE NOT NULL,
    visit_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    chief_complaint TEXT,
    vital_signs TEXT,
    diagnosis TEXT,
    icd10_code_id INT,
    severity ENUM('LOW', 'MODERATE', 'HIGH', 'CRITICAL') DEFAULT 'LOW',
    advice TEXT,
    follow_up_date DATE,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by INT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patient(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES Doctor(id) ON DELETE CASCADE,
    FOREIGN KEY (appointment_id) REFERENCES Appointment(id) ON DELETE CASCADE,
    FOREIGN KEY (icd10_code_id) REFERENCES icd10_codes(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES User(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES User(id) ON DELETE SET NULL
);

-- ============================================================
-- 9. PRESCRIPTION
-- ============================================================
CREATE TABLE Prescription (
    id INT PRIMARY KEY AUTO_INCREMENT,
    medical_record_id INT NOT NULL UNIQUE,
    issue_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    print_timestamp TIMESTAMP NULL,
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (medical_record_id) REFERENCES MedicalRecord(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES User(id)
);

-- ============================================================
-- 10. MEDICATION
-- ============================================================
CREATE TABLE Medication (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    stock_quantity INT DEFAULT 0,
    reorder_level INT DEFAULT 10,
    expiry_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_medication_stock (stock_quantity)
);

-- ============================================================
-- 11. PRESCRIPTION ITEM
-- ============================================================
CREATE TABLE PrescriptionItem (
    id INT PRIMARY KEY AUTO_INCREMENT,
    prescription_id INT NOT NULL,
    medication_id INT NOT NULL,
    dosage VARCHAR(50) NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    duration VARCHAR(50) NOT NULL,
    route VARCHAR(50),
    meal_instruction VARCHAR(100),
    FOREIGN KEY (prescription_id) REFERENCES Prescription(id) ON DELETE CASCADE,
    FOREIGN KEY (medication_id) REFERENCES Medication(id) ON DELETE CASCADE
);

-- ============================================================
-- 12. LAB TEST TYPE
-- ============================================================
CREATE TABLE LabTestType (
    id INT PRIMARY KEY AUTO_INCREMENT,
    test_name VARCHAR(100) NOT NULL,
    test_code VARCHAR(20) NOT NULL UNIQUE,
    category VARCHAR(50),
    description TEXT,
    normal_range_male VARCHAR(100),
    normal_range_female VARCHAR(100),
    unit VARCHAR(30),
    cost DECIMAL(10,2),
    sample_type ENUM('Blood','Urine','Stool','Saliva','Sputum','Tissue','Swab','Other') NOT NULL,
    turnaround_time INT,
    preparation_instructions TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 13. LAB TEST ORDER
-- ============================================================
CREATE TABLE LabTestOrder (
    id INT PRIMARY KEY AUTO_INCREMENT,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    medical_record_id INT,
    test_type_id INT,
    test_type VARCHAR(100) NOT NULL,
    priority ENUM('ROUTINE', 'URGENT', 'STAT') DEFAULT 'ROUTINE',
    status ENUM('ORDERED', 'SAMPLE_COLLECTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') DEFAULT 'ORDERED',
    special_instructions TEXT,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by INT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES Patient(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES Doctor(id) ON DELETE CASCADE,
    FOREIGN KEY (medical_record_id) REFERENCES MedicalRecord(id) ON DELETE SET NULL,
    FOREIGN KEY (test_type_id) REFERENCES LabTestType(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES User(id),
    FOREIGN KEY (updated_by) REFERENCES User(id)
);

-- ============================================================
-- 14. LAB RESULT
-- ============================================================
CREATE TABLE LabResult (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL UNIQUE,
    result_value VARCHAR(100),
    unit VARCHAR(50),
    reference_range VARCHAR(100),
    is_critical BOOLEAN DEFAULT FALSE,
    entry_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by INT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES LabTestOrder(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES User(id),
    FOREIGN KEY (updated_by) REFERENCES User(id)
);

-- ============================================================
-- 15. BILL
-- ============================================================
CREATE TABLE Bill (
    id INT PRIMARY KEY AUTO_INCREMENT,
    appointment_id INT NOT NULL UNIQUE,
    patient_id INT NOT NULL,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    issue_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    consultation_charge DECIMAL(10, 2) DEFAULT 0.00,
    lab_charge DECIMAL(10, 2) DEFAULT 0.00,
    medication_charge DECIMAL(10, 2) DEFAULT 0.00,
    procedure_charge DECIMAL(10, 2) DEFAULT 0.00,
    discount DECIMAL(10, 2) DEFAULT 0.00,
    tax DECIMAL(10, 2) DEFAULT 0.00,
    total_amount DECIMAL(10, 2) DEFAULT 0.00,
    status ENUM('UNPAID', 'PARTIALLY_PAID', 'PAID') DEFAULT 'UNPAID',
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by INT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (appointment_id) REFERENCES Appointment(id) ON DELETE CASCADE,
    FOREIGN KEY (patient_id) REFERENCES Patient(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES User(id),
    FOREIGN KEY (updated_by) REFERENCES User(id)
);

-- ============================================================
-- 16. PAYMENT
-- ============================================================
CREATE TABLE Payment (
    id INT PRIMARY KEY AUTO_INCREMENT,
    bill_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    method ENUM('CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'UPI', 'INSURANCE') NOT NULL,
    transaction_ref VARCHAR(100),
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (bill_id) REFERENCES Bill(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES User(id)
);

-- ============================================================
-- 17. DOCTOR JOIN REQUEST
-- ============================================================
CREATE TABLE DoctorJoinRequest (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    specialization VARCHAR(100),
    qualification VARCHAR(200),
    license_number VARCHAR(100),
    experience_years INT,
    message TEXT,
    status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    admin_notes TEXT,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================================
-- 18. AUDIT LOG
-- ============================================================
CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id INT,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE SET NULL,
    INDEX idx_audit_timestamp (timestamp)
);

-- ============================================================
-- SEED DATA
-- Password for ALL users: password123
-- Hash generated via bcryptjs (compatible with Spring BCryptPasswordEncoder)
-- ============================================================

SET @PASS = '$2b$10$I8rai03/KEro3kFTlGkk7.Z9hGJdwGoOULM5ivq2ACFoc5wenPkvi';

-- ADMIN_STAFF — Arjun Mehta
INSERT INTO User (username, email, password_hash, role, is_active)
VALUES ('arjun.mehta', 'admin@camrs.com', @PASS, 'ADMIN_STAFF', 1);
SET @admin_uid = LAST_INSERT_ID();
INSERT INTO Staff (user_id, first_name, last_name, department, staff_type)
VALUES (@admin_uid, 'Arjun', 'Mehta', 'Administration', 'ADMIN');

-- DOCTOR — Dr. Priya Sharma
INSERT INTO User (username, email, password_hash, role, is_active)
VALUES ('priya.sharma', 'doctor@camrs.com', @PASS, 'DOCTOR', 1);
SET @doctor_uid = LAST_INSERT_ID();
INSERT INTO Doctor (user_id, first_name, last_name, specialization, qualification, license_number, phone, email, consultation_fee, is_active)
VALUES (@doctor_uid, 'Priya', 'Sharma', 'General Medicine', 'MBBS, MD (Internal Medicine) — AIIMS Delhi', 'KA-MED-2019-04521', '9876543210', 'doctor@camrs.com', 500.00, 1);
SET @doctor_id = LAST_INSERT_ID();
INSERT INTO DoctorSchedule (doctor_id, working_days, start_time, end_time)
VALUES (@doctor_id, 'Mon,Tue,Wed,Thu,Fri', '09:00:00', '17:00:00');

-- PATIENT 1 — Rahul Kumar
INSERT INTO User (username, email, password_hash, role, is_active)
VALUES ('rahul.kumar', 'patient@camrs.com', @PASS, 'PATIENT', 1);
SET @patient1_uid = LAST_INSERT_ID();
INSERT INTO Patient (user_id, first_name, last_name, email, phone, date_of_birth, age, gender, address, medical_history, allergies, insurance_details)
VALUES (@patient1_uid, 'Rahul', 'Kumar', 'patient@camrs.com', '9123456780', '1995-08-12', 30, 'Male',
  '42, Rajaji Nagar, Bengaluru, Karnataka 560010',
  'No significant past medical history', 'Dust', 'Star Health Policy – SH20240812');

-- PATIENT 2 — Ananya Iyer
INSERT INTO User (username, email, password_hash, role, is_active)
VALUES ('ananya.iyer', 'patient2@camrs.com', @PASS, 'PATIENT', 1);
SET @patient2_uid = LAST_INSERT_ID();
INSERT INTO Patient (user_id, first_name, last_name, email, phone, date_of_birth, age, gender, address, medical_history, allergies, insurance_details)
VALUES (@patient2_uid, 'Ananya', 'Iyer', 'patient2@camrs.com', '9988776655', '2000-03-25', 25, 'Female',
  '18, T. Nagar, Chennai, Tamil Nadu 600017',
  'Childhood asthma (resolved)', 'Penicillin', 'ICICI Lombard Health – IL20250325');

-- LAB_STAFF — Neha Verma
INSERT INTO User (username, email, password_hash, role, is_active)
VALUES ('neha.verma', 'lab@camrs.com', @PASS, 'LAB_STAFF', 1);
SET @lab_uid = LAST_INSERT_ID();
INSERT INTO Staff (user_id, first_name, last_name, department, staff_type)
VALUES (@lab_uid, 'Neha', 'Verma', 'Laboratory', 'LAB');

INSERT INTO Medication (name, category, stock_quantity, reorder_level, expiry_date) VALUES
('Paracetamol 500mg',  'Analgesic',        500, 50, '2027-12-31'),
('Amoxicillin 250mg',  'Antibiotic',        300, 30, '2027-06-30'),
('Omeprazole 20mg',    'Antacid',           200, 20, '2027-09-30'),
('Metformin 500mg',    'Antidiabetic',      400, 40, '2027-12-31'),
('Amlodipine 5mg',     'Antihypertensive',  250, 25, '2027-10-31'),
('Ibuprofen 400mg',    'Analgesic',         350, 35, '2027-11-30'),
('Cetirizine 10mg',    'Antihistamine',     400, 40, '2027-12-31'),
('Azithromycin 500mg', 'Antibiotic',        200, 20, '2027-08-31'),
('Ciprofloxacin 500mg','Antibiotic',        250, 25, '2027-07-31'),
('Doxycycline 100mg',  'Antibiotic',        300, 30, '2027-09-30'),
('Pantoprazole 40mg',  'Antacid',           350, 35, '2027-10-31'),
('Ranitidine 150mg',   'Antacid',           300, 30, '2027-06-30'),
('Losartan 50mg',      'Antihypertensive',  200, 20, '2027-12-31'),
('Atorvastatin 10mg',  'Lipid-lowering',    250, 25, '2027-11-30'),
('Montelukast 10mg',   'Antiasthmatic',     200, 20, '2027-10-31'),
('Salbutamol 4mg',     'Bronchodilator',    300, 30, '2027-12-31'),
('Prednisolone 5mg',   'Corticosteroid',    200, 20, '2027-08-31'),
('Diclofenac 50mg',    'Analgesic',         350, 35, '2027-09-30'),
('Clopidogrel 75mg',   'Antiplatelet',      200, 20, '2027-12-31'),
('Aspirin 75mg',       'Antiplatelet',      400, 40, '2027-12-31'),
('Domperidone 10mg',   'Antiemetic',        300, 30, '2027-07-31'),
('Ondansetron 4mg',    'Antiemetic',        200, 20, '2027-10-31'),
('Metoclopramide 10mg','Antiemetic',        250, 25, '2027-08-31'),
('Loperamide 2mg',     'Antidiarrhoeal',    300, 30, '2027-11-30'),
('Fluconazole 150mg',  'Antifungal',        200, 20, '2027-09-30');

-- LAB TEST TYPES
INSERT INTO LabTestType (test_name, test_code, category, sample_type, cost, normal_range_male, normal_range_female, unit, turnaround_time, is_active) VALUES
('Complete Blood Count',          'CBC',      'Haematology',     'Blood',  300.00, '4.5-5.5 M/µL', '4.0-5.0 M/µL', 'M/µL',   1, 1),
('Blood Glucose (Fasting)',       'FBS',      'Biochemistry',    'Blood',  150.00, '70-100 mg/dL', '70-100 mg/dL', 'mg/dL',  1, 1),
('HbA1c',                         'HBA1C',    'Biochemistry',    'Blood',  500.00, '<5.7%',         '<5.7%',         '%',      1, 1),
('Lipid Profile',                 'LIPID',    'Biochemistry',    'Blood',  600.00, 'LDL <100 mg/dL','LDL <100 mg/dL','mg/dL', 1, 1),
('Liver Function Test',           'LFT',      'Biochemistry',    'Blood',  700.00, 'ALT 7-56 U/L', 'ALT 7-56 U/L', 'U/L',    1, 1),
('Kidney Function Test',          'KFT',      'Biochemistry',    'Blood',  700.00, 'Creatinine 0.7-1.3','Creatinine 0.5-1.1','mg/dL', 1, 1),
('Thyroid Stimulating Hormone',   'TSH',      'Endocrinology',   'Blood',  450.00, '0.4-4.0 mIU/L','0.4-4.0 mIU/L','mIU/L',  1, 1),
('Urine Routine & Microscopy',    'URINE_RM', 'Microbiology',    'Urine',  200.00, 'Normal',        'Normal',        '',       1, 1),
('Stool Routine Examination',     'STOOL_RE', 'Microbiology',    'Stool',  200.00, 'No parasites',  'No parasites',  '',       1, 1),
('Chest X-Ray',                   'CXR',      'Radiology',       'Other',  400.00, 'Clear',         'Clear',         '',       1, 1),
('ECG / EKG',                     'ECG',      'Cardiology',      'Other',  350.00, 'Normal sinus',  'Normal sinus',  '',       1, 1),
('Dengue NS1 Antigen',            'DENGUE',   'Serology',        'Blood',  800.00, 'Negative',      'Negative',      '',       1, 1),
('COVID-19 Rapid Antigen',        'COVID_RAT','Microbiology',    'Swab',   500.00, 'Negative',      'Negative',      '',       1, 1),
('Malaria Parasite Test',         'MALARIA',  'Haematology',     'Blood',  300.00, 'Negative',      'Negative',      '',       1, 1),
('HIV Antibody Test',             'HIV',      'Serology',        'Blood',  600.00, 'Non-reactive',  'Non-reactive',  '',       1, 1);

-- ICD-10 Codes
INSERT INTO icd10_codes (code, description) VALUES
('A09', 'Infectious gastroenteritis and colitis, unspecified'),
('B34.9', 'Viral infection, unspecified'),
('D50.9', 'Iron deficiency anaemia, unspecified'),
('E11.9', 'Type 2 diabetes mellitus without complications'),
('E78.5', 'Dyslipidaemia, unspecified'),
('F41.1', 'Generalized anxiety disorder'),
('F32.9', 'Major depressive disorder, single episode'),
('G43.9', 'Migraine, unspecified'),
('I10', 'Essential (primary) hypertension'),
('J00', 'Acute nasopharyngitis (common cold)'),
('J02.9', 'Acute pharyngitis, unspecified'),
('J06.9', 'Upper respiratory infection, unspecified'),
('J18.9', 'Pneumonia, unspecified organism'),
('J20.9', 'Acute bronchitis, unspecified'),
('J45.9', 'Asthma, unspecified'),
('K21.0', 'Gastro-oesophageal reflux disease with oesophagitis'),
('K29.7', 'Gastritis, unspecified'),
('K59.0', 'Constipation'),
('L30.9', 'Dermatitis, unspecified'),
('L70.0', 'Acne vulgaris'),
('M54.5', 'Low back pain'),
('N39.0', 'Urinary tract infection, site not specified'),
('R05', 'Cough'),
('R10.4', 'Other and unspecified abdominal pain'),
('R50.9', 'Fever, unspecified'),
('R51', 'Headache'),
('T78.4', 'Allergy, unspecified'),
('Z00.00', 'General adult medical examination'),
('Z23', 'Encounter for immunization');

-- Verify
SELECT id, username, email, role FROM User;
SELECT id, test_name, test_code, category FROM LabTestType;

