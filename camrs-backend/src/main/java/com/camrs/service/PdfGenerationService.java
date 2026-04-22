package com.camrs.service;

import com.camrs.entity.*;
import com.camrs.repository.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfGenerationService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final LabTestOrderRepository labTestOrderRepository;

    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_TOP = 780;
    private static final float PAGE_BOTTOM = 60;
    private static final float MARGIN_LEFT = 50;
    private static final float MARGIN_RIGHT = 50;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT;

    // Colors
    private static final Color COLOR_PRIMARY = new Color(30, 58, 95);       // Dark navy
    private static final Color COLOR_ACCENT = new Color(41, 98, 155);       // Medium blue
    private static final Color COLOR_LIGHT_BG = new Color(240, 245, 250);   // Light blue-gray
    private static final Color COLOR_BORDER = new Color(180, 200, 220);     // Soft border
    private static final Color COLOR_TEXT = new Color(50, 50, 50);          // Dark gray text
    private static final Color COLOR_MUTED = new Color(120, 130, 140);      // Muted text
    private static final Color COLOR_RED = new Color(180, 40, 40);          // Critical red
    private static final Color COLOR_WHITE = Color.WHITE;

    public PdfGenerationService(MedicalRecordRepository medicalRecordRepository,
                                LabTestOrderRepository labTestOrderRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.labTestOrderRepository = labTestOrderRepository;
    }

    public byte[] generatePrescriptionPdfByAppointment(Integer appointmentId) throws IOException {
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Medical record not found for appointment"));
        return generatePrescriptionPdf(record.getId());
    }

    public byte[] generatePrescriptionPdf(Integer medicalRecordId) throws IOException {
        MedicalRecord record = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found"));

        try (PDDocument doc = new PDDocument()) {
            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontItalic = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);

            // === HEADER BAND ===
            float y = PAGE_TOP;
            drawRect(cs, 0, y - 5, PAGE_WIDTH, 55, COLOR_PRIMARY, true);
            drawTextColored(cs, fontBold, 18, MARGIN_LEFT, y + 25, "CAMRS", COLOR_WHITE);
            drawTextColored(cs, fontNormal, 8, MARGIN_LEFT, y + 10, "Clinic Appointment & Medical Records System", COLOR_WHITE);
            drawTextColored(cs, fontBold, 13, PAGE_WIDTH - MARGIN_RIGHT - 130, y + 25, "PRESCRIPTION", COLOR_WHITE);
            drawTextColored(cs, fontNormal, 7, PAGE_WIDTH - MARGIN_RIGHT - 130, y + 10,
                    "Date: " + record.getVisitDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")), COLOR_WHITE);
            y -= 75;

            // === DOCTOR & PATIENT INFO (Two columns) ===
            drawRect(cs, MARGIN_LEFT, y - 55, CONTENT_WIDTH, 60, COLOR_LIGHT_BG, true);
            drawRect(cs, MARGIN_LEFT, y - 55, CONTENT_WIDTH, 60, COLOR_BORDER, false);

            // Doctor column
            drawTextColored(cs, fontBold, 7, MARGIN_LEFT + 10, y - 2, "PRESCRIBING PHYSICIAN", COLOR_MUTED);
            drawTextColored(cs, fontBold, 10, MARGIN_LEFT + 10, y - 16,
                    "Dr. " + record.getDoctor().getFirstName() + " " + record.getDoctor().getLastName(), COLOR_PRIMARY);
            drawTextColored(cs, fontNormal, 8, MARGIN_LEFT + 10, y - 30, record.getDoctor().getSpecialization(), COLOR_TEXT);
            if (record.getDoctor().getLicenseNumber() != null) {
                drawTextColored(cs, fontNormal, 7, MARGIN_LEFT + 10, y - 42,
                        "Lic: " + record.getDoctor().getLicenseNumber(), COLOR_MUTED);
            }

            // Patient column
            float colRight = MARGIN_LEFT + CONTENT_WIDTH / 2 + 10;
            drawTextColored(cs, fontBold, 7, colRight, y - 2, "PATIENT DETAILS", COLOR_MUTED);
            drawTextColored(cs, fontBold, 10, colRight, y - 16,
                    record.getPatient().getFirstName() + " " + record.getPatient().getLastName(), COLOR_PRIMARY);
            String patientMeta = "";
            if (record.getPatient().getAge() != null) patientMeta += "Age: " + record.getPatient().getAge();
            if (record.getPatient().getGender() != null) patientMeta += "  |  " + record.getPatient().getGender();
            drawTextColored(cs, fontNormal, 8, colRight, y - 30, patientMeta, COLOR_TEXT);
            drawTextColored(cs, fontNormal, 7, colRight, y - 42,
                    "Patient ID: " + record.getPatient().getId(), COLOR_MUTED);

            // Vertical divider
            cs.setStrokingColor(COLOR_BORDER);
            cs.moveTo(MARGIN_LEFT + CONTENT_WIDTH / 2, y + 4);
            cs.lineTo(MARGIN_LEFT + CONTENT_WIDTH / 2, y - 54);
            cs.stroke();

            y -= 75;

            // === CLINICAL INFO SECTION ===
            drawTextColored(cs, fontBold, 9, MARGIN_LEFT, y, "CLINICAL INFORMATION", COLOR_ACCENT);
            y -= 4;
            drawLine(cs, MARGIN_LEFT, y, MARGIN_LEFT + CONTENT_WIDTH, y, COLOR_ACCENT, 1.5f);
            y -= 16;

            // Diagnosis
            drawTextColored(cs, fontBold, 8, MARGIN_LEFT, y, "Diagnosis:", COLOR_TEXT);
            String diagText = record.getDiagnosis() != null ? record.getDiagnosis() : "N/A";
            if (record.getIcd10Code() != null) {
                diagText += " [" + record.getIcd10Code().getCode() + "]";
            }
            y = drawWrappedText(cs, fontNormal, 8, MARGIN_LEFT + 65, y, diagText, CONTENT_WIDTH - 65, COLOR_TEXT);
            y -= 6;

            // Chief Complaint
            drawTextColored(cs, fontBold, 8, MARGIN_LEFT, y, "Complaint:", COLOR_TEXT);
            String ccText = record.getChiefComplaint() != null ? record.getChiefComplaint() : "N/A";
            y = drawWrappedText(cs, fontNormal, 8, MARGIN_LEFT + 65, y, ccText, CONTENT_WIDTH - 65, COLOR_TEXT);
            y -= 6;

            // Vital Signs
            if (record.getVitalSigns() != null && !record.getVitalSigns().isEmpty()) {
                drawTextColored(cs, fontBold, 8, MARGIN_LEFT, y, "Vitals:", COLOR_TEXT);
                y = drawWrappedText(cs, fontNormal, 8, MARGIN_LEFT + 65, y, record.getVitalSigns(), CONTENT_WIDTH - 65, COLOR_TEXT);
                y -= 6;
            }

            // Severity
            if (record.getSeverity() != null) {
                drawTextColored(cs, fontBold, 8, MARGIN_LEFT, y, "Severity:", COLOR_TEXT);
                Color sevColor = record.getSeverity() == MedicalRecord.Severity.CRITICAL || record.getSeverity() == MedicalRecord.Severity.HIGH
                        ? COLOR_RED : COLOR_TEXT;
                drawTextColored(cs, fontBold, 8, MARGIN_LEFT + 65, y, record.getSeverity().name(), sevColor);
                y -= 14;
            }
            y -= 8;

            // === MEDICATIONS TABLE ===
            if (record.getPrescription() != null && record.getPrescription().getItems() != null
                    && !record.getPrescription().getItems().isEmpty()) {

                if (y < PAGE_BOTTOM + 80) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = PAGE_TOP;
                }

                drawTextColored(cs, fontBold, 9, MARGIN_LEFT, y, "PRESCRIBED MEDICATIONS", COLOR_ACCENT);
                y -= 4;
                drawLine(cs, MARGIN_LEFT, y, MARGIN_LEFT + CONTENT_WIDTH, y, COLOR_ACCENT, 1.5f);
                y -= 18;

                // Table header
                float[] cols = {MARGIN_LEFT, MARGIN_LEFT + 140, MARGIN_LEFT + 210, MARGIN_LEFT + 290, MARGIN_LEFT + 360, MARGIN_LEFT + 420};
                String[] headers = {"Medication", "Dosage", "Frequency", "Duration", "Route", "Instructions"};

                drawRect(cs, MARGIN_LEFT, y - 4, CONTENT_WIDTH, 16, COLOR_PRIMARY, true);
                for (int i = 0; i < headers.length; i++) {
                    drawTextColored(cs, fontBold, 7, cols[i] + 4, y, headers[i], COLOR_WHITE);
                }
                y -= 18;

                boolean alternate = false;
                for (PrescriptionItem item : record.getPrescription().getItems()) {
                    if (y < PAGE_BOTTOM + 20) {
                        cs.close();
                        page = new PDPage(PDRectangle.A4);
                        doc.addPage(page);
                        cs = new PDPageContentStream(doc, page);
                        y = PAGE_TOP;
                    }

                    if (alternate) {
                        drawRect(cs, MARGIN_LEFT, y - 4, CONTENT_WIDTH, 14, COLOR_LIGHT_BG, true);
                    }
                    alternate = !alternate;

                    drawTextColored(cs, fontNormal, 7, cols[0] + 4, y, truncate(item.getMedication().getName(), 26), COLOR_TEXT);
                    drawTextColored(cs, fontNormal, 7, cols[1] + 4, y, item.getDosage() != null ? item.getDosage() : "-", COLOR_TEXT);
                    drawTextColored(cs, fontNormal, 7, cols[2] + 4, y, item.getFrequency() != null ? item.getFrequency() : "", COLOR_TEXT);
                    drawTextColored(cs, fontNormal, 7, cols[3] + 4, y, item.getDuration() != null ? item.getDuration() : "", COLOR_TEXT);
                    drawTextColored(cs, fontNormal, 7, cols[4] + 4, y, item.getRoute() != null ? item.getRoute() : "", COLOR_TEXT);
                    drawTextColored(cs, fontNormal, 7, cols[5] + 4, y, item.getMealInstruction() != null ? item.getMealInstruction() : "", COLOR_TEXT);
                    y -= 14;
                }

                // Table bottom border
                drawLine(cs, MARGIN_LEFT, y + 10, MARGIN_LEFT + CONTENT_WIDTH, y + 10, COLOR_BORDER, 0.5f);
            }

            y -= 16;

            // === ADVICE ===
            if (record.getAdvice() != null && !record.getAdvice().isEmpty()) {
                if (y < PAGE_BOTTOM + 40) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = PAGE_TOP;
                }

                drawTextColored(cs, fontBold, 9, MARGIN_LEFT, y, "ADVICE", COLOR_ACCENT);
                y -= 4;
                drawLine(cs, MARGIN_LEFT, y, MARGIN_LEFT + CONTENT_WIDTH, y, COLOR_ACCENT, 1.5f);
                y -= 14;
                // Advice box
                List<String> adviceLines = wrapText(record.getAdvice(), fontNormal, 8, CONTENT_WIDTH - 20);
                float boxH = adviceLines.size() * 12 + 14;
                drawRect(cs, MARGIN_LEFT, y - boxH + 10, CONTENT_WIDTH, boxH, COLOR_LIGHT_BG, true);
                drawRect(cs, MARGIN_LEFT, y - boxH + 10, CONTENT_WIDTH, boxH, COLOR_BORDER, false);
                for (String line : adviceLines) {
                    drawTextColored(cs, fontNormal, 8, MARGIN_LEFT + 10, y, line, COLOR_TEXT);
                    y -= 12;
                }
                y -= 8;
            }

            // === FOLLOW-UP ===
            if (record.getFollowUpDate() != null) {
                if (y < PAGE_BOTTOM + 30) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = PAGE_TOP;
                }
                drawRect(cs, MARGIN_LEFT, y - 10, CONTENT_WIDTH, 22, new Color(255, 248, 230), true);
                drawRect(cs, MARGIN_LEFT, y - 10, CONTENT_WIDTH, 22, new Color(230, 200, 120), false);
                drawTextColored(cs, fontBold, 8, MARGIN_LEFT + 10, y - 2,
                        "FOLLOW-UP DATE:  " + record.getFollowUpDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                        COLOR_PRIMARY);
                y -= 30;
            }

            // === FOOTER ===
            drawLine(cs, MARGIN_LEFT, PAGE_BOTTOM + 15, MARGIN_LEFT + CONTENT_WIDTH, PAGE_BOTTOM + 15, COLOR_BORDER, 0.5f);
            drawTextColored(cs, fontItalic, 6, MARGIN_LEFT, PAGE_BOTTOM + 5,
                    "This is a computer-generated prescription from CAMRS. Please consult your doctor before making changes to medication.", COLOR_MUTED);
            drawTextColored(cs, fontNormal, 6, PAGE_WIDTH - MARGIN_RIGHT - 100, PAGE_BOTTOM + 5,
                    "Generated: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), COLOR_MUTED);

            cs.close();

            // Update print timestamp
            if (record.getPrescription() != null) {
                record.getPrescription().setPrintTimestamp(java.time.LocalDateTime.now());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    public byte[] generateLabReportPdfByAppointment(Integer appointmentId) throws IOException {
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Medical record not found for appointment"));
        LabTestOrder order = labTestOrderRepository.findByMedicalRecordId(record.getId()).stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Lab order not found for appointment"));
        return generateLabReportPdf(order.getId());
    }

    public byte[] generateLabReportPdf(Integer labOrderId) throws IOException {
        LabTestOrder order = labTestOrderRepository.findById(labOrderId)
                .orElseThrow(() -> new RuntimeException("Lab order not found"));

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontItalic = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float y = PAGE_TOP;

            // === HEADER BAND ===
            drawRect(cs, 0, y - 5, PAGE_WIDTH, 55, COLOR_PRIMARY, true);
            drawTextColored(cs, fontBold, 18, MARGIN_LEFT, y + 25, "CAMRS", COLOR_WHITE);
            drawTextColored(cs, fontNormal, 8, MARGIN_LEFT, y + 10, "Clinic Appointment & Medical Records System", COLOR_WHITE);
            drawTextColored(cs, fontBold, 13, PAGE_WIDTH - MARGIN_RIGHT - 145, y + 25, "LABORATORY REPORT", COLOR_WHITE);
            drawTextColored(cs, fontNormal, 7, PAGE_WIDTH - MARGIN_RIGHT - 145, y + 10,
                    "Date: " + order.getOrderDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")), COLOR_WHITE);
            y -= 75;

            // === PATIENT & DOCTOR INFO ===
            drawRect(cs, MARGIN_LEFT, y - 55, CONTENT_WIDTH, 60, COLOR_LIGHT_BG, true);
            drawRect(cs, MARGIN_LEFT, y - 55, CONTENT_WIDTH, 60, COLOR_BORDER, false);

            drawTextColored(cs, fontBold, 7, MARGIN_LEFT + 10, y - 2, "PATIENT", COLOR_MUTED);
            drawTextColored(cs, fontBold, 10, MARGIN_LEFT + 10, y - 16,
                    order.getPatient().getFirstName() + " " + order.getPatient().getLastName(), COLOR_PRIMARY);
            String pMeta = "";
            if (order.getPatient().getAge() != null) pMeta += "Age: " + order.getPatient().getAge();
            if (order.getPatient().getGender() != null) pMeta += "  |  " + order.getPatient().getGender();
            drawTextColored(cs, fontNormal, 8, MARGIN_LEFT + 10, y - 30, pMeta, COLOR_TEXT);

            float colR = MARGIN_LEFT + CONTENT_WIDTH / 2 + 10;
            drawTextColored(cs, fontBold, 7, colR, y - 2, "ORDERED BY", COLOR_MUTED);
            drawTextColored(cs, fontBold, 10, colR, y - 16,
                    "Dr. " + order.getDoctor().getFirstName() + " " + order.getDoctor().getLastName(), COLOR_PRIMARY);
            drawTextColored(cs, fontNormal, 8, colR, y - 30, order.getDoctor().getSpecialization(), COLOR_TEXT);

            cs.setStrokingColor(COLOR_BORDER);
            cs.moveTo(MARGIN_LEFT + CONTENT_WIDTH / 2, y + 4);
            cs.lineTo(MARGIN_LEFT + CONTENT_WIDTH / 2, y - 54);
            cs.stroke();

            y -= 80;

            // === TEST DETAILS ===
            drawTextColored(cs, fontBold, 9, MARGIN_LEFT, y, "TEST DETAILS", COLOR_ACCENT);
            y -= 4;
            drawLine(cs, MARGIN_LEFT, y, MARGIN_LEFT + CONTENT_WIDTH, y, COLOR_ACCENT, 1.5f);
            y -= 18;

            // Info grid
            String[][] testInfo = {
                    {"Test Name", order.getTestType()},
                    {"Priority", order.getPriority().name()},
                    {"Status", order.getStatus().name()},
            };
            for (String[] pair : testInfo) {
                drawTextColored(cs, fontBold, 8, MARGIN_LEFT, y, pair[0] + ":", COLOR_TEXT);
                Color valColor = "STAT".equals(pair[1]) || "URGENT".equals(pair[1]) ? COLOR_RED : COLOR_TEXT;
                drawTextColored(cs, fontNormal, 8, MARGIN_LEFT + 80, y, pair[1], valColor);
                y -= 14;
            }

            if (order.getSpecialInstructions() != null && !order.getSpecialInstructions().isEmpty()) {
                drawTextColored(cs, fontBold, 8, MARGIN_LEFT, y, "Instructions:", COLOR_TEXT);
                y = drawWrappedText(cs, fontNormal, 8, MARGIN_LEFT + 80, y, order.getSpecialInstructions(), CONTENT_WIDTH - 80, COLOR_TEXT);
                y -= 6;
            }

            y -= 14;

            // === RESULTS ===
            drawTextColored(cs, fontBold, 9, MARGIN_LEFT, y, "RESULTS", COLOR_ACCENT);
            y -= 4;
            drawLine(cs, MARGIN_LEFT, y, MARGIN_LEFT + CONTENT_WIDTH, y, COLOR_ACCENT, 1.5f);
            y -= 18;

            if (order.getLabResult() != null) {
                LabResult result = order.getLabResult();

                // Results box
                float boxTop = y + 6;

                drawTextColored(cs, fontBold, 8, MARGIN_LEFT + 10, y, "Value:", COLOR_TEXT);
                String valStr = (result.getResultValue() != null ? result.getResultValue() : "N/A")
                        + (result.getUnit() != null ? " " + result.getUnit() : "");
                drawTextColored(cs, fontBold, 10, MARGIN_LEFT + 80, y, valStr, COLOR_PRIMARY);
                y -= 16;

                drawTextColored(cs, fontBold, 8, MARGIN_LEFT + 10, y, "Reference:", COLOR_TEXT);
                drawTextColored(cs, fontNormal, 8, MARGIN_LEFT + 80, y,
                        result.getReferenceRange() != null ? result.getReferenceRange() : "N/A", COLOR_TEXT);
                y -= 16;

                if (result.getIsCritical() != null && result.getIsCritical()) {
                    drawRect(cs, MARGIN_LEFT, y - 4, CONTENT_WIDTH, 18, new Color(255, 235, 235), true);
                    drawRect(cs, MARGIN_LEFT, y - 4, CONTENT_WIDTH, 18, COLOR_RED, false);
                    drawTextColored(cs, fontBold, 9, MARGIN_LEFT + 10, y,
                            "[!] CRITICAL VALUE - Immediate attention required", COLOR_RED);
                    y -= 22;
                }

                // Draw box around results
                float boxBottom = y + 6;
                drawRect(cs, MARGIN_LEFT, boxBottom, CONTENT_WIDTH, boxTop - boxBottom, COLOR_LIGHT_BG, true);
                // Re-draw text on top of box (overlay approach — draw box first then reposition)

                if (result.getNotes() != null && !result.getNotes().isEmpty()) {
                    y -= 6;
                    drawTextColored(cs, fontBold, 8, MARGIN_LEFT, y, "Lab Notes:", COLOR_TEXT);
                    y -= 2;
                    y = drawWrappedText(cs, fontNormal, 8, MARGIN_LEFT, y - 10, result.getNotes(), CONTENT_WIDTH, COLOR_TEXT);
                }
            } else {
                drawRect(cs, MARGIN_LEFT, y - 8, CONTENT_WIDTH, 26, COLOR_LIGHT_BG, true);
                drawRect(cs, MARGIN_LEFT, y - 8, CONTENT_WIDTH, 26, COLOR_BORDER, false);
                drawTextColored(cs, fontItalic, 9, MARGIN_LEFT + 10, y, "Results pending - sample being processed", COLOR_MUTED);
                y -= 30;
            }

            // === FOOTER ===
            drawLine(cs, MARGIN_LEFT, PAGE_BOTTOM + 15, MARGIN_LEFT + CONTENT_WIDTH, PAGE_BOTTOM + 15, COLOR_BORDER, 0.5f);
            drawTextColored(cs, fontItalic, 6, MARGIN_LEFT, PAGE_BOTTOM + 5,
                    "This is a computer-generated lab report from CAMRS. For queries, contact the laboratory department.", COLOR_MUTED);
            drawTextColored(cs, fontNormal, 6, PAGE_WIDTH - MARGIN_RIGHT - 100, PAGE_BOTTOM + 5,
                    "Generated: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), COLOR_MUTED);

            cs.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    // --- Drawing Helpers ---

    private void drawTextColored(PDPageContentStream cs, PDType1Font font, float size, float x, float y, String text, Color color) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(color);
        cs.newLineAtOffset(x, y);
        // Sanitize: PDFBox showText cannot handle newlines, tabs, or non-WinAnsi chars
        String safe = (text != null ? text : "").replaceAll("[\\r\\n\\t]", " ").trim();
        cs.showText(safe);
        cs.endText();
    }

    private void drawRect(PDPageContentStream cs, float x, float y, float w, float h, Color color, boolean fill) throws IOException {
        cs.addRect(x, y, w, h);
        if (fill) {
            cs.setNonStrokingColor(color);
            cs.fill();
        } else {
            cs.setStrokingColor(color);
            cs.setLineWidth(0.5f);
            cs.stroke();
        }
    }

    private void drawLine(PDPageContentStream cs, float x1, float y1, float x2, float y2, Color color, float width) throws IOException {
        cs.setStrokingColor(color);
        cs.setLineWidth(width);
        cs.moveTo(x1, y1);
        cs.lineTo(x2, y2);
        cs.stroke();
    }

    private float drawWrappedText(PDPageContentStream cs, PDType1Font font, float size, float x, float startY, String text, float maxWidth, Color color) throws IOException {
        if (text == null || text.isEmpty()) return startY;
        List<String> lines = wrapText(text, font, size, maxWidth);
        float y = startY;
        for (String line : lines) {
            drawTextColored(cs, font, size, x, y, line, color);
            y -= size + 3;
        }
        return y;
    }

    private List<String> wrapText(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        // First split on newlines to respect explicit line breaks
        String[] paragraphs = text.split("[\\r\\n]+");
        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) continue;
            String[] words = trimmed.split("\\s+");
            StringBuilder currentLine = new StringBuilder();
            for (String word : words) {
                String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
                float testWidth = font.getStringWidth(testLine) / 1000 * fontSize;
                if (testWidth > maxWidth && currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    currentLine = new StringBuilder(testLine);
                }
            }
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }
        return lines;
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen - 2) + ".." : text;
    }
}
