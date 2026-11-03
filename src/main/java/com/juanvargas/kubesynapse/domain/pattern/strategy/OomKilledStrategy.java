package com.juanvargas.kubesynapse.domain.pattern.strategy;

import com.juanvargas.kubesynapse.domain.model.DiagnosticReport;
import com.juanvargas.kubesynapse.domain.model.DiagnosticSeverity;
import com.juanvargas.kubesynapse.domain.pattern.chain.DiagnosticContext;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Concrete Strategy: Handles OOMKilled and Java Heap exhaustion scenarios.
 */
public class OomKilledStrategy implements DiagnosticStrategy {

    @Override
    public boolean canHandle(String detectedReason) {
        return "OOMKILLED".equalsIgnoreCase(detectedReason)
                || "JAVA_HEAP_OOM".equalsIgnoreCase(detectedReason)
                || "CONTAINER_SIGKILL_137".equalsIgnoreCase(detectedReason);
    }

    @Override
    public DiagnosticReport generateReport(DiagnosticContext context, String aiRecommendation) {
        return DiagnosticReport.builder()
                .incidentId("INC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .namespace(context.getNamespace())
                .podName(context.getPodName())
                .severity(DiagnosticSeverity.CRITICAL)
                .rootCause("Agotamiento de Memoria (OOMKilled / Java Heap OutOfMemory)")
                .technicalDetails(String.join(" | ", context.getTechnicalFindings()))
                .aiRecommendation(aiRecommendation != null ? aiRecommendation : 
                    "Incrementar los límites de memoria en el Deployment (resources.limits.memory) y ajustar -XX:MaxRAMPercentage=75.0 en la JVM.")
                .suggestedRemediations(List.of(
                    "Elevar memory.limits a un valor superior al consumo pico.",
                    "Configurar Spring Boot con -XX:+ExitOnOutOfMemoryError para reiniciar limpiamente.",
                    "Revisar posibles memory leaks en colecciones estáticas o caches sin TTL."
                ))
                .pipelineTrail(context.getPipelineTrail())
                .analyzedAt(Instant.now())
                .build();
    }
}
