package com.juanvargas.kubesynapse.application.service;

import com.juanvargas.kubesynapse.domain.model.DiagnosticReport;
import com.juanvargas.kubesynapse.domain.model.DiagnosticSeverity;
import com.juanvargas.kubesynapse.domain.model.PodIncidentEvent;
import com.juanvargas.kubesynapse.domain.model.PodStatusInfo;
import com.juanvargas.kubesynapse.domain.pattern.chain.DiagnosticContext;
import com.juanvargas.kubesynapse.domain.pattern.chain.DiagnosticPipeline;
import com.juanvargas.kubesynapse.domain.pattern.strategy.DiagnosticStrategy;
import com.juanvargas.kubesynapse.domain.pattern.strategy.factory.DiagnosticStrategyFactory;
import com.juanvargas.kubesynapse.application.port.in.DiagnosePodUseCase;
import com.juanvargas.kubesynapse.application.port.out.AiDiagnosticsPort;
import com.juanvargas.kubesynapse.application.port.out.EventPublisherPort;
import com.juanvargas.kubesynapse.application.port.out.KubernetesPort;

import java.util.Optional;

/**
 * ======================================================================================
 * SERVICIO DE DOMINIO: IncidentTriageService
 * ARQUITECTURA HEXAGONAL: CORE DOMAIN SERVICE (POJO PURO)
 * ======================================================================================
 * Orquesta el flujo completo de diagnóstico y operaciones autónomas:
 * 1. Recuperación de telemetría viva desde KubernetesPort.
 * 2. Triaje multidimensional a través de DiagnosticPipeline (Chain of Responsibility).
 * 3. Inferencia de causa raíz mediante DiagnosticStrategyFactory (Factory Method).
 * 4. Asistencia inteligente y razonamiento con IA mediante AiDiagnosticsPort.
 * 5. Generación de informe inmutable mediante DiagnosticStrategy (Strategy + Builder).
 * 6. Publicación asíncrona de eventos a RabbitMQ mediante EventPublisherPort (si hay anomalía).
 *
 * Cero acoplamiento a frameworks externos (sin anotaciones Spring, JPA ni AMQP).
 * ======================================================================================
 */
public class IncidentTriageService implements DiagnosePodUseCase {

    private final KubernetesPort k8sPort;
    private final AiDiagnosticsPort aiPort;
    private final EventPublisherPort eventPublisherPort;
    private final com.juanvargas.kubesynapse.application.port.out.IncidentReportRepositoryPort reportRepository;
    private final DiagnosticPipeline pipeline;
    private final DiagnosticStrategyFactory strategyFactory;

    public IncidentTriageService(KubernetesPort k8sPort, AiDiagnosticsPort aiPort) {
        this(k8sPort, aiPort, null, null);
    }

    public IncidentTriageService(KubernetesPort k8sPort, AiDiagnosticsPort aiPort, EventPublisherPort eventPublisherPort) {
        this(k8sPort, aiPort, eventPublisherPort, null);
    }

    public IncidentTriageService(KubernetesPort k8sPort, AiDiagnosticsPort aiPort, 
                                 EventPublisherPort eventPublisherPort, 
                                 com.juanvargas.kubesynapse.application.port.out.IncidentReportRepositoryPort reportRepository) {
        this.k8sPort = k8sPort;
        this.aiPort = aiPort;
        this.eventPublisherPort = eventPublisherPort;
        this.reportRepository = reportRepository;
        this.pipeline = new DiagnosticPipeline();
        this.strategyFactory = new DiagnosticStrategyFactory();
    }

    @Override
    public DiagnosticReport diagnosePod(String namespace, String podName) {
        // 1. Obtener estado en tiempo real desde el cluster
        Optional<PodStatusInfo> podOpt = k8sPort.getPodStatus(namespace, podName);
        PodStatusInfo podInfo = podOpt.orElse(null);

        // 2. Ejecutar la cadena de responsabilidad (Chain of Responsibility)
        DiagnosticContext context = pipeline.execute(namespace, podName, podInfo);

        // 3. Consultar asistencia y plan correctivo al motor de IA (Ollama / Fallback)
        String aiAdvice = null;
        if (podInfo != null && aiPort != null) {
            aiAdvice = aiPort.generateRemediationAdvice(podInfo, context.getDetectedReason());
        }

        // 4. Seleccionar estrategia de resolución y construir reporte inmutable
        DiagnosticStrategy strategy = strategyFactory.getStrategy(context.getDetectedReason());
        DiagnosticReport report = strategy.generateReport(context, aiAdvice);

        // 5. Arquitectura Orientada a Eventos: Publicar evento a RabbitMQ si el pod está en alerta o fallo
        if (eventPublisherPort != null && report.getSeverity() != null && report.getSeverity() != DiagnosticSeverity.HEALTHY) {
            eventPublisherPort.publishIncidentEvent(PodIncidentEvent.fromReport(report));
        }

        // 6. Persistencia en Base de Datos (JPA): Guardar reporte inmutable
        if (reportRepository != null) {
            reportRepository.save(report);
        }

        return report;
    }
}
