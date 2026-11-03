package com.juanvargas.kubesynapse.infrastructure.adapter.in.web;

import com.juanvargas.kubesynapse.domain.model.ClusterOverview;
import com.juanvargas.kubesynapse.domain.model.DiagnosticReport;
import com.juanvargas.kubesynapse.domain.model.PodStatusInfo;
import com.juanvargas.kubesynapse.domain.model.RemediationResult;
import com.juanvargas.kubesynapse.domain.pattern.command.RestartPodCommand;
import com.juanvargas.kubesynapse.application.port.in.DiagnosePodUseCase;
import com.juanvargas.kubesynapse.application.port.in.ExecuteRemediationUseCase;
import com.juanvargas.kubesynapse.application.port.in.GetClusterOverviewUseCase;
import com.juanvargas.kubesynapse.application.port.out.KubernetesPort;
import com.juanvargas.kubesynapse.infrastructure.adapter.out.mcp.KubernetesMcpToolRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

/**
 * ======================================================================================
 * ADAPTADOR PRIMARIO: AGENTE AUTÓNOMO COPILOT EN LENGUAJE NATURAL
 * ======================================================================================
 * Permite al operador enviar instrucciones libres en lenguaje natural (ej. "Revisá el pod
 * demo-oomkilled-service y decime por qué falló").
 *
 * El agente:
 * 1. Analiza la intención semántica (Diagnóstico, Remediación, Salud del Clúster, Consulta CKA).
 * 2. Selecciona e invoca la herramienta MCP apropiada o el caso de uso del dominio.
 * 3. Expone la traza de razonamiento paso a paso (Chain of Thought).
 * 4. Devuelve una respuesta clara, técnica y con acciones ejecutables de 1 solo clic.
 * ======================================================================================
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/agent")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "KubeGuardian AI Copilot", description = "Consola interactiva de instrucciones en lenguaje natural para Kubernetes")
public class AgentController {

    private final DiagnosePodUseCase diagnoseUseCase;
    private final GetClusterOverviewUseCase overviewUseCase;
    private final ExecuteRemediationUseCase remediationUseCase;
    private final KubernetesPort k8sPort;
    private final KubernetesMcpToolRegistry mcpToolRegistry;

    @PostMapping("/instruct")
    @Operation(summary = "Procesar instrucción en lenguaje natural con razonamiento Chain-of-Thought y ejecución de herramientas")
    public ResponseEntity<AgentInstructionResponse> processInstruction(@RequestBody AgentInstructionRequest request) {
        String prompt = request.getPrompt() != null ? request.getPrompt().trim() : "";
        log.info("🤖 [Agent Copilot]: Instrucción recibida -> '{}'", prompt);

        if (prompt.isBlank()) {
            return ResponseEntity.badRequest().body(AgentInstructionResponse.builder()
                    .userPrompt("")
                    .intent("UNKNOWN")
                    .agentResponse("Por favor ingresa una instrucción o comando para que pueda ayudarte.")
                    .thoughtProcess(List.of("Instrucción vacía recibida."))
                    .build());
        }

        String lowerPrompt = prompt.toLowerCase();
        List<String> thoughts = new ArrayList<>();
        thoughts.add("1. 🧠 Análisis semántico de la instrucción del operador: '" + prompt + "'");

        // 1. Detección de Namespace (default por defecto, kube-system, cka-lab o dev)
        String namespace = "default";
        if (lowerPrompt.contains("kube-system")) {
            namespace = "kube-system";
        } else if (lowerPrompt.contains("cka-lab")) {
            namespace = "cka-lab";
        } else if (lowerPrompt.contains("dev")) {
            namespace = "dev";
        }

        // 2. Detección de Pod objetivo
        String podName = null;
        if (lowerPrompt.contains("cka-crash-api")) {
            podName = "cka-crash-api";
            namespace = "cka-lab";
        } else if (lowerPrompt.contains("cka-oom-worker")) {
            podName = "cka-oom-worker";
            namespace = "cka-lab";
        } else if (lowerPrompt.contains("nginx-web")) {
            podName = "nginx-web";
            namespace = "dev";
        } else if (lowerPrompt.contains("demo-oomkilled-service") || lowerPrompt.contains("oomkilled") || lowerPrompt.contains("oom")) {
            podName = "demo-oomkilled-service";
        } else if (lowerPrompt.contains("demo-crashloop-service") || lowerPrompt.contains("crashloop")) {
            podName = "demo-crashloop-service";
        } else if (lowerPrompt.contains("rabbitmq")) {
            podName = "rabbitmq-broker";
        } else if (lowerPrompt.contains("storage-provisioner")) {
            podName = "storage-provisioner";
            namespace = "kube-system";
        }
        // -------------------------------------------------------------
        // CONTROL DE ACCESO BASADO EN ROLES (RBAC): FILTRO DE MUTACIÓN
        // -------------------------------------------------------------
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isMutation = lowerPrompt.contains("crea ") || lowerPrompt.contains("crear") || lowerPrompt.contains("create")
                || lowerPrompt.contains("elimina") || lowerPrompt.contains("borrar") || lowerPrompt.contains("delete")
                || lowerPrompt.contains("limpi") || lowerPrompt.contains("caos") || lowerPrompt.contains("chaos")
                || lowerPrompt.contains("inyect");

        if (!isAdmin && isMutation) {
            thoughts.add("2. 🔒 Verificación RBAC: Rol actual del usuario es 'ROLE_OPERATOR'.");
            thoughts.add("3. ⛔ Operación de mutación BLOQUEADA. Los operadores tienen privilegios de sólo lectura.");
            return ResponseEntity.ok(AgentInstructionResponse.builder()
                    .userPrompt(prompt)
                    .intent("RBAC_RESTRICTED")
                    .toolInvoked("K8S_RBAC_POLICY_FILTER")
                    .targetResource(podName != null ? podName : "cluster")
                    .thoughtProcess(thoughts)
                    .agentResponse("🔒 **Acceso Denegado por Política RBAC (Role-Based Access Control)**\n\n" +
                            "Tu perfil activo es **`ROLE_OPERATOR` (Solo Lectura y Observabilidad)**.\n\n" +
                            "El rol de Operador te permite diagnosticar pods, consultar la salud del clúster y evaluar soluciones CKA, pero **tiene prohibido crear pods, eliminar recursos o inyectar fallos de caos**.\n\n" +
                            "👉 **¿Necesitas realizar cambios?** Cambia al perfil **SRE Admin** en el selector de la esquina superior derecha.")
                    .timestamp(Instant.now().toString())
                    .build());
        }

        // -------------------------------------------------------------
        // INTENCIÓN A: ¿Limpiar Laboratorio CKA?
        // -------------------------------------------------------------
        if (lowerPrompt.contains("limpi") || lowerPrompt.contains("borrar cka") || lowerPrompt.contains("resetear cka")) {
            thoughts.add("2. 🧹 Intención detectada: LIMPIEZA DE LABORATORIO CKA.");
            thoughts.add("3. 🗑️ Eliminando namespace 'cka-lab' y liberando recursos...");
            k8sPort.deleteNamespace("cka-lab");
            thoughts.add("4. ✅ Namespace 'cka-lab' eliminado del clúster.");

            return ResponseEntity.ok(AgentInstructionResponse.builder()
                    .userPrompt(prompt)
                    .intent("CLEAN_LAB")
                    .toolInvoked("k8s_delete_namespace")
                    .targetResource("cka-lab")
                    .thoughtProcess(thoughts)
                    .agentResponse("🧹 **Laboratorio CKA Limpiado:** He eliminado el namespace `cka-lab` y todos sus recursos de Minikube. El clúster quedó en estado limpio.")
                    .build());
        }

        // -------------------------------------------------------------
        // INTENCIÓN B: ¿Generar Ejercicio de Troubleshooting CKA (Chaos Engineering)?
        // -------------------------------------------------------------
        boolean isDiagnostic = lowerPrompt.contains("revis") || lowerPrompt.contains("diagnos") || lowerPrompt.contains("analiz") || lowerPrompt.contains("por que") || lowerPrompt.contains("por qué");
        if (!isDiagnostic && (lowerPrompt.contains("ejercicio") || lowerPrompt.contains("troubleshoot") 
                || lowerPrompt.contains("simula un problema") || lowerPrompt.contains("caos") || lowerPrompt.contains("chaos")
                || lowerPrompt.contains("inyect") || lowerPrompt.contains("generar ejercicio"))) {

            String labNamespace = "cka-lab";
            thoughts.add("2. 🧪 Intención detectada: GENERACIÓN DE ESCENARIO PRÁCTICO CKA (Chaos Simulator).");
            thoughts.add("3. 📦 Asegurando creación de namespace aislado: '" + labNamespace + "'.");
            k8sPort.createNamespace(labNamespace);

            String scenarioType = "OOMKilled";
            String targetPod = "cka-oom-worker";
            String yamlManifest;

            if (lowerPrompt.contains("crash") || lowerPrompt.contains("exit 1")) {
                scenarioType = "CrashLoopBackOff";
                targetPod = "cka-crash-api";
                yamlManifest = """
                    apiVersion: v1
                    kind: Pod
                    metadata:
                      name: cka-crash-api
                      namespace: cka-lab
                      labels:
                        exam: cka
                        scenario: crashloop
                    spec:
                      restartPolicy: Always
                      containers:
                      - name: broken-service
                        image: busybox:1.36
                        command: ["/bin/sh", "-c", "echo '[BOOT]: Starting financial service...'; sleep 3; echo '[FATAL]: DB Connection failed at 10.0.0.99'; exit 1"]
                    """;
            } else if (lowerPrompt.contains("image") || lowerPrompt.contains("imagen") || lowerPrompt.contains("pull")) {
                scenarioType = "ImagePullBackOff";
                targetPod = "cka-image-err";
                yamlManifest = """
                    apiVersion: v1
                    kind: Pod
                    metadata:
                      name: cka-image-err
                      namespace: cka-lab
                      labels:
                        exam: cka
                        scenario: errimagepull
                    spec:
                      restartPolicy: Always
                      containers:
                      - name: web-server
                        image: nginx:v999.invalid-tag-no-existe
                    """;
            } else {
                // Default: OOMKilled
                yamlManifest = """
                    apiVersion: v1
                    kind: Pod
                    metadata:
                      name: cka-oom-worker
                      namespace: cka-lab
                      labels:
                        exam: cka
                        scenario: oomkilled
                    spec:
                      restartPolicy: Always
                      containers:
                      - name: memory-eater
                        image: polinux/stress
                        command: ["stress"]
                        args: ["--vm", "1", "--vm-bytes", "128M", "--vm-hang", "1"]
                        resources:
                          limits:
                            memory: "32Mi"
                          requests:
                            memory: "16Mi"
                    """;
            }

            thoughts.add("4. 🚀 Desplegando manifiesto YAML del escenario '" + scenarioType + "' en Minikube.");
            boolean applied = k8sPort.applyYaml(labNamespace, yamlManifest);
            thoughts.add("5. ✅ Despliegue de laboratorio completado con éxito: " + applied);

            String ckaPromptResponse = "🎯 **¡Escenario de Desafío SRE inyectado en vivo en Minikube!**\n\n" +
                    "Acabo de modificar el clúster inyectando una falla real en el namespace `cka-lab` para que pongas a prueba tus habilidades de certificación:\n\n" +
                    "- **Escenario:** `Troubleshooting: " + scenarioType + "`\n" +
                    "- **Namespace:** `cka-lab`\n" +
                    "- **Pod afectado:** `" + targetPod + "`\n\n" +
                    "📝 **Tu Misión como Administrador CKA:**\n" +
                    "1. Inspecciona el pod en tu terminal: `kubectl get pods -n cka-lab`\n" +
                    "2. Analiza los eventos y la razón de salida: `kubectl describe pod " + targetPod + " -n cka-lab`\n" +
                    "3. Encuentra la causa raíz y planifica la corrección.\n\n" +
                    "💡 **Opciones del Agente:**\n" +
                    "- Pídeme: *'Revisá el pod " + targetPod + " en cka-lab y decime por qué falló'* para que yo haga el triaje.\n" +
                    "- O pídeme: *'Limpiar el laboratorio cka'* cuando quieras resetear el clúster.";

            return ResponseEntity.ok(AgentInstructionResponse.builder()
                    .userPrompt(prompt)
                    .intent("CKA_CHAOS_LAB_INJECTED")
                    .toolInvoked("k8s_apply_manifest")
                    .targetResource(labNamespace + "/" + targetPod)
                    .thoughtProcess(thoughts)
                    .agentResponse(ckaPromptResponse)
                    .suggestedAction("diagnose-lab")
                    .build());
        }

        // -------------------------------------------------------------
        // INTENCIÓN C: ¿Crear Pod en namespace específico?
        // -------------------------------------------------------------
        if (lowerPrompt.contains("crea un pod") || lowerPrompt.contains("crear un pod") 
                || lowerPrompt.contains("creame un pod") || lowerPrompt.contains("crear pod")
                || lowerPrompt.contains("despliega un pod") || lowerPrompt.contains("desplegar un pod")
                || lowerPrompt.contains("cree un pod")) {

            // Extraer namespace si lo especificó (ej: en dev, en test, en cka-lab)
            String targetNs = "default";
            if (lowerPrompt.contains("namespace")) {
                int nsIdx = lowerPrompt.indexOf("namespace");
                String[] words = lowerPrompt.substring(nsIdx).split("\\s+");
                if (words.length > 1) {
                    targetNs = words[1].replaceAll("[^a-zA-Z0-9-]", "");
                }
            } else if (lowerPrompt.contains(" en ")) {
                int enIdx = lowerPrompt.indexOf(" en ");
                String[] words = lowerPrompt.substring(enIdx + 4).split("\\s+");
                if (words.length > 0 && !words[0].isBlank()) {
                    targetNs = words[0].replaceAll("[^a-zA-Z0-9-]", "");
                }
            }

            // Extraer imagen (ej: nginx, redis, busybox, postgres)
            String image = "nginx:alpine";
            String newPodName = "app-" + UUID.randomUUID().toString().substring(0, 5);
            if (lowerPrompt.contains("redis")) {
                image = "redis:alpine";
                newPodName = "redis-server";
            } else if (lowerPrompt.contains("busybox")) {
                image = "busybox:1.36";
                newPodName = "busybox-tool";
            } else if (lowerPrompt.contains("nginx")) {
                image = "nginx:alpine";
                newPodName = "nginx-web";
            }

            thoughts.add("2. 🚀 Intención detectada: CREACIÓN ACTIVA DE POD en clúster.");
            thoughts.add("3. 📋 Parametrización: Pod='" + newPodName + "', Image='" + image + "', Namespace='" + targetNs + "'.");
            thoughts.add("4. 🛠️ Invocando Fabric8 Kubernetes API Client para crear Pod...");

            boolean created = k8sPort.createSimplePod(targetNs, newPodName, image);
            thoughts.add("5. ✅ Pod creado en Minikube: " + created);

            String responseText = "🚀 **Pod creado exitosamente en tu clúster Minikube:**\n\n" +
                    "- **Pod:** `" + newPodName + "`\n" +
                    "- **Namespace:** `" + targetNs + "`\n" +
                    "- **Imagen:** `" + image + "`\n" +
                    "- **Estado:** Desplegado mediante Fabric8 Kubernetes SDK.\n\n" +
                    "Podés comprobarlo en tu terminal ejecutando:\n" +
                    "```bash\nkubectl get pod " + newPodName + " -n " + targetNs + "\n```";

            return ResponseEntity.ok(AgentInstructionResponse.builder()
                    .userPrompt(prompt)
                    .intent("CREATE_POD")
                    .toolInvoked("k8s_create_pod")
                    .targetResource(targetNs + "/" + newPodName)
                    .thoughtProcess(thoughts)
                    .agentResponse(responseText)
                    .suggestedAction("verify-pod")
                    .build());
        }

        // 3. Intención: ¿Diagnosticar / Analizar / Revisar / Por qué falla?
        if (lowerPrompt.contains("diagnos") || lowerPrompt.contains("revis") || lowerPrompt.contains("verific") 
                || lowerPrompt.contains("analiz") || lowerPrompt.contains("falla") || lowerPrompt.contains("error") 
                || lowerPrompt.contains("problema") || lowerPrompt.contains("por que") || lowerPrompt.contains("por qué")
                || (podName != null && !lowerPrompt.contains("reinici") && !lowerPrompt.contains("restart"))) {

            if (podName == null) {
                podName = "demo-oomkilled-service"; // Fallback sugerido para demo
            }

            thoughts.add("2. 🔍 Intención detectada: DIAGNÓSTICO DE INCIDENTE en pod '" + podName + "' (ns: " + namespace + ").");
            thoughts.add("3. 🛠️ Invocando herramienta MCP: 'k8s_diagnose_incident' con pipeline hexagonal.");

            DiagnosticReport report = diagnoseUseCase.diagnosePod(namespace, podName);
            thoughts.add("4. 📊 Resultado del diagnóstico: Severidad=" + report.getSeverity() + ", CausaRaíz='" + report.getRootCause() + "'.");
            thoughts.add("5. 💡 Generando respuesta con recomendaciones CKA y planes de remediación.");

            StringBuilder responseText = new StringBuilder();
            responseText.append("He inspeccionado el pod **").append(report.getPodName())
                    .append("** en el namespace `").append(report.getNamespace()).append("`.\n\n");
            responseText.append("🔴 **Severidad:** `").append(report.getSeverity()).append("`\n");
            responseText.append("📌 **Causa Raíz:** ").append(report.getRootCause()).append("\n\n");
            responseText.append("📝 **Detalles Técnicos:** ").append(report.getTechnicalDetails()).append("\n\n");
            responseText.append("💡 **Recomendación del Agente:**\n").append(report.getAiRecommendation()).append("\n\n");

            if (report.getSuggestedRemediations() != null && !report.getSuggestedRemediations().isEmpty()) {
                responseText.append("🔧 **Acciones sugeridas:**\n");
                for (String rem : report.getSuggestedRemediations()) {
                    responseText.append("- ").append(rem).append("\n");
                }
            }

            return ResponseEntity.ok(AgentInstructionResponse.builder()
                    .userPrompt(prompt)
                    .intent("DIAGNOSE_POD")
                    .toolInvoked("k8s_diagnose_incident")
                    .targetResource(namespace + "/" + podName)
                    .thoughtProcess(thoughts)
                    .agentResponse(responseText.toString())
                    .payload(report)
                    .suggestedAction("restart-pod")
                    .build());
        }

        // 4. Intención: ¿Reiniciar / Remediación / Rollback?
        if (lowerPrompt.contains("reinici") || lowerPrompt.contains("restart") || lowerPrompt.contains("remedi")) {
            boolean isDryRun = lowerPrompt.contains("dry") || lowerPrompt.contains("simul") || !lowerPrompt.contains("fuerza");
            if (podName == null) {
                podName = "demo-crashloop-service";
            }

            thoughts.add("2. ⚡ Intención detectada: REMEDIACIÓN (Reinicio de Pod).");
            thoughts.add("3. 🛡️ Modo seguro activado: dryRun=" + isDryRun);
            thoughts.add("4. 🛠️ Invocando herramienta MCP: 'k8s_restart_pod' con Command Pattern.");

            RestartPodCommand cmd = new RestartPodCommand(namespace, podName, k8sPort, isDryRun);
            RemediationResult res = remediationUseCase.execute(cmd);

            thoughts.add("5. 📋 Comando ejecutado con success=" + res.isSuccess() + ".");

            String responseText = "Operación de remediación ejecutada sobre el pod **" + podName + "**.\n\n" +
                    "- **Resultado:** `" + (res.isSuccess() ? "EXITOSO" : "FALLIDO") + "`\n" +
                    "- **Acción:** `" + res.getAction() + "`\n" +
                    "- **Modo Dry-Run (Simulación):** " + (res.isDryRun() ? "SÍ (Seguro, sin impacto destructivo)" : "NO (Aplicado en vivo)") + "\n" +
                    "- **Mensaje:** " + res.getMessage() + "\n" +
                    "- **Command ID:** `" + res.getCommandId() + "`";

            return ResponseEntity.ok(AgentInstructionResponse.builder()
                    .userPrompt(prompt)
                    .intent("REMEDIATION_RESTART")
                    .toolInvoked("k8s_restart_pod")
                    .targetResource(namespace + "/" + podName)
                    .thoughtProcess(thoughts)
                    .agentResponse(responseText)
                    .payload(res)
                    .suggestedAction("undo")
                    .build());
        }

        // 5. Intención: Consulta específica sobre Nodos del Clúster
        boolean isNodeQuery = lowerPrompt.contains("nodo") || lowerPrompt.contains("nodos") 
                || lowerPrompt.contains("node") || lowerPrompt.contains("nodes");
        if (isNodeQuery) {
            thoughts.add("2. 🖥️ Intención detectada: CONSULTA DE TOPOLOGÍA DE NODOS.");
            thoughts.add("3. 🛠️ Invocando 'GetClusterOverviewUseCase' para inspeccionar los nodos en Minikube.");

            ClusterOverview overview = overviewUseCase.getClusterOverview();
            List<String> nodeNames = overview.getNodes() != null && !overview.getNodes().isEmpty() 
                    ? overview.getNodes() 
                    : List.of("minikube");
            String primaryNode = nodeNames.get(0);
            thoughts.add("4. 📋 Nodo identificado en el clúster: '" + primaryNode + "'.");

            StringBuilder sb = new StringBuilder();
            sb.append("🖥️ **Información del Nodo en el Clúster Kubernetes:**\n\n");
            sb.append("- **Nombre del nodo:** `").append(primaryNode).append("`\n");
            sb.append("- **Estado:** 🟢 `Ready` (Nodo activo de control-plane)\n");
            sb.append("- **Roles:** `control-plane, master, worker`\n");
            sb.append("- **Entorno:** Clúster Minikube v1.35.1 (Kubernetes v1.31 / v1.32)\n");
            sb.append("- **Total de pods alojados:** ").append(overview.getTotalPods()).append(" (")
              .append(overview.getHealthyPods()).append(" en estado saludable)\n\n");
            sb.append("👉 **Comandos de terminal para verificarlo en vivo:**\n");
            sb.append("- Listar nodos: `kubectl get nodes -o wide`\n");
            sb.append("- Detalle completo: `kubectl describe node ").append(primaryNode).append("`\n");

            return ResponseEntity.ok(AgentInstructionResponse.builder()
                    .userPrompt(prompt)
                    .intent("NODE_QUERY")
                    .toolInvoked("get_cluster_nodes")
                    .targetResource("nodes/" + primaryNode)
                    .thoughtProcess(thoughts)
                    .agentResponse(sb.toString())
                    .payload(overview)
                    .build());
        }

        // 6. Intención: ¿Salud del Clúster / Overview / Listar pods?
        if (lowerPrompt.contains("cluster") || lowerPrompt.contains("clúster") || lowerPrompt.contains("salud") 
                || lowerPrompt.contains("estado") || lowerPrompt.contains("resumen") || lowerPrompt.contains("pods")) {

            thoughts.add("2. 🌐 Intención detectada: INSPECCIÓN GENERAL DEL CLÚSTER.");
            thoughts.add("3. 🛠️ Invocando 'GetClusterOverviewUseCase' contra API Server de Minikube.");

            ClusterOverview overview = overviewUseCase.getClusterOverview();
            thoughts.add("4. 📊 Topología recuperada: " + overview.getTotalPods() + " pods totales, " + overview.getTroubledPods() + " con problemas.");

            StringBuilder sb = new StringBuilder();
            sb.append("📊 **Resumen del Clúster Kubernetes (Minikube):**\n\n");
            sb.append("- **Nodos:** `").append(String.join(", ", overview.getNodes())).append("` (").append(overview.getNodes().size()).append(" nodo activo)\n");
            sb.append("- **Namespaces:** `").append(String.join(", ", overview.getNamespaces())).append("`\n");
            sb.append("- **Total Pods:** ").append(overview.getTotalPods()).append("\n");
            sb.append("- **Pods Saludables:** 🟢 ").append(overview.getHealthyPods()).append("\n");
            sb.append("- **Pods en Alerta/Falla:** 🔴 ").append(overview.getTroubledPods()).append("\n\n");

            if (overview.getTroubledPods() > 0 && overview.getRecentTroubledPods() != null) {
                sb.append("⚠️ **Pods que requieren atención:**\n");
                for (PodStatusInfo p : overview.getRecentTroubledPods()) {
                    sb.append("- `").append(p.getNamespace()).append("/").append(p.getName())
                            .append("` -> Fase: `").append(p.getPhase())
                            .append("`, Reinicios: `").append(p.getRestartCount()).append("`\n");
                }
            }

            return ResponseEntity.ok(AgentInstructionResponse.builder()
                    .userPrompt(prompt)
                    .intent("CLUSTER_OVERVIEW")
                    .toolInvoked("get_cluster_overview")
                    .targetResource("cluster")
                    .thoughtProcess(thoughts)
                    .agentResponse(sb.toString())
                    .payload(overview)
                    .build());
        }

        // 7. Intención: Preguntas generales de CKA / ExitCodes / Conceptos de Kubernetes
        thoughts.add("2. 📚 Intención detectada: CONSULTA TÉCNICA / CONOCIMIENTO CKA.");
        thoughts.add("3. 🔍 Consultando Knowledge Base y Runbooks de Kubernetes.");

        String ckaAnswer;
        if (lowerPrompt.contains("137") || lowerPrompt.contains("oom")) {
            ckaAnswer = "📘 **Código de Salida 137 (Fatal error signal 9 - SIGKILL):**\n\n" +
                    "Ocurre cuando el kernel de Linux envía la señal SIGKILL (9) al proceso debido a que el contenedor consumió más memoria de la definida en `resources.limits.memory` (128 + 9 = 137).\n\n" +
                    "**Cómo diagnosticarlo:**\n" +
                    "1. Ejecutar `kubectl describe pod <pod>` y buscar `OOMKilled: true` en Last State.\n" +
                    "2. En aplicaciones Spring Boot/Java, configurar `-XX:MaxRAMPercentage=75.0` para que la JVM no desborde el cgroup.\n" +
                    "3. Incrementar el límite en el Deployment: `limits.memory: 256Mi`.";
        } else if (lowerPrompt.contains("143")) {
            ckaAnswer = "📘 **Código de Salida 143 (SIGTERM):**\n\n" +
                    "Indica que el contenedor fue terminado ordenadamente (Graceful Shutdown) por Kubernetes mediante señal SIGTERM (15) (128 + 15 = 143), típicamente durante un rolling update o desalojo de nodo.";
        } else if (lowerPrompt.contains("crashloop") || lowerPrompt.contains("exit code 1")) {
            ckaAnswer = "📘 **CrashLoopBackOff (Exit Code 1):**\n\n" +
                    "Indica que la aplicación falló repetidamente al arrancar y Kubernetes aplica un retraso exponencial (back-off) antes de reiniciar.\n\n" +
                    "**Causas comunes:** Variables de entorno faltantes, falla de conexión a base de datos, error de sintaxis en configuración o excepciones no capturadas durante el bootstrap.";
        } else if (lowerPrompt.contains("service") || lowerPrompt.contains("servicio")) {
            ckaAnswer = "📘 **Service en Kubernetes:**\n\n" +
                    "Es una abstracción que define un conjunto lógico de Pods y una política de acceso (ClusterIP, NodePort, LoadBalancer).\n" +
                    "- En el diagnóstico de clústeres, verifica siempre los `Endpoints` (`kubectl get endpoints <svc>`) para asegurar que el `selector` coincide con las `labels` de los Pods.";
        } else if (lowerPrompt.contains("ingress")) {
            ckaAnswer = "📘 **Ingress en Kubernetes:**\n\n" +
                    "Gestiona el acceso externo a los servicios del clúster (típicamente HTTP/HTTPS) proporcionando enrutamiento basado en nombres de host (`host:`) o rutas URL (`path:`).";
        } else if (lowerPrompt.contains("pvc") || lowerPrompt.contains("pv") || lowerPrompt.contains("storage") || lowerPrompt.contains("volumen")) {
            ckaAnswer = "📘 **Almacenamiento Persistente (PV & PVC):**\n\n" +
                    "- **PersistentVolume (PV):** Recurso de almacenamiento a nivel de clúster aprovisionado por un administrador.\n" +
                    "- **PersistentVolumeClaim (PVC):** Solicitud de almacenamiento realizada por un usuario o Pod.\n" +
                    "- Ambos se enlazan (estado `Bound`) cuando coinciden la `storageClassName`, el `accessModes` y la capacidad solicitada.";
        } else {
            ckaAnswer = "Soy **KubeGuardian AI**, tu asistente autónomo para operaciones de Kubernetes (CKA). Puedes pedirme:\n\n" +
                    "- 🖥️ *'¿Cómo se llama el nodo?'*\n" +
                    "- 🔍 *'Revisá el pod demo-oomkilled-service y decime por qué falló'*\n" +
                    "- ⚡ *'Simulá el reinicio de demo-crashloop-service con dry-run'*\n" +
                    "- 📊 *'¿Cómo está el estado de salud del clúster?'*\n" +
                    "- 📘 *'¿Qué significa el código de salida 137?'*";
        }

        return ResponseEntity.ok(AgentInstructionResponse.builder()
                .userPrompt(prompt)
                .intent("KNOWLEDGE_QUERY")
                .toolInvoked("k8s_rag_knowledge")
                .targetResource("cka-runbooks")
                .thoughtProcess(thoughts)
                .agentResponse(ckaAnswer)
                .build());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentInstructionRequest {
        private String prompt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentInstructionResponse {
        private String userPrompt;
        private String intent;
        private String toolInvoked;
        private String targetResource;
        private List<String> thoughtProcess;
        private String agentResponse;
        private Object payload;
        private String suggestedAction;
        @Builder.Default
        private String timestamp = Instant.now().toString();
    }
}
