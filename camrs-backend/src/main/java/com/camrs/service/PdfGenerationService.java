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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfGenerationService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final LabTestOrderRepository labTestOrderRepository;

    private static final float PAGE_TOP = 770;
    private static final float PAGE_BOTTOM = 50;
    private static final float MARGIN = 50;

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

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float y = PAGE_TOP;

            // Header
            y = drawText(cs, fontBold, 14, MARGIN, y, "CAMRS — Prescription");
            y -= 6;

            y = drawText(cs, fontNormal, 8, MARGIN, y,
                    "Date: " + record.getVisitDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")));
            y -= 4;

            // Doctor info
            y = drawText(cs, fontBold, 9, MARGIN, y,
                    "Dr. " + record.getDoctor().getFirstName() + " " + record.getDoctor().getLastName()
                            + " (" + record.getDoctor().getSpecialization() + ")");
            y -= 4;

            // Patient info
            y = drawText(cs, fontNormal, 8, MARGIN, y,
                    "Patient: " + record.getPatient().getFirstName() + " " + record.getPatient().getLastName());
            y -= 8;

            // Divider
            cs.moveTo(MARGIN, y);
            cs.lineTo(560, y);
            cs.stroke();
            y -= 14;

            // Diagnosis
            y = drawText(cs, fontBold, 8, MARGIN, y, "Diagnosis: ");
            // Wrap long diagnosis text
            String diagText = record.getDiagnosis() != null ? record.getDiagnosis() : "N/A";
            y = drawWrappedText(cs, fontNormal, 8, MARGIN + 60, y + 10, diagText, 450);
            y -= 4;

            y = drawText(cs, fontBold, 8, MARGIN, y, "Chief Complaint: ");
            String ccText = record.getChiefComplaint() != null ? record.getChiefComplaint() : "N/A";
            y = drawWrappedText(cs, fontNormal, 8, MARGIN + 90, y + 10, ccText, 420);
            y -= 12;

            // Medications table
            if (record.getPrescription() != null && record.getPrescription().getItems() != null
                    && !record.getPrescription().getItems().isEmpty()) {

                // Check page overflow
                if (y < PAGE_BOTTOM + 60) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = PAGE_TOP;
                }

                y = drawText(cs, fontBold, 10, MARGIN, y, "Prescribed Medications");
                y -= 14;

                // Table header
                float[] cols = {MARGIN, 180, 260, 340, 410, 480};
                String[] headers = {"Medication", "Dosage", "Frequency", "Duration", "Route", "Instructions"};
                for (int i = 0; i < headers.length; i++) {
                    drawTextInline(cs, fontBold, 7, cols[i], y, headers[i]);
                }
                y -= 10;

                cs.moveTo(MARGIN, y + 3);
                cs.lineTo(560, y + 3);
                cs.stroke();

                for (PrescriptionItem item : record.getPrescription().getItems()) {
                    if (y < PAGE_BOTTOM + 20) {
                        cs.close();
                        page = new PDPage(PDRectangle.A4);
                        doc.addPage(page);
                        cs = new PDPageContentStream(doc, page);
                        y = PAGE_TOP;
                    }

                    drawTextInline(cs, fontNormal, 7, cols[0], y, truncate(item.getMedication().getName(), 22));
                    drawTextInline(cs, fontNormal, 7, cols[1], y, item.getDosage() != null ? item.getDosage() : "");
                    drawTextInline(cs, fontNormal, 7, cols[2], y, item.getFrequency() != null ? item.getFrequency() : "");
                    drawTextInline(cs, fontNormal, 7, cols[3], y, item.getDuration() != null ? item.getDuration() : "");
                    drawTextInline(cs, fontNormal, 7, cols[4], y, item.getRoute() != null ? item.getRoute() : "");
                    drawTextInline(cs, fontNormal, 7, cols[5], y, item.getMealInstruction() != null ? item.getMealInstruction() : "");
                    y -= 12;
                }
            }

            y -= 10;
            // Advice
            if (record.getAdvice() != null && !record.getAdvice().isEmpty()) {
                if (y < PAGE_BOTTOM + 30) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = PAGE_TOP;
                }
                y = drawText(cs, fontBold, 8, MARGIN, y, "Advice: ");
                y = drawWrappedText(cs, fontNormal, 8, MARGIN + 40, y + 10, record.getAdvice(), 470);
                y -= 4;
            }

            if (record.getFollowUpDate() != null) {
                y = drawText(cs, fontBold, 8, MARGIN, y, "Follow-up: " +
                        record.getFollowUpDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            }

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

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float y = PAGE_TOP;

            y = drawText(cs, fontBold, 14, MARGIN, y, "CAMRS — Laboratory Report");
            y -= 6;

            y = drawText(cs, fontNormal, 8, MARGIN, y,
                    "Date: " + order.getOrderDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")));
            y -= 4;

            y = drawText(cs, fontNormal, 8, MARGIN, y,
                    "Patient: " + order.getPatient().getFirstName() + " " + order.getPatient().getLastName());
            y -= 4;

            y = drawText(cs, fontNormal, 8, MARGIN, y,
                    "Ordered by: Dr. " + order.getDoctor().getFirstName() + " " + order.getDoctor().getLastName());
            y -= 10;

            cs.moveTo(MARGIN, y);
            cs.lineTo(560, y);
            cs.stroke();
            y -= 14;

            y = drawText(cs, fontBold, 10, MARGIN, y, "Test: " + order.getTestType());
            y -= 4;

            y = drawText(cs, fontNormal, 8, MARGIN, y,
                    "Priority: " + order.getPriority().name() + "   |   Status: " + order.getStatus().name());
            y -= 14;

            if (order.getLabResult() != null) {
                LabResult result = order.getLabResult();
                y = drawText(cs, fontBold, 9, MARGIN, y, "Results");
                y -= 4;

                y = drawText(cs, fontNormal, 8, MARGIN, y,
                        "Value: " + (result.getResultValue() != null ? result.getResultValue() : "N/A")
                                + " " + (result.getUnit() != null ? result.getUnit() : ""));
                y -= 4;

                y = drawText(cs, fontNormal, 8, MARGIN, y,
                        "Reference Range: " + (result.getReferenceRange() != null ? result.getReferenceRange() : "N/A"));
                y -= 4;

                if (result.getIsCritical() != null && result.getIsCritical()) {
                    y = drawText(cs, fontBold, 9, MARGIN, y, "*** CRITICAL VALUE ***");
                    y -= 4;
                }

                if (result.getNotes() != null && !result.getNotes().isEmpty()) {
                    y = drawWrappedText(cs, fontNormal, 8, MARGIN, y, "Notes: " + result.getNotes(), 510);
                }
            } else {
                y = drawText(cs, fontNormal, 8, MARGIN, y, "Results: Pending");
            }

            cs.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    // --- Helper methods ---

    private float drawText(PDPageContentStream cs, PDType1Font font, float size, float x, float y, String text) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text != null ? text : "");
        cs.endText();
        return y - size - 4;
    }

    private void drawTextInline(PDPageContentStream cs, PDType1Font font, float size, float x, float y, String text) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text != null ? text : "");
        cs.endText();
    }

    private float drawWrappedText(PDPageContentStream cs, PDType1Font font, float size, float x, float startY, String text, float maxWidth) throws IOException {
        if (text == null || text.isEmpty()) return startY;
        List<String> lines = wrapText(text, font, size, maxWidth);
        float y = startY;
        for (String line : lines) {
            cs.beginText();
            cs.setFont(font, size);
            cs.newLineAtOffset(x, y);
            cs.showText(line);
            cs.endText();
            y -= size + 3;
        }
        return y;
    }

    private List<String> wrapText(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
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
        return lines;
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen - 2) + ".." : text;
    }
}
