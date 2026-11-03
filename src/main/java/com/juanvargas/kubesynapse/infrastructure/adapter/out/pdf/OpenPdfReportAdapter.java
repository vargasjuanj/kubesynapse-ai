package com.juanvargas.kubesynapse.infrastructure.adapter.out.pdf;

import com.juanvargas.kubesynapse.application.port.out.PdfReportPort;
import com.juanvargas.kubesynapse.domain.model.DiagnosticReport;
import com.juanvargas.kubesynapse.domain.model.cka.CkaAssertionCheck;
import com.juanvargas.kubesynapse.domain.model.cka.CkaExercise;
import com.juanvargas.kubesynapse.domain.model.cka.CkaVerificationResult;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * ======================================================================================
 * ADAPTADOR DE SALIDA (Driven Adapter): GENERADOR DE REPORTES PDF (OPENPDF / JASPER)
 * ======================================================================================
 * Genera informes imprimibles de incidentes SRE y certificados de evaluación CKA.
 * ======================================================================================
 */
@Slf4j
@Component
public class OpenPdfReportAdapter implements PdfReportPort {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(15, 23, 42));
    private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(100, 116, 139));
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(30, 41, 59));
    private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(51, 65, 85));
    private static final Font CODE_FONT = FontFactory.getFont(FontFactory.COURIER, 9, new Color(15, 23, 42));
    private static final Font DISCLAIMER_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, new Color(148, 163, 184));

    @Override
    public byte[] generateCkaResultPdf(CkaExercise exercise, CkaVerificationResult result, String studentName) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Header Banner
            Paragraph title = new Paragraph("Kube-CKA-AI-Assistant — Informe de Práctica CKA", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph sub = new Paragraph("Simulador Autónomo de Laboratorios Kubernetes | Fecha: " + Instant.now(), SUBTITLE_FONT);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(15);
            document.add(sub);

            // Student & Exercise Summary Table
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(15);

            addCell(metaTable, "Estudiante / Operador:", studentName != null ? studentName : "Operador SRE");
            addCell(metaTable, "Ejercicio ID:", exercise.getId() + " (" + exercise.getDomain() + ")");
            addCell(metaTable, "Título:", exercise.getTitleEs() != null ? exercise.getTitleEs() : exercise.getTitleEn());
            addCell(metaTable, "Resultado de Evaluación:", result.isPassed() ? "✅ APROBADO (+ " + result.getScoreEarned() + " pts)" : "❌ REQUIERE REVISIÓN (0 pts)");
            document.add(metaTable);

            // Aserciones Técnicas
            Paragraph assertionsTitle = new Paragraph("Aserciones Técnicas Evaluadas en el Clúster Minikube:", SECTION_FONT);
            assertionsTitle.setSpacingAfter(8);
            document.add(assertionsTitle);

            PdfPTable checksTable = new PdfPTable(4);
            checksTable.setWidthPercentage(100);
            checksTable.setWidths(new float[]{30, 15, 25, 30});
            checksTable.setSpacingAfter(15);

            addHeaderCell(checksTable, "Aserción");
            addHeaderCell(checksTable, "Estado");
            addHeaderCell(checksTable, "Esperado");
            addHeaderCell(checksTable, "Mensaje");

            for (CkaAssertionCheck check : result.getChecks()) {
                checksTable.addCell(new Phrase(check.getCheckName(), BODY_FONT));
                PdfPCell statusCell = new PdfPCell(new Phrase(check.isPassed() ? "PASS" : "FAIL", BODY_FONT));
                statusCell.setBackgroundColor(check.isPassed() ? new Color(220, 252, 231) : new Color(254, 226, 226));
                checksTable.addCell(statusCell);
                checksTable.addCell(new Phrase(check.getExpected(), CODE_FONT));
                checksTable.addCell(new Phrase(check.getMessage(), BODY_FONT));
            }
            document.add(checksTable);

            // Pro-Tip
            if (result.getStudentTips() != null && !result.getStudentTips().isBlank()) {
                Paragraph tip = new Paragraph("💡 Consejo Práctico CKA:\n" + result.getStudentTips(), BODY_FONT);
                tip.setSpacingAfter(20);
                document.add(tip);
            }

            // Legal Disclaimer
            Paragraph disclaimer = new Paragraph(
                    "Aviso Legal: Documento generado automáticamente por Kube-CKA-AI-Assistant. Herramienta independiente con fines de estudio personal. No afiliada ni patrocinada por la Linux Foundation ni CNCF.",
                    DISCLAIMER_FONT
            );
            disclaimer.setAlignment(Element.ALIGN_CENTER);
            document.add(disclaimer);

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            log.error("Error al generar PDF de evaluación CKA: {}", ex.getMessage(), ex);
            return new byte[0];
        }
    }

    @Override
    public byte[] generateDiagnosticReportPdf(DiagnosticReport report) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("Kube-CKA-AI-Assistant — Informe Técnico de Diagnóstico SRE", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph sub = new Paragraph("Incidente: " + report.getIncidentId() + " | Generado: " + report.getAnalyzedAt(), SUBTITLE_FONT);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(15);
            document.add(sub);

            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(15);

            addCell(metaTable, "Recurso:", report.getNamespace() + "/" + report.getPodName());
            addCell(metaTable, "Severidad:", report.getSeverity().name());
            addCell(metaTable, "Causa Raíz:", report.getRootCause());
            addCell(metaTable, "Detalles Técnicos:", report.getTechnicalDetails() != null ? report.getTechnicalDetails() : "Sin telemetría anómala");
            document.add(metaTable);

            Paragraph aiTitle = new Paragraph("Análisis y Recomendación del Asistente de IA:", SECTION_FONT);
            aiTitle.setSpacingAfter(8);
            document.add(aiTitle);

            Paragraph aiRec = new Paragraph(report.getAiRecommendation(), CODE_FONT);
            aiRec.setSpacingAfter(15);
            document.add(aiRec);

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            log.error("Error al generar PDF de diagnóstico: {}", ex.getMessage(), ex);
            return new byte[0];
        }
    }

    private void addCell(PdfPTable table, String label, String value) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, SECTION_FONT));
        c1.setBackgroundColor(new Color(241, 245, 249));
        table.addCell(c1);
        table.addCell(new Phrase(value, BODY_FONT));
    }

    private void addHeaderCell(PdfPTable table, String header) {
        PdfPCell cell = new PdfPCell(new Phrase(header, SECTION_FONT));
        cell.setBackgroundColor(new Color(226, 232, 240));
        table.addCell(cell);
    }
}
