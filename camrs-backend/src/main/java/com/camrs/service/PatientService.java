package com.camrs.service;

import com.camrs.entity.Patient;
import com.camrs.entity.User;
import com.camrs.repository.PatientRepository;
import com.camrs.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public PatientService(PatientRepository patientRepository, UserRepository userRepository) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", patient.getId());
        profile.put("userId", user.getId());
        profile.put("firstName", patient.getFirstName());
        profile.put("lastName", patient.getLastName());
        profile.put("email", patient.getEmail() != null ? patient.getEmail() : user.getEmail());
        profile.put("phone", patient.getPhone());
        profile.put("dateOfBirth", patient.getDateOfBirth());
        profile.put("age", patient.getDateOfBirth() != null 
                ? Period.between(patient.getDateOfBirth(), LocalDate.now()).getYears() : 0);
        profile.put("gender", patient.getGender() != null ? patient.getGender().name() : "Other");
        profile.put("address", patient.getAddress());
        profile.put("medicalHistory", patient.getMedicalHistory());
        profile.put("allergies", patient.getAllergies());
        profile.put("insuranceDetails", patient.getInsuranceDetails());
        profile.put("emergencyContact", patient.getEmergencyContact());
        return profile;
    }

    @Transactional
    public Map<String, Object> updateProfile(String email, Map<String, Object> updates) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        if (updates.containsKey("firstName")) patient.setFirstName((String) updates.get("firstName"));
        if (updates.containsKey("lastName")) patient.setLastName((String) updates.get("lastName"));
        if (updates.containsKey("phone")) patient.setPhone((String) updates.get("phone"));
        if (updates.containsKey("address")) patient.setAddress((String) updates.get("address"));
        if (updates.containsKey("medicalHistory")) patient.setMedicalHistory((String) updates.get("medicalHistory"));
        if (updates.containsKey("allergies")) patient.setAllergies((String) updates.get("allergies"));
        if (updates.containsKey("insuranceDetails")) patient.setInsuranceDetails((String) updates.get("insuranceDetails"));
        if (updates.containsKey("emergencyContact")) patient.setEmergencyContact((String) updates.get("emergencyContact"));

        if (updates.containsKey("gender")) {
            String g = (String) updates.get("gender");
            if ("Male".equalsIgnoreCase(g)) patient.setGender(Patient.Gender.Male);
            else if ("Female".equalsIgnoreCase(g)) patient.setGender(Patient.Gender.Female);
            else patient.setGender(Patient.Gender.Other);
        }

        patientRepository.save(patient);
        return getProfile(email);
    }
}
