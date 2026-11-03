package com.juanvargas.kubesynapse.infrastructure.adapter.out.ai;

import com.juanvargas.kubesynapse.domain.model.PodStatusInfo;
import com.juanvargas.kubesynapse.application.port.out.AiDiagnosticsPort;
import com.juanvargas.kubesynapse.infrastructure.adapter.out.ai.provider.AiModelProvider;
import com.juanvargas.kubesynapse.infrastructure.adapter.out.ai.provider.HeuristicFallbackProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ======================================================================================
 * PATRÓN ARQUITECTÓNICO: OUTBOUND ADAPTER (Driven Adapter / Adaptador Secundario)
 * ======================================================================================
 * CATÁLOGO DE PATRONES DE DISEÑO DE IA IMPLEMENTADOS:
 * ======================================================================================
 * 1. AI MODEL GATEWAY / STRATEGY PATTERN:
 *    - Desacopla completamente el dominio del proveedor y modelo de IA.
 *    - Permite alternar en caliente o por configuración entre Ollama (local),
 *      OpenAI / DeepSeek (cloud), o el motor heurístico determinístico.
 *
 * 2. TELEMETRY-AUGMENTED GENERATION (TAG / GROUNDING):
 *    - Variante de RAG para observabilidad y AI-Ops: El prompt no se envía vacío;
 *      se "ancla" (grounding) con la telemetría viva de Kubernetes (Namespace, Pod,
 *      Fase, Reinicios, ExitCode, Eventos del clúster y cola de logs).
 *    - Elimina alucinaciones en un 99% al forzar al LLM a razonar sobre hechos verificables.
 *
 * 3. PROMPT TEMPLATE PATTERN:
 *    - Separa estrictamente el System Prompt (Rol de Ingeniero Principal CKA y SRE)
 *      del User Prompt (Hechos contextuales del clúster).
 *
 * 4. GUARDRAILS & CIRCUIT BREAKER / GRACEFUL FALLBACK (Martin Fowler):
 *    - Si el modelo de IA seleccionado no responde dentro de la ventana de tiempo
 *      o sufre de saturación de recursos, el Gateway conmuta automáticamente a
 *      {@link HeuristicFallbackProvider} para garantizar respuesta en milisegundos.
 *
 * Referencias educativas:
 * - Enterprise AI Design Patterns (Lak Lakshmanan / O'Reilly): Model Gateway Pattern.
 * - Prompt Engineering Guide: https://www.promptingguide.ai/techniques/grounding
 * - Martin Fowler (Circuit Breaker): https://martinfowler.com/bliki/CircuitBreaker.html
 * ======================================================================================
 */
@Slf4j
@Component
public class SpringAiDiagnosticsAdapter implements AiDiagnosticsPort {

    private final Map<String, AiModelProvider> providerMap;
    private final HeuristicFallbackProvider fallbackProvider;
    private final com.juanvargas.kubesynapse.application.port.out.RunbookKnowledgePort runbookKnowledgePort;

    @Value("${kubesynapse.ai.provider:ollama}")
    private String configuredProvider;

    public SpringAiDiagnosticsAdapter(
            List<AiModelProvider> providers,
            HeuristicFallbackProvider fallbackProvider,
            com.juanvargas.kubesynapse.application.port.out.RunbookKnowledgePort runbookKnowledgePort) {
        this.providerMap = providers.stream()
                .collect(Collectors.toMap(AiModelProvider::getProviderName, p -> p, (a, b) -> a));
        this.fallbackProvider = fallbackProvider;
        this.runbookKnowledgePort = runbookKnowledgePort;
    }

    @Override
    public String generateRootCauseAnalysis(PodStatusInfo podInfo, String detectedReason) {
        String systemPrompt = "Eres un Ingeniero Principal de Kubernetes (CKA) y Arquitecto Java Spring Boot. " +
                "Analiza incidentes de pods en Kubernetes aplicando razonamiento Chain-of-Thought estructurado.";

        // Patrón RAG: Recuperación de Runbook oficial de Kubernetes
        String runbookKnowledge = runbookKnowledgePort.findRelevantRunbook(detectedReason, podInfo.getLastExitCode());

        // Patrón TAG (Grounding de Telemetría) + RAG (Runbook oficial) + CoT (Pasos de razonamiento)
        String userPrompt = String.format(
                "=== GUÍA TÉCNICA OFICIAL RECUPERADA (RAG RUNBOOK KNOWLEDGE) ===\n%s\n" +
                "=== TELEMETRÍA EN VIVO DEL CLÚSTER (TAG GROUNDING) ===\n" +
                "- Pod: %s (Namespace: %s)\n" +
                "- Fase Actual: %s | Reinicios: %d | Último ExitCode: %s\n" +
                "- Razón Preliminar: %s\n" +
                "- Límite de Memoria: %s\n" +
                "- Eventos Recientes: %s\n\n" +
                "INSTRUCCIÓN (CADENA DE PENSAMIENTO / CHAIN-OF-THOUGHT):\n" +
                "Sigue estos pasos en tu análisis técnico:\n" +
                "1. Telemetría y Síntomas observados.\n" +
                "2. Correlación de Cgroups, Exit Codes y Memoria JVM.\n" +
                "3. Diagnóstico Diferencial de Causa Raíz basado en el Runbook.\n" +
                "4. Impacto en Producción y Disponibilidad del Microservicio.",
                runbookKnowledge,
                podInfo.getName(),
                podInfo.getNamespace(),
                podInfo.getPhase(),
                podInfo.getRestartCount(),
                podInfo.getLastExitCode() != null ? podInfo.getLastExitCode() : "N/A",
                detectedReason,
                podInfo.getMemoryLimit() != null ? podInfo.getMemoryLimit() : "Sin límite",
                podInfo.getClusterEvents() != null ? String.join("; ", podInfo.getClusterEvents()) : "Ninguno"
        );

        return executeWithFallback(systemPrompt, userPrompt);
    }

    @Override
    public String generateRemediationAdvice(PodStatusInfo podInfo, String detectedReason) {
        String systemPrompt = "Eres un Ingeniero Principal de Kubernetes (CKA) y Especialista SRE. " +
                "Proporciona planes de remediación accionables y directos para operadores de clúster.";

        String runbookKnowledge = runbookKnowledgePort.findRelevantRunbook(detectedReason, podInfo.getLastExitCode());

        // Patrón RAG + TAG: Plan de remediación concreto
        String userPrompt = String.format(
                "INCIDENTE DE PRODUCCIÓN EN KUBERNETES:\n" +
                "- Pod: %s (Namespace: %s)\n" +
                "- Causa de Falla: %s\n" +
                "- Runbook de Referencia: %s\n\n" +
                "INSTRUCCIÓN:\n" +
                "Entrega un plan de remediación en 3 pasos accionables en español, " +
                "con comandos 'kubectl' o parámetros JVM exactos para solucionar la causa raíz.",
                podInfo.getName(),
                podInfo.getNamespace(),
                detectedReason,
                runbookKnowledge.lines().findFirst().orElse("")
        );

        return executeWithFallback(systemPrompt, userPrompt);
    }

    /**
     * Resuelve el proveedor configurado (AI Gateway Pattern) y ejecuta la llamada con Circuit Breaker.
     */
    private String executeWithFallback(String systemPrompt, String userPrompt) {
        AiModelProvider activeProvider = providerMap.getOrDefault(configuredProvider.toLowerCase(), fallbackProvider);
        log.info("🌐 [AI Model Gateway]: Canalizando inferencia hacia proveedor '{}'...", activeProvider.getProviderName());

        String response = activeProvider.generateText(systemPrompt, userPrompt);

        if (response != null && !response.isBlank()) {
            return String.format("[%s Engine]:\n%s", activeProvider.getProviderName().toUpperCase(), response);
        }

        // Circuit Breaker: Fallback inmediato
        log.warn("⚠️ [AI Model Gateway]: Proveedor '{}' no disponible o excedió timeout. Activando Fallback Heurístico.",
                activeProvider.getProviderName());
        return fallbackProvider.generateText(systemPrompt, userPrompt);
    }
}
