package com.juanvargas.kubesynapse.infrastructure.adapter.out.mcp;

import com.juanvargas.kubesynapse.domain.model.PodStatusInfo;
import com.juanvargas.kubesynapse.domain.model.RemediationResult;
import com.juanvargas.kubesynapse.application.port.in.DiagnosePodUseCase;
import com.juanvargas.kubesynapse.application.port.in.ExecuteRemediationUseCase;
import com.juanvargas.kubesynapse.application.port.in.GetClusterOverviewUseCase;
import com.juanvargas.kubesynapse.application.port.out.KubernetesPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ======================================================================================
 * MODEL CONTEXT PROTOCOL (MCP): REGISTRO Y DESPACHADOR DE HERRAMIENTAS K8S
 * ======================================================================================
 * Actúa como el MCP Server Provider para Kubernetes dentro del ecosistema KubeSynapse.
 * Expone un catálogo estructurado de Tools y Resources conforme al estándar MCP,
 * permitiendo a LLMs autónomos interactuar con el clúster sin acoplamiento.
 *
 * Implementa conceptos vistos en:
 * - Curso DevTalles / Gabriel Chaldú: Spring AI MCP Server, @Tool, @McpResource.
 * ======================================================================================
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KubernetesMcpToolRegistry {

    private final KubernetesPort kubernetesPort;
    private final DiagnosePodUseCase diagnosePodUseCase;
    private final ExecuteRemediationUseCase executeRemediationUseCase;
    private final GetClusterOverviewUseCase clusterOverviewUseCase;

    /**
     * Retorna el catálogo completo de herramientas disponibles según el protocolo MCP.
     */
    public List<McpToolDefinition> getAvailableTools() {
        List<McpToolDefinition> tools = new ArrayList<>();

        tools.add(new McpToolDefinition(
                "k8s_describe_pod",
                "Obtiene la telemetría viva, fase, cgroups, reinicios y eventos recientes de un pod.",
                Map.of(
                        "namespace", Map.of("type", "string", "description", "Namespace de Kubernetes (ej. default)"),
                        "podName", Map.of("type", "string", "description", "Nombre exacto del Pod")
                ),
                List.of("namespace", "podName")
        ));

        tools.add(new McpToolDefinition(
                "k8s_get_logs",
                "Extrae las últimas N líneas de logs de stdout y stderr del contenedor.",
                Map.of(
                        "namespace", Map.of("type", "string", "description", "Namespace de Kubernetes"),
                        "podName", Map.of("type", "string", "description", "Nombre del Pod"),
                        "tailLines", Map.of("type", "integer", "description", "Cantidad de líneas a recuperar (default 100)")
                ),
                List.of("namespace", "podName")
        ));

        tools.add(new McpToolDefinition(
                "k8s_diagnose_incident",
                "Ejecuta el pipeline hexagonal de triaje y análisis de causa raíz asistido por IA.",
                Map.of(
                        "namespace", Map.of("type", "string", "description", "Namespace de Kubernetes"),
                        "podName", Map.of("type", "string", "description", "Nombre del Pod")
                ),
                List.of("namespace", "podName")
        ));

        tools.add(new McpToolDefinition(
                "k8s_restart_pod",
                "Ejecuta una remediación de reinicio controlado del pod (soporta simulación dry-run).",
                Map.of(
                        "namespace", Map.of("type", "string", "description", "Namespace de Kubernetes"),
                        "podName", Map.of("type", "string", "description", "Nombre del Pod"),
                        "dryRun", Map.of("type", "boolean", "description", "Si es true, no borra el pod físicamente")
                ),
                List.of("namespace", "podName")
        ));

        return tools;
    }

    /**
     * Invoca una herramienta MCP por nombre pasando los argumentos estructurados.
     */
    public Object executeTool(String toolName, Map<String, Object> arguments) {
        log.info("🛠️ [MCP Dispatcher]: Ejecutando herramienta MCP '{}' con args: {}", toolName, arguments);

        String namespace = (String) arguments.getOrDefault("namespace", "default");
        String podName = (String) arguments.getOrDefault("podName", "");

        return switch (toolName) {
            case "k8s_describe_pod" -> kubernetesPort.getPodStatus(namespace, podName)
                    .orElse(PodStatusInfo.builder().name(podName).namespace(namespace).phase("Unknown").build());

            case "k8s_get_logs" -> {
                int lines = arguments.containsKey("tailLines") ? ((Number) arguments.get("tailLines")).intValue() : 100;
                yield kubernetesPort.getPodLogs(namespace, podName, lines);
            }

            case "k8s_diagnose_incident" -> diagnosePodUseCase.diagnosePod(namespace, podName);

            case "k8s_restart_pod" -> {
                boolean dryRun = arguments.containsKey("dryRun") && Boolean.parseBoolean(arguments.get("dryRun").toString());
                com.juanvargas.kubesynapse.domain.pattern.command.RestartPodCommand cmd =
                        new com.juanvargas.kubesynapse.domain.pattern.command.RestartPodCommand(namespace, podName, kubernetesPort, dryRun);
                yield executeRemediationUseCase.execute(cmd);
            }

            default -> Map.of("error", "Herramienta MCP no reconocida: " + toolName);
        };
    }
}
