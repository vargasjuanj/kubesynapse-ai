package com.juanvargas.kubesynapse.domain.pattern.chain;

import com.juanvargas.kubesynapse.domain.model.PodStatusInfo;

/**
 * ======================================================================================
 * PATRÓN DE COMPORTAMIENTO: CHAIN OF RESPONSIBILITY (Cadena de Responsabilidad)
 * ======================================================================================
 * Propósito:
 * - Evita acoplar el emisor de una petición a su receptor, dando a más de un objeto
 *   la oportunidad de procesar la petición.
 * - En KubeSynapse AI, el incidente del Pod pasa a través de una cadena de manejadores
 *   especializados. Cada eslabón analiza un aspecto específico del estado del Pod:
 *
 *   1. PodExistenceHandler:      ¿Existe el pod en el cluster?
 *            ↓
 *   2. PodPhaseHandler:          ¿Está en fase Pending, Failed o Running?
 *            ↓
 *   3. ContainerRestartHandler:  ¿Cuántos reinicios tiene y cuál fue el exit code (137, 1)?
 *            ↓
 *   4. OomKilledHandler:         ¿Fue liquidado por Linux cgroups (Out Of Memory)?
 *            ↓
 *   5. LogScannerHandler:        ¿Existen stacktraces de Spring Boot / JVM en stdout/stderr?
 *
 * Ventaja en producción:
 * - Si un eslabón encuentra una evidencia concluyente (ej: OOMKilled flag = true),
 *   marca context.setConclusive(true) y finaliza de inmediato sin procesamiento innecesario.
 * - Agregar un nuevo chequeo (ej: NetworkPolicyHandler o CoreDnsHandler) es tan simple
 *   como crear una clase que extienda de DiagnosticHandler e insertarla en la cadena.
 *
 * Referencias educativas:
 * - Refactoring.Guru: https://refactoring.guru/es/design-patterns/chain-of-responsibility
 * - MIW-UPM Chain of Responsibility: https://github.com/miw-upm
 * ======================================================================================
 */
public class DiagnosticPipeline {

    private final DiagnosticHandler headHandler;

    public DiagnosticPipeline() {
        // Build the chain: Existence -> Phase -> Restarts -> OOM -> Logs
        DiagnosticHandler existence = new PodExistenceHandler();
        DiagnosticHandler phase = new PodPhaseHandler();
        DiagnosticHandler restarts = new ContainerRestartHandler();
        DiagnosticHandler oom = new OomKilledHandler();
        DiagnosticHandler logs = new LogScannerHandler();

        existence.setNext(phase)
                 .setNext(restarts)
                 .setNext(oom)
                 .setNext(logs);

        this.headHandler = existence;
    }

    public DiagnosticContext execute(String namespace, String podName, PodStatusInfo podInfo) {
        DiagnosticContext context = DiagnosticContext.builder()
                .namespace(namespace)
                .podName(podName)
                .podInfo(podInfo)
                .build();

        headHandler.handle(context);
        return context;
    }
}
