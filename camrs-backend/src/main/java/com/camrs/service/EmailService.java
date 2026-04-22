package com.camrs.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${camrs.notifications.email-enabled:false}")
    private boolean emailEnabled;

    @Value("${spring.mail.username:noreply@camrs.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ──────────────── 1. Welcome Email ────────────────

    @Async
    public void sendWelcomeEmail(String toEmail, String patientName) {
        send(toEmail, "Welcome to CAMRS - Registration Successful",
                wrapTemplate("Welcome, " + patientName + "!",
                        """
                        <p>Your account has been successfully created on the <strong>CAMRS</strong> platform.</p>
                        <div class="info-box">
                            <p><strong>What you can do:</strong></p>
                            <ul>
                                <li>Book and manage appointments with our doctors</li>
                                <li>View your medical records and prescriptions</li>
                                <li>Access lab test results online</li>
                                <li>Track your billing and payment history</li>
                                <li>Download prescriptions and lab reports as PDFs</li>
                            </ul>
                        </div>
                        <p>To get started, log in to the patient portal using your registered credentials.</p>
                        <div style="text-align:center;">
                            <a href="http://localhost:5173/login" class="btn">Log In to CAMRS</a>
                        </div>
                        """));
    }

    // ──────────────── 2. Appointment Booked ────────────────

    @Async
    public void sendAppointmentConfirmation(String toEmail, String patientName,
                                             String doctorName, String date, String time, String tokenNumber) {
        send(toEmail, "CAMRS - Appointment Confirmed | Token: " + tokenNumber,
                wrapTemplate("Appointment Confirmed",
                        """
                        <p>Dear <strong>%s</strong>, your appointment has been successfully booked.</p>
                        <div class="info-box">
                            <table>
                                <tr><td style="color:#888;padding:4px 16px 4px 0;">Doctor</td><td><strong>Dr. %s</strong></td></tr>
                                <tr><td style="color:#888;padding:4px 16px 4px 0;">Date</td><td><strong>%s</strong></td></tr>
                                <tr><td style="color:#888;padding:4px 16px 4px 0;">Time</td><td><strong>%s</strong></td></tr>
                                <tr><td style="color:#888;padding:4px 16px 4px 0;">Token</td><td style="font-size:18px;color:#29629b;"><strong>%s</strong></td></tr>
                            </table>
                        </div>
                        <p style="font-size:13px;color:#888;">Your appointment is pending doctor approval. You will be notified once it is confirmed. Please arrive 10 minutes before your scheduled time.</p>
                        """.formatted(patientName, doctorName, date, time, tokenNumber)));
    }

    // ──────────────── 3. Appointment Rescheduled ────────────────

    @Async
    public void sendAppointmentRescheduled(String toEmail, String patientName,
                                            String doctorName, String newDate, String newTime) {
        send(toEmail, "CAMRS - Appointment Rescheduled",
                wrapTemplate("Appointment Rescheduled",
                        """
                        <p>Dear <strong>%s</strong>, your appointment has been rescheduled.</p>
                        <div class="info-box">
                            <table>
                                <tr><td style="color:#888;padding:4px 16px 4px 0;">Doctor</td><td><strong>Dr. %s</strong></td></tr>
                                <tr><td style="color:#888;padding:4px 16px 4px 0;">New Date</td><td><strong>%s</strong></td></tr>
                                <tr><td style="color:#888;padding:4px 16px 4px 0;">New Time</td><td><strong>%s</strong></td></tr>
                            </table>
                        </div>
                        <p style="font-size:13px;color:#888;">Please note the updated schedule. If you have any concerns, contact the clinic.</p>
                        """.formatted(patientName, doctorName, newDate, newTime)));
    }

    // ──────────────── 4. Critical Lab Result ────────────────

    @Async
    public void sendCriticalLabResult(String toEmail, String patientName,
                                       String testName, String resultValue, String unit, String referenceRange) {
        send(toEmail, "CAMRS - URGENT: Critical Lab Result",
                wrapTemplate("[!] Critical Lab Result",
                        """
                        <p>Dear <strong>%s</strong>, a lab test result has been flagged as <strong style="color:#b22828;">CRITICAL</strong>.</p>
                        <div style="background:#fff0f0;border-left:4px solid #b22828;padding:16px 20px;border-radius:6px;margin:20px 0;">
                            <table>
                                <tr><td style="color:#888;padding:4px 16px 4px 0;">Test</td><td><strong>%s</strong></td></tr>
                                <tr><td style="color:#888;padding:4px 16px 4px 0;">Result</td><td style="color:#b22828;"><strong>%s %s</strong></td></tr>
                                <tr><td style="color:#888;padding:4px 16px 4px 0;">Reference</td><td>%s</td></tr>
                            </table>
                        </div>
                        <p><strong>Please contact the clinic or your doctor immediately.</strong></p>
                        <p style="font-size:13px;color:#888;">You can view the full lab report by logging into your CAMRS patient portal.</p>
                        <div style="text-align:center;">
                            <a href="http://localhost:5173/login" class="btn" style="background:#b22828;">View Lab Results</a>
                        </div>
                        """.formatted(patientName, testName,
                                resultValue != null ? resultValue : "N/A",
                                unit != null ? unit : "",
                                referenceRange != null ? referenceRange : "N/A")));
    }

    // ──────────────── 5. Password Reset Confirmation ────────────────

    @Async
    public void sendPasswordResetConfirmation(String toEmail, String patientName) {
        send(toEmail, "CAMRS - Password Reset Successful",
                wrapTemplate("Password Reset Successful",
                        """
                        <p>Dear <strong>%s</strong>, your password has been successfully reset.</p>
                        <div class="info-box">
                            <p>You can now log in with your new password.</p>
                        </div>
                        <p style="font-size:13px;color:#888;">If you did not request this change, please contact the clinic administrator immediately.</p>
                        <div style="text-align:center;">
                            <a href="http://localhost:5173/login" class="btn">Log In Now</a>
                        </div>
                        """.formatted(patientName)));
    }

    // ──────────────── Core Send ────────────────

    private void send(String toEmail, String subject, String htmlBody) {
        if (!emailEnabled) {
            System.out.println("[EMAIL-DISABLED] Skipping: " + subject + " to " + toEmail);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            System.out.println("[EMAIL] Sent: " + subject + " to " + toEmail);
        } catch (MessagingException e) {
            System.err.println("[EMAIL-ERROR] Failed: " + subject + " to " + toEmail + " - " + e.getMessage());
        }
    }

    // ──────────────── HTML Template Wrapper ────────────────

    private String wrapTemplate(String heading, String bodyContent) {
        return """
            <!DOCTYPE html>
            <html><head><meta charset="UTF-8"><style>
                body{font-family:'Segoe UI',Tahoma,sans-serif;background:#f4f7fb;margin:0;padding:0;}
                .container{max-width:600px;margin:30px auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);}
                .header{background:linear-gradient(135deg,#1e3a5f 0%%,#29629b 100%%);padding:28px 40px;text-align:center;}
                .header h1{color:#fff;margin:0;font-size:26px;letter-spacing:2px;}
                .header p{color:#b8d4f0;margin:6px 0 0;font-size:12px;}
                .body-content{padding:32px 40px;color:#333;}
                .body-content h2{color:#1e3a5f;font-size:19px;margin-top:0;}
                .body-content p{line-height:1.7;font-size:14px;color:#555;}
                .info-box{background:#f0f5fa;border-left:4px solid #29629b;padding:16px 20px;border-radius:6px;margin:20px 0;}
                .info-box p{margin:4px 0;font-size:14px;color:#1e3a5f;}
                .info-box ul{margin:8px 0;padding-left:20px;}
                .info-box li{padding:3px 0;font-size:13px;color:#555;}
                .btn{display:inline-block;background:#29629b;color:#ffffff;padding:12px 28px;border-radius:8px;text-decoration:none;font-weight:600;font-size:14px;margin:16px 0;}
                .footer{background:#f8f9fb;padding:16px 40px;text-align:center;border-top:1px solid #e8ecf0;}
                .footer p{font-size:11px;color:#999;margin:3px 0;}
                table{border-collapse:collapse;} td{padding:4px 0;font-size:14px;color:#333;}
            </style></head>
            <body><div class="container">
                <div class="header"><h1>CAMRS</h1><p>Clinic Appointment & Medical Records System</p></div>
                <div class="body-content"><h2>%s</h2>%s</div>
                <div class="footer">
                    <p>&copy; 2026 CAMRS - Clinic Appointment & Medical Records System</p>
                    <p>This is an automated email. Please do not reply.</p>
                </div>
            </div></body></html>
            """.formatted(heading, bodyContent);
    }
}
