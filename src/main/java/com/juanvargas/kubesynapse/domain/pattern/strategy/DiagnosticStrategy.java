package com.juanvargas.kubesynapse.domain.pattern.strategy;

import com.juanvargas.kubesynapse.domain.model.DiagnosticReport;
import com.juanvargas.kubesynapse.domain.pattern.chain.DiagnosticContext;

/**
 * Strategy interface for root cause diagnostic deduction.
 */
public interface DiagnosticStrategy {
    boolean canHandle(String detectedReason);
    DiagnosticReport generateReport(DiagnosticContext context, String aiRecommendation);
}
