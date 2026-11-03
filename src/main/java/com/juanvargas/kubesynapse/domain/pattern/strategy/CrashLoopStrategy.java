package com.juanvargas.kubesynapse.domain.pattern.strategy;

import com.juanvargas.kubesynapse.domain.model.DiagnosticReport;
import com.juanvargas.kubesynapse.domain.model.DiagnosticSeverity;
import com.juanvargas.kubesynapse.domain.pattern.chain.DiagnosticContext;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Concrete Strategy: Handles Application crashes and Spring Boot startup failures.
 */
public class CrashLoopStrategy implements DiagnosticStrategy {

    @Override
    public boolean canHandle(String detectedReason) {
        return "SPRING_BEAN_INITIALIZATION_ERROR".equalsIgnoreCase(detectedReason)
                || "APPLICATION_ERROR_1".equalsIgnoreCase(detectedReason)
                || "POD_FAILED".equalsIgnoreCase(detectedReason);
    }

    @Override
    public DiagnosticReport generateReport(DiagnosticContext context, String aiRecommendation) {
        return DiagnosticReport.builder()
                .incidentId("INC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .namespace(context.getNamespace())
                .podName(context.getPodName())
                .severity(DiagnosticSeverity.CRITICAL)
                .rootCause("Falla Crítica de Aplicación / CrashLoopBackOff")
                .technicalDetails(String.join(" | ", context.getTechnicalFindings()))
                .aiRecommendation(aiRecommendation != null ? aiRecommendation :
                    "Verificar variables de entorno requeridas en application.yml y chequear que los beans @Configuration estén correctamente inyectados.")
                .suggestedRemediations(List.of(
                    "Inspeccionar los logs de inicio del pod con: kubectl logs <pod> --previous",
                    "Verificar Secretos y ConfigMaps asociados en el Deployment.",
                    "Probar el arranque local del artefacto JAR con el mismo perfil de configuración."
                ))
                .pipelineTrail(context.getPipelineTrail())
                .analyzedAt(Instant.now())
                .build();
    }
}
