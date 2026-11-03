package com.juanvargas.kubesynapse.infrastructure.config;

import com.juanvargas.kubesynapse.application.port.in.CkaExamUseCase;
import com.juanvargas.kubesynapse.application.port.in.DiagnosePodUseCase;
import com.juanvargas.kubesynapse.application.port.in.GetClusterOverviewUseCase;
import com.juanvargas.kubesynapse.application.port.out.*;
import com.juanvargas.kubesynapse.application.service.CkaExamService;
import com.juanvargas.kubesynapse.application.service.ClusterOverviewService;
import com.juanvargas.kubesynapse.application.service.IncidentTriageService;
import com.juanvargas.kubesynapse.application.service.RemediationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ======================================================================================
 * CONFIGURACIÓN DE INFRAESTRUCTURA: INYECCIÓN DE DEPENDENCIAS DEL DOMINIO
 * ======================================================================================
 * Rol en Arquitectura Hexagonal:
 * - Es el único lugar donde Spring Framework conoce e instancia los servicios de dominio.
 * - Conecta los adaptadores secundarios (Fabric8 K8s, Ollama AI, RabbitMQ Publisher)
 *   hacia los puertos que exige el Dominio puro.
 * - Mantiene la pureza de las clases de negocio (cero dependencias de framework en el dominio).
 * ======================================================================================
 */
@Configuration
public class DomainServiceConfig {

    @Bean
    public DiagnosePodUseCase diagnosePodUseCase(
            KubernetesPort k8sPort,
            AiDiagnosticsPort aiPort,
            EventPublisherPort eventPublisherPort,
            com.juanvargas.kubesynapse.application.port.out.IncidentReportRepositoryPort reportRepo) {
        return new IncidentTriageService(k8sPort, aiPort, eventPublisherPort, reportRepo);
    }

    @Bean
    public GetClusterOverviewUseCase getClusterOverviewUseCase(KubernetesPort k8sPort) {
        return new ClusterOverviewService(k8sPort);
    }

    @Bean
    public RemediationService remediationService(AuditLogPort auditLogPort) {
        return new RemediationService(auditLogPort);
    }

    @Bean
    public CkaExamUseCase ckaExamUseCase(
            KubernetesPort k8sPort,
            CkaClusterVerificationPort ckaClusterVerificationPort,
            CkaExerciseRepositoryPort exerciseRepo) {
        return new CkaExamService(k8sPort, ckaClusterVerificationPort, exerciseRepo);
    }
}

