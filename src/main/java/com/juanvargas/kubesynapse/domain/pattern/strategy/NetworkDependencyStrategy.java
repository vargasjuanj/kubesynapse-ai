package com.juanvargas.kubesynapse.domain.pattern.strategy;

import com.juanvargas.kubesynapse.domain.model.DiagnosticReport;
import com.juanvargas.kubesynapse.domain.model.DiagnosticSeverity;
import com.juanvargas.kubesynapse.domain.pattern.chain.DiagnosticContext;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Concrete Strategy: Handles Network and Database connectivity failures.
 */
public class NetworkDependencyStrategy implements DiagnosticStrategy {

    @Override
    public boolean canHandle(String detectedReason) {
        return "NETWORK_DEPENDENCY_FAILURE".equalsIgnoreCase(detectedReason)
                || "POD_PENDING".equalsIgnoreCase(detectedReason);
    }

    @Override
    public DiagnosticReport generateReport(DiagnosticContext context, String aiRecommendation) {
        return DiagnosticReport.builder()
                .incidentId("INC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .namespace(context.getNamespace())
                .podName(context.getPodName())
                .severity(DiagnosticSeverity.WARNING)
                .rootCause("Falla de Conectividad o Dependencia no Disponible")
                .technicalDetails(String.join(" | ", context.getTechnicalFindings()))
                .aiRecommendation(aiRecommendation != null ? aiRecommendation :
                    "Verificar que el Service o la Base de Datos dependiente esté corriendo y expuesta en el puerto correcto dentro del cluster.")
                .suggestedRemediations(List.of(
                    "Ejecutar 'kubectl get svc -n " + context.getNamespace() + "' para validar los endpoints activos.",
                    "Revisar NetworkPolicies que puedan estar bloqueando el tráfico de entrada/salida.",
                    "Configurar Connection Pooling (HikariCP) con connection-timeout y validation-query adecuados."
                ))
                .pipelineTrail(context.getPipelineTrail())
                .analyzedAt(Instant.now())
                .build();
    }
}
