package com.camrs.config;

import com.camrs.entity.*;
import com.camrs.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final StaffRepository staffRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final MedicationRepository medicationRepository;
    private final LabTestTypeRepository labTestTypeRepository;
    private final Icd10CodeRepository icd10CodeRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, DoctorRepository doctorRepository,
                           PatientRepository patientRepository, StaffRepository staffRepository,
                           DoctorScheduleRepository doctorScheduleRepository,
                           MedicationRepository medicationRepository,
                           LabTestTypeRepository labTestTypeRepository,
                           Icd10CodeRepository icd10CodeRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.staffRepository = staffRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.medicationRepository = medicationRepository;
        this.labTestTypeRepository = labTestTypeRepository;
        this.icd10CodeRepository = icd10CodeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createDefaultUsers();
        seedMedications();
        seedLabTestTypes();
        seedIcd10Codes();
    }

    private void createDefaultUsers() {
        String encodedPassword = passwordEncoder.encode("password123");

        // ──────────────────────────────────────────────
        // 1. ADMIN STAFF — Arjun Mehta
        // ──────────────────────────────────────────────
        if (userRepository.findByEmail("admin@camrs.com").isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("arjun.mehta");
            adminUser.setEmail("admin@camrs.com");
            adminUser.setPassword(encodedPassword);
            adminUser.setRole(User.Role.ADMIN_STAFF);
            adminUser.setIsActive(true);
            adminUser.setAccountLocked(false);
            adminUser.setFailedLoginAttempts(0);
            adminUser = userRepository.save(adminUser);

            Staff adminStaff = new Staff();
            adminStaff.setUser(adminUser);
            adminStaff.setStaffType(Staff.StaffType.ADMIN);
            adminStaff.setFirstName("Arjun");
            adminStaff.setLastName("Mehta");
            adminStaff.setDepartment("Administration");
            adminStaff.setIsActive(true);
            staffRepository.save(adminStaff);

            System.out.println("✔ Admin Staff created  : admin@camrs.com / password123");
        }

        // ──────────────────────────────────────────────
        // 2. DOCTOR — Dr. Priya Sharma
        // ──────────────────────────────────────────────
        if (userRepository.findByEmail("doctor@camrs.com").isEmpty()) {
            User doctorUser = new User();
            doctorUser.setUsername("priya.sharma");
            doctorUser.setEmail("doctor@camrs.com");
            doctorUser.setPassword(encodedPassword);
            doctorUser.setRole(User.Role.DOCTOR);
            doctorUser.setIsActive(true);
            doctorUser.setAccountLocked(false);
            doctorUser.setFailedLoginAttempts(0);
            doctorUser = userRepository.save(doctorUser);

            Doctor doctor = new Doctor();
            doctor.setUser(doctorUser);
            doctor.setFirstName("Priya");
            doctor.setLastName("Sharma");
            doctor.setSpecialization("General Medicine");
            doctor.setQualification("MBBS, MD (Internal Medicine) — AIIMS Delhi");
            doctor.setLicenseNumber("KA-MED-2019-04521");
            doctor.setPhone("9876543210");
            doctor.setEmail("doctor@camrs.com");
            doctor.setConsultationFee(new BigDecimal("500.00"));
            doctor.setIsActive(true);
            doctor = doctorRepository.save(doctor);

            // Doctor Schedule
            DoctorSchedule schedule = new DoctorSchedule();
            schedule.setDoctor(doctor);
            schedule.setWorkingDays("Mon,Tue,Wed,Thu,Fri");
            schedule.setStartTime(LocalTime.of(9, 0));
            schedule.setEndTime(LocalTime.of(18, 0));
            doctorScheduleRepository.save(schedule);

            System.out.println("✔ Doctor created       : doctor@camrs.com / password123");
        }

        // ──────────────────────────────────────────────
        // 3. LAB STAFF — Neha Verma
        // ──────────────────────────────────────────────
        if (userRepository.findByEmail("lab@camrs.com").isEmpty()) {
            User labUser = new User();
            labUser.setUsername("neha.verma");
            labUser.setEmail("lab@camrs.com");
            labUser.setPassword(encodedPassword);
            labUser.setRole(User.Role.LAB_STAFF);
            labUser.setIsActive(true);
            labUser.setAccountLocked(false);
            labUser.setFailedLoginAttempts(0);
            labUser = userRepository.save(labUser);

            Staff labStaff = new Staff();
            labStaff.setUser(labUser);
            labStaff.setStaffType(Staff.StaffType.LAB);
            labStaff.setFirstName("Neha");
            labStaff.setLastName("Verma");
            labStaff.setDepartment("Laboratory");
            labStaff.setIsActive(true);
            staffRepository.save(labStaff);

            System.out.println("✔ Lab Staff created    : lab@camrs.com / password123");
        }

        // ──────────────────────────────────────────────
        // 4. PATIENT 1 — Rahul Kumar
        // ──────────────────────────────────────────────
        if (userRepository.findByEmail("patient@camrs.com").isEmpty()) {
            User patientUser1 = new User();
            patientUser1.setUsername("rahul.kumar");
            patientUser1.setEmail("patient@camrs.com");
            patientUser1.setPassword(encodedPassword);
            patientUser1.setRole(User.Role.PATIENT);
            patientUser1.setIsActive(true);
            patientUser1.setAccountLocked(false);
            patientUser1.setFailedLoginAttempts(0);
            patientUser1 = userRepository.save(patientUser1);

            LocalDate dob1 = LocalDate.of(1995, 8, 12);
            Patient patient1 = new Patient();
            patient1.setUser(patientUser1);
            patient1.setFirstName("Rahul");
            patient1.setLastName("Kumar");
            patient1.setEmail("patient@camrs.com");
            patient1.setPhone("9123456780");
            patient1.setDateOfBirth(dob1);
            patient1.setAge(Period.between(dob1, LocalDate.now()).getYears());
            patient1.setGender(Patient.Gender.Male);
            patient1.setAddress("42, Rajaji Nagar, Bengaluru, Karnataka 560010");
            patient1.setMedicalHistory("No significant past medical history");
            patient1.setAllergies("Dust");
            patient1.setInsuranceDetails("Star Health Policy – SH20240812");
            patientRepository.save(patient1);

            System.out.println("✔ Patient 1 created   : patient@camrs.com / password123");
        }

        // ──────────────────────────────────────────────
        // 5. PATIENT 2 — Ananya Iyer
        // ──────────────────────────────────────────────
        if (userRepository.findByEmail("patient2@camrs.com").isEmpty()) {
            User patientUser2 = new User();
            patientUser2.setUsername("ananya.iyer");
            patientUser2.setEmail("patient2@camrs.com");
            patientUser2.setPassword(encodedPassword);
            patientUser2.setRole(User.Role.PATIENT);
            patientUser2.setIsActive(true);
            patientUser2.setAccountLocked(false);
            patientUser2.setFailedLoginAttempts(0);
            patientUser2 = userRepository.save(patientUser2);

            LocalDate dob2 = LocalDate.of(2000, 3, 25);
            Patient patient2 = new Patient();
            patient2.setUser(patientUser2);
            patient2.setFirstName("Ananya");
            patient2.setLastName("Iyer");
            patient2.setEmail("patient2@camrs.com");
            patient2.setPhone("9988776655");
            patient2.setDateOfBirth(dob2);
            patient2.setAge(Period.between(dob2, LocalDate.now()).getYears());
            patient2.setGender(Patient.Gender.Female);
            patient2.setAddress("18, T. Nagar, Chennai, Tamil Nadu 600017");
            patient2.setMedicalHistory("Childhood asthma (resolved)");
            patient2.setAllergies("Penicillin");
            patient2.setInsuranceDetails("ICICI Lombard Health – IL20250325");
            patientRepository.save(patient2);

            System.out.println("✔ Patient 2 created   : patient2@camrs.com / password123");
        }
    }

    private void seedMedications() {
        if (medicationRepository.count() > 0) return;
        System.out.println("⏳ Seeding medications...");
        String[][] meds = {
            {"Paracetamol 500mg",  "Analgesic",        "500", "50", "2027-12-31", "5.00"},
            {"Amoxicillin 250mg",  "Antibiotic",       "300", "30", "2027-06-30", "12.00"},
            {"Omeprazole 20mg",    "Antacid",          "200", "20", "2027-09-30", "8.50"},
            {"Metformin 500mg",    "Antidiabetic",     "400", "40", "2027-12-31", "6.00"},
            {"Amlodipine 5mg",     "Antihypertensive", "250", "25", "2027-10-31", "7.50"},
            {"Ibuprofen 400mg",    "Analgesic",        "350", "35", "2027-11-30", "6.50"},
            {"Cetirizine 10mg",    "Antihistamine",    "400", "40", "2027-12-31", "4.00"},
            {"Azithromycin 500mg", "Antibiotic",       "200", "20", "2027-08-31", "18.00"},
            {"Ciprofloxacin 500mg","Antibiotic",       "250", "25", "2027-07-31", "15.00"},
            {"Doxycycline 100mg",  "Antibiotic",       "300", "30", "2027-09-30", "10.00"},
            {"Pantoprazole 40mg",  "Antacid",          "350", "35", "2027-10-31", "9.00"},
            {"Ranitidine 150mg",   "Antacid",          "300", "30", "2027-06-30", "5.50"},
            {"Losartan 50mg",      "Antihypertensive", "200", "20", "2027-12-31", "8.00"},
            {"Atorvastatin 10mg",  "Lipid-lowering",   "250", "25", "2027-11-30", "10.00"},
            {"Montelukast 10mg",   "Antiasthmatic",    "200", "20", "2027-10-31", "12.00"},
            {"Salbutamol 4mg",     "Bronchodilator",   "300", "30", "2027-12-31", "7.00"},
            {"Prednisolone 5mg",   "Corticosteroid",   "200", "20", "2027-08-31", "6.00"},
            {"Diclofenac 50mg",    "Analgesic",        "350", "35", "2027-09-30", "5.00"},
            {"Clopidogrel 75mg",   "Antiplatelet",     "200", "20", "2027-12-31", "14.00"},
            {"Aspirin 75mg",       "Antiplatelet",     "400", "40", "2027-12-31", "3.00"},
            {"Domperidone 10mg",   "Antiemetic",       "300", "30", "2027-07-31", "5.50"},
            {"Ondansetron 4mg",    "Antiemetic",       "200", "20", "2027-10-31", "8.00"},
            {"Metoclopramide 10mg","Antiemetic",       "250", "25", "2027-08-31", "4.50"},
            {"Loperamide 2mg",     "Antidiarrhoeal",   "300", "30", "2027-11-30", "6.00"},
            {"Fluconazole 150mg",  "Antifungal",       "200", "20", "2027-09-30", "20.00"},
        };
        for (String[] m : meds) {
            Medication med = new Medication();
            med.setName(m[0]);
            med.setCategory(m[1]);
            med.setStockQuantity(Integer.parseInt(m[2]));
            med.setReorderLevel(Integer.parseInt(m[3]));
            med.setExpiryDate(LocalDate.parse(m[4]));
            med.setPrice(new BigDecimal(m[5]));
            medicationRepository.save(med);
        }
        System.out.println("✔ " + meds.length + " medications seeded");
    }

    private void seedLabTestTypes() {
        if (labTestTypeRepository.count() > 0) return;
        System.out.println("⏳ Seeding lab test types...");
        Object[][] tests = {
            {"Complete Blood Count",        "CBC",      "Haematology",   "Blood",  300.00, "4.5-5.5 M/uL", "4.0-5.0 M/uL", "M/uL",  1},
            {"Blood Glucose (Fasting)",     "FBS",      "Biochemistry",  "Blood",  150.00, "70-100 mg/dL", "70-100 mg/dL", "mg/dL", 1},
            {"HbA1c",                       "HBA1C",    "Biochemistry",  "Blood",  500.00, "<5.7%",        "<5.7%",        "%",     1},
            {"Lipid Profile",               "LIPID",    "Biochemistry",  "Blood",  600.00, "LDL <100 mg/dL","LDL <100 mg/dL","mg/dL",1},
            {"Liver Function Test",         "LFT",      "Biochemistry",  "Blood",  700.00, "ALT 7-56 U/L", "ALT 7-56 U/L", "U/L",   1},
            {"Kidney Function Test",        "KFT",      "Biochemistry",  "Blood",  700.00, "Creatinine 0.7-1.3","Creatinine 0.5-1.1","mg/dL",1},
            {"Thyroid Stimulating Hormone", "TSH",      "Endocrinology", "Blood",  450.00, "0.4-4.0 mIU/L","0.4-4.0 mIU/L","mIU/L", 1},
            {"Urine Routine & Microscopy",  "URINE_RM", "Microbiology",  "Urine",  200.00, "Normal",       "Normal",       "",      1},
            {"Stool Routine Examination",   "STOOL_RE", "Microbiology",  "Stool",  200.00, "No parasites", "No parasites", "",      1},
            {"Chest X-Ray",                 "CXR",      "Radiology",     "Other",  400.00, "Clear",        "Clear",        "",      1},
            {"ECG / EKG",                   "ECG",      "Cardiology",    "Other",  350.00, "Normal sinus", "Normal sinus", "",      1},
            {"Dengue NS1 Antigen",          "DENGUE",   "Serology",      "Blood",  800.00, "Negative",     "Negative",     "",      1},
            {"COVID-19 Rapid Antigen",      "COVID_RAT","Microbiology",  "Swab",   500.00, "Negative",     "Negative",     "",      1},
            {"Malaria Parasite Test",       "MALARIA",  "Haematology",   "Blood",  300.00, "Negative",     "Negative",     "",      1},
            {"HIV Antibody Test",           "HIV",      "Serology",      "Blood",  600.00, "Non-reactive", "Non-reactive", "",      1},
        };
        for (Object[] t : tests) {
            LabTestType ltt = new LabTestType();
            ltt.setTestName((String) t[0]);
            ltt.setTestCode((String) t[1]);
            ltt.setCategory((String) t[2]);
            ltt.setSampleType(LabTestType.SampleType.valueOf((String) t[3]));
            ltt.setCost(BigDecimal.valueOf((Double) t[4]));
            ltt.setNormalRangeMale((String) t[5]);
            ltt.setNormalRangeFemale((String) t[6]);
            ltt.setUnit((String) t[7]);
            ltt.setTurnaroundTime((Integer) t[8]);
            ltt.setIsActive(true);
            labTestTypeRepository.save(ltt);
        }
        System.out.println("✔ " + tests.length + " lab test types seeded");
    }

    private void seedIcd10Codes() {
        if (icd10CodeRepository.count() > 0) return;
        System.out.println("⏳ Seeding ICD-10 codes...");
        String[][] codes = {
            {"A09",    "Infectious gastroenteritis and colitis, unspecified"},
            {"B34.9",  "Viral infection, unspecified"},
            {"D50.9",  "Iron deficiency anaemia, unspecified"},
            {"E11.9",  "Type 2 diabetes mellitus without complications"},
            {"E78.5",  "Dyslipidaemia, unspecified"},
            {"F41.1",  "Generalized anxiety disorder"},
            {"F32.9",  "Major depressive disorder, single episode"},
            {"G43.9",  "Migraine, unspecified"},
            {"I10",    "Essential (primary) hypertension"},
            {"J00",    "Acute nasopharyngitis (common cold)"},
            {"J02.9",  "Acute pharyngitis, unspecified"},
            {"J06.9",  "Upper respiratory infection, unspecified"},
            {"J18.9",  "Pneumonia, unspecified organism"},
            {"J20.9",  "Acute bronchitis, unspecified"},
            {"J45.9",  "Asthma, unspecified"},
            {"K21.0",  "Gastro-oesophageal reflux disease with oesophagitis"},
            {"K29.7",  "Gastritis, unspecified"},
            {"K59.0",  "Constipation"},
            {"L30.9",  "Dermatitis, unspecified"},
            {"L70.0",  "Acne vulgaris"},
            {"M54.5",  "Low back pain"},
            {"N39.0",  "Urinary tract infection, site not specified"},
            {"R05",    "Cough"},
            {"R10.4",  "Other and unspecified abdominal pain"},
            {"R50.9",  "Fever, unspecified"},
            {"R51",    "Headache"},
            {"T78.4",  "Allergy, unspecified"},
            {"Z00.00", "General adult medical examination"},
            {"Z23",    "Encounter for immunization"},
        };
        for (String[] c : codes) {
            Icd10Code code = new Icd10Code();
            code.setCode(c[0]);
            code.setDescription(c[1]);
            icd10CodeRepository.save(code);
        }
        System.out.println("✔ " + codes.length + " ICD-10 codes seeded");
    }
}
