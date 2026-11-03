package com.juanvargas.kubesynapse.domain.model;

import java.io.Serializable;
import java.time.Instant;

/**
 * ======================================================================================
 * PATRÓN ARQUITECTÓNICO: DOMAIN EVENT (Evento de Dominio - Microservicios & EDA)
 * ======================================================================================
 * Rol en la arquitectura:
 * - Representa un hecho inmutable ocurrido en el clúster de Kubernetes: un incidente
 *   crítico o de advertencia ha sido diagnosticado.
 * - Desacopla el microservicio de diagnóstico (KubeCKA-Synapse) de los consumidores
 *   (Slack, PagerDuty, auto-remediación, persistencia de auditoría).
 * - Puro Java 21: Serializable para transporte por RabbitMQ AMQP / JSON.
 *
 * Referencias educativas:
 * - Martin Fowler (Domain Event): https://martinfowler.com/eaaDev/DomainEvent.html
 * - Enterprise Integration Patterns: https://www.enterpriseintegrationpatterns.com/
 * ======================================================================================
 */
public record PodIncidentEvent(
        String incidentId,
        String namespace,
        String podName,
        String severity,
        String rootCause,
        String aiAdvice,
        Instant occurredAt
) implements Serializable {

    public static PodIncidentEvent fromReport(DiagnosticReport report) {
        return new PodIncidentEvent(
                report.getIncidentId(),
                report.getNamespace(),
                report.getPodName(),
                report.getSeverity() != null ? report.getSeverity().name() : "UNKNOWN",
                report.getRootCause(),
                report.getAiRecommendation(),
                Instant.now()
        );
    }
}
