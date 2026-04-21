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
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, DoctorRepository doctorRepository,
                           PatientRepository patientRepository, StaffRepository staffRepository,
                           DoctorScheduleRepository doctorScheduleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.staffRepository = staffRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createDefaultUsers();
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
}
