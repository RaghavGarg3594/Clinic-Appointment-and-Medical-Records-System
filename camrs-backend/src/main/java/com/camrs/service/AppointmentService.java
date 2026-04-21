package com.camrs.service;
import com.camrs.dto.*;
import com.camrs.entity.*;
import com.camrs.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final BillRepository billRepository;

    private static final LocalTime DEFAULT_START = LocalTime.of(9, 0);
    private static final LocalTime DEFAULT_END = LocalTime.of(18, 0);
    private static final int SLOT_MINUTES = 30;

    public AppointmentService(AppointmentRepository appointmentRepository, DoctorRepository doctorRepository,
                              PatientRepository patientRepository, UserRepository userRepository,
                              BillRepository billRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.billRepository = billRepository;
    }

    @Transactional
    public AppointmentResponse bookAppointment(String patientEmail, AppointmentBookingRequest request) {
        User user = userRepository.findByEmail(patientEmail).orElseThrow(() -> new RuntimeException("User not found"));
        Patient patient = patientRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Patient profile not found"));
        Doctor doctor = doctorRepository.findById(request.getDoctorId()).orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Weekend check
        DayOfWeek dayOfWeek = request.getAppointmentDate().getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            throw new RuntimeException("Appointments cannot be booked on weekends (Saturday/Sunday)");
        }

        boolean isEmergency = "EMERGENCY".equals(request.getAppointmentType());

        if (isEmergency) {
            return handleEmergencyBooking(patient, doctor, request);
        }

        // Reject past time slots on today
        if (!isEmergency && request.getAppointmentDate().equals(LocalDate.now())) {
            LocalTime requestedTime = LocalTime.parse(request.getTimeSlot());
            if (requestedTime.isBefore(LocalTime.now())) {
                throw new RuntimeException("Cannot book a time slot that has already passed");
            }
        }

        // Regular booking
        if (!isEmergency) {
            boolean taken = appointmentRepository.findByDoctorIdAndAppointmentDate(doctor.getId(), request.getAppointmentDate())
                    .stream().anyMatch(a -> a.getTimeSlot().equals(LocalTime.parse(request.getTimeSlot())) && a.getStatus() != Appointment.AppointmentStatus.CANCELLED);
            if (taken) {
                throw new RuntimeException("Time slot is no longer available");
            }
        }

        Appointment appt = new Appointment();
        appt.setPatient(patient);
        appt.setDoctor(doctor);
        appt.setAppointmentDate(request.getAppointmentDate());
        LocalTime parsedTime = LocalTime.parse(request.getTimeSlot());
        appt.setTimeSlot(parsedTime);
        appt.setAppointmentTime(parsedTime);
        try {
            appt.setAppointmentType(Appointment.AppointmentType.valueOf(request.getAppointmentType()));
        } catch (Exception e) {
            appt.setAppointmentType(Appointment.AppointmentType.ROUTINE);
        }
        appt.setStatus(Appointment.AppointmentStatus.APPROVAL_PENDING);
        appt.setTokenNumber("TK-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        appt = appointmentRepository.save(appt);

        // Create appointment fee bill (Option B: flat consultation fee)
        createAppointmentFeeBill(appt, doctor);

        return mapToResponse(appt);
    }

    @Transactional
    private AppointmentResponse handleEmergencyBooking(Patient patient, Doctor doctor, AppointmentBookingRequest request) {
        LocalDate date = request.getAppointmentDate();
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);

        // Get all non-cancelled appointments for this doctor on this date, ordered by time
        List<Appointment> existingAppts = appointmentRepository.findByDoctorIdAndAppointmentDate(doctor.getId(), date)
                .stream()
                .filter(a -> a.getStatus() != Appointment.AppointmentStatus.CANCELLED)
                .sorted(Comparator.comparing(Appointment::getTimeSlot))
                .collect(Collectors.toList());

        // Find the insertion point: nearest half-hour from now
        LocalTime insertSlot = now;
        int minuteRemainder = insertSlot.getMinute() % SLOT_MINUTES;
        if (minuteRemainder != 0) {
            insertSlot = insertSlot.plusMinutes(SLOT_MINUTES - minuteRemainder);
        }
        if (insertSlot.isBefore(DEFAULT_START)) {
            insertSlot = DEFAULT_START;
        }

        // Shift all appointments at or after the insert slot by 30 minutes
        for (Appointment existing : existingAppts) {
            if (!existing.getTimeSlot().isBefore(insertSlot)) {
                LocalTime shifted = existing.getTimeSlot().plusMinutes(SLOT_MINUTES);
                existing.setTimeSlot(shifted);
                existing.setAppointmentTime(shifted);
                appointmentRepository.save(existing);
            }
        }

        // Create the emergency appointment at the insert slot
        Appointment appt = new Appointment();
        appt.setPatient(patient);
        appt.setDoctor(doctor);
        appt.setAppointmentDate(date);
        appt.setTimeSlot(insertSlot);
        appt.setAppointmentTime(insertSlot);
        appt.setAppointmentType(Appointment.AppointmentType.EMERGENCY);
        appt.setStatus(Appointment.AppointmentStatus.APPROVAL_PENDING);
        appt.setTokenNumber("TK-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        appt = appointmentRepository.save(appt);

        createAppointmentFeeBill(appt, doctor);

        return mapToResponse(appt);
    }

    private void createAppointmentFeeBill(Appointment appt, Doctor doctor) {
        BigDecimal consultationFee = doctor.getConsultationFee() != null ? doctor.getConsultationFee() : new BigDecimal("500");
        Bill bill = new Bill();
        bill.setAppointment(appt);
        bill.setPatient(appt.getPatient());
        bill.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        bill.setIssueDate(LocalDateTime.now());
        bill.setConsultationCharge(consultationFee);
        bill.setMedicationCharge(BigDecimal.ZERO);
        bill.setLabCharge(BigDecimal.ZERO);
        bill.setTax(BigDecimal.ZERO);
        bill.setDiscount(BigDecimal.ZERO);
        bill.setTotalAmount(consultationFee);
        bill.setStatus(Bill.BillStatus.UNPAID);
        billRepository.save(bill);
    }

    public List<AppointmentResponse> getPatientAppointments(String patientEmail) {
        User user = userRepository.findByEmail(patientEmail).orElseThrow(() -> new RuntimeException("User not found"));
        Patient patient = patientRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Patient not found"));
        return appointmentRepository.findByPatientIdOrderByAppointmentDateDescTimeSlotDesc(patient.getId())
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    public List<AppointmentResponse> getDoctorAppointments(String doctorEmail, LocalDate date) {
        User user = userRepository.findByEmail(doctorEmail).orElseThrow(() -> new RuntimeException("User not found"));
        Doctor doctor = doctorRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Doctor profile not found"));
        return appointmentRepository.findByDoctorIdAndAppointmentDate(doctor.getId(), date)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    public List<AppointmentResponse> getDoctorUpcomingAppointments(String doctorEmail) {
        User user = userRepository.findByEmail(doctorEmail).orElseThrow(() -> new RuntimeException("User not found"));
        Doctor doctor = doctorRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Doctor profile not found"));
        return appointmentRepository
                .findByDoctorIdAndAppointmentDateGreaterThanEqualOrderByAppointmentDateAscTimeSlotAsc(doctor.getId(), LocalDate.now())
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    @Transactional
    public AppointmentResponse updateStatus(Integer id, String statusStr) {
        Appointment appt = appointmentRepository.findById(id).orElseThrow();
        appt.setStatus(Appointment.AppointmentStatus.valueOf(statusStr));
        return mapToResponse(appointmentRepository.save(appt));
    }
    @Transactional
    public AppointmentResponse cancelAppointment(Integer id, String reason) {
        Appointment appt = appointmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Appointment not found"));
        appt.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appt.setCancellationReason(reason);
        return mapToResponse(appointmentRepository.save(appt));
    }

    @Transactional
    public AppointmentResponse rescheduleAppointment(Integer id, LocalDate newDate, String newTimeSlot) {
        Appointment appt = appointmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Weekend check
        DayOfWeek dayOfWeek = newDate.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            throw new RuntimeException("Cannot reschedule to a weekend");
        }

        boolean taken = appointmentRepository.findByDoctorIdAndAppointmentDate(appt.getDoctor().getId(), newDate)
                .stream().anyMatch(a -> a.getTimeSlot().equals(LocalTime.parse(newTimeSlot)) && a.getStatus() != Appointment.AppointmentStatus.CANCELLED && !a.getId().equals(id));
        if (taken) {
            throw new RuntimeException("Time slot is no longer available");
        }

        appt.setAppointmentDate(newDate);
        LocalTime parsedTime = LocalTime.parse(newTimeSlot);
        appt.setTimeSlot(parsedTime);
        appt.setAppointmentTime(parsedTime);
        appt.setStatus(Appointment.AppointmentStatus.RESCHEDULED);

        return mapToResponse(appointmentRepository.save(appt));
    }

    public List<LocalTime> getAvailableSlots(Integer doctorId, LocalDate date) {
        // Weekend check — no slots on Saturday/Sunday
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return new ArrayList<>();
        }

        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow();
        DoctorSchedule schedule = doctor.getSchedule();

        LocalTime startTime = DEFAULT_START;
        LocalTime endTime = DEFAULT_END;

        if (schedule != null) {
            if (schedule.getLeaveDate() != null && schedule.getLeaveDate().equals(date)) {
                return new ArrayList<>();
            }
            String dayStr = date.getDayOfWeek().toString();
            String dayShort = dayStr.substring(0, 1).toUpperCase() + dayStr.substring(1, 3).toLowerCase();
            if (schedule.getWorkingDays() != null && !schedule.getWorkingDays().contains(dayShort)) {
                return new ArrayList<>();
            }
            if (schedule.getStartTime() != null) startTime = schedule.getStartTime();
            if (schedule.getEndTime() != null) endTime = schedule.getEndTime();
        }

        List<LocalTime> slots = new ArrayList<>();
        LocalTime curr = startTime;
        while (curr.isBefore(endTime)) {
            slots.add(curr);
            curr = curr.plusMinutes(SLOT_MINUTES);
        }

        List<LocalTime> bookedSlots = appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, date)
                .stream()
                .filter(a -> a.getStatus() != Appointment.AppointmentStatus.CANCELLED)
                .map(Appointment::getTimeSlot)
                .collect(Collectors.toList());
        slots.removeAll(bookedSlots);

        // Filter out past time slots if the date is today
        if (date.equals(LocalDate.now())) {
            LocalTime now = LocalTime.now();
            slots.removeIf(slot -> slot.isBefore(now));
        }

        return slots;
    }

    private AppointmentResponse mapToResponse(Appointment appt) {
        String doctorName = "Unknown";
        String specialization = "Unknown";
        Integer doctorId = null;
        if (appt.getDoctor() != null) {
            try {
                doctorId = appt.getDoctor().getId();
                doctorName = appt.getDoctor().getFirstName() + " " + appt.getDoctor().getLastName();
                specialization = appt.getDoctor().getSpecialization();
            } catch (Exception e) {
                doctorName = "Unknown";
            }
        }
        String patientName = "Unknown";
        Integer patientId = null;
        if (appt.getPatient() != null) {
            try {
                patientId = appt.getPatient().getId();
                patientName = appt.getPatient().getFirstName() + " " + appt.getPatient().getLastName();
            } catch (Exception e) {
                patientName = "Unknown";
            }
        }

        boolean hasLabReport = false;
        if (appt.getMedicalRecord() != null && appt.getMedicalRecord().getLabTestOrders() != null) {
            hasLabReport = appt.getMedicalRecord().getLabTestOrders().stream()
                    .anyMatch(l -> l.getStatus() == LabTestOrder.TestStatus.COMPLETED && l.getLabResult() != null);
        }

        return new AppointmentResponse(
                appt.getId(),
                doctorId,
                doctorName,
                specialization,
                patientId,
                patientName,
                appt.getAppointmentDate(),
                appt.getTimeSlot(),
                appt.getTokenNumber(),
                appt.getAppointmentType() != null ? appt.getAppointmentType().name() : "ROUTINE",
                appt.getStatus() != null ? appt.getStatus().name() : "APPROVAL_PENDING",
                hasLabReport
        );
    }
}
