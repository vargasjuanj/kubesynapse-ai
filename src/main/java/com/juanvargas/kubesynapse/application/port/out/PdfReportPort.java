package com.juanvargas.kubesynapse.application.port.out;

import com.juanvargas.kubesynapse.domain.model.DiagnosticReport;
import com.juanvargas.kubesynapse.domain.model.cka.CkaExercise;
import com.juanvargas.kubesynapse.domain.model.cka.CkaVerificationResult;

/**
 * ======================================================================================
 * PUERTO DE SALIDA (Outbound Port): GENERADOR DE REPORTES PDF (JASPERREPORTS / OPENPDF)
 * ======================================================================================
 * Permite exportar informes técnicos y certificados de evaluación CKA en formato PDF.
 * ======================================================================================
 */
public interface PdfReportPort {

    byte[] generateCkaResultPdf(CkaExercise exercise, CkaVerificationResult result, String studentName);

    byte[] generateDiagnosticReportPdf(DiagnosticReport report);
}
