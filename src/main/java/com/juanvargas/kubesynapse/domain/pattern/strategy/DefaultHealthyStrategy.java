package com.juanvargas.kubesynapse.domain.pattern.strategy;

import com.juanvargas.kubesynapse.domain.model.DiagnosticReport;
import com.juanvargas.kubesynapse.domain.model.DiagnosticSeverity;
import com.juanvargas.kubesynapse.domain.pattern.chain.DiagnosticContext;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Concrete Strategy: Handles healthy, normally running pods with zero errors.
 */
public class DefaultHealthyStrategy implements DiagnosticStrategy {

    @Override
    public boolean canHandle(String detectedReason) {
        return true; // Fallback strategy
    }

    @Override
    public DiagnosticReport generateReport(DiagnosticContext context, String aiRecommendation) {
        return DiagnosticReport.builder()
                .incidentId("REP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .namespace(context.getNamespace())
                .podName(context.getPodName())
                .severity(context.getSeverity() != null ? context.getSeverity() : DiagnosticSeverity.HEALTHY)
                .rootCause(context.getDetectedReason().equalsIgnoreCase("UNKNOWN") 
                        ? "Estado Operativo Normal (Sin fallas críticas detectadas)" 
                        : context.getDetectedReason())
                .technicalDetails(context.getTechnicalFindings().isEmpty() 
                        ? "El pod opera dentro de los umbrales normales de salud." 
                        : String.join(" | ", context.getTechnicalFindings()))
                .aiRecommendation(aiRecommendation != null ? aiRecommendation : 
                        "No se requieren acciones correctivas inmediatas. Mantener monitoreo continuo.")
                .suggestedRemediations(List.of("Monitoreo estándar con métricas de Actuator / Prometheus."))
                .pipelineTrail(context.getPipelineTrail())
                .analyzedAt(Instant.now())
                .build();
    }
}
