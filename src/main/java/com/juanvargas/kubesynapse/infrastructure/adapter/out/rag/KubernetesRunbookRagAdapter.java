package com.juanvargas.kubesynapse.infrastructure.adapter.out.rag;

import com.juanvargas.kubesynapse.application.port.out.RunbookKnowledgePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * ======================================================================================
 * ADAPTADOR DE SALIDA (Outbound Adapter): KUBERNETES RUNBOOK RAG ADAPTER
 * ======================================================================================
 * Implementa {@link RunbookKnowledgePort} utilizando una base de conocimiento documental
 * especializada en incidentes de Kubernetes y microservicios Spring Boot.
 *
 * Enfoque pedagógico y empresarial (Gabriel Chaldú / Spring AI):
 * - Representa la fase "Retrieval" de RAG: localiza el documento o fragmento de
 *   runbook correspondiente a la falla.
 * - Este contenido se inyecta posteriormente en el prompt template ({runbookContext})
 *   para que el modelo de IA genere recomendaciones 100% alineadas con las mejores
 *   prácticas del clúster y sin inventar parámetros o comandos inexistentes.
 * ======================================================================================
 */
@Slf4j
@Component
public class KubernetesRunbookRagAdapter implements RunbookKnowledgePort {

    private final Map<String, String> runbookStore = new HashMap<>();

    public KubernetesRunbookRagAdapter() {
        initRunbookStore();
    }

    private void initRunbookStore() {
        runbookStore.put("OOMKILLED", """
                [K8S RUNBOOK SRE-101: OOMKilled (Exit Code 137)]
                - Causa: El proceso consumió más memoria RAM que la especificada en 'resources.limits.memory'
                  del pod. El kernel de Linux invoca al OOM-Killer y envía la señal SIGKILL (9 + 128 = 137).
                - Patrón Java/Spring Boot: Si la JVM no detecta los límites del contenedor, el Heap por defecto
                  puede tomar hasta el 25% de la RAM del nodo físico, sobrepasando el cgroup del pod.
                - Mitigación Inmediata:
                  1. Agregar en deployment.yaml:
                     env:
                       - name: JAVA_TOOL_OPTIONS
                         value: "-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
                  2. Aumentar el límite de memoria del contenedor en un 30% a 50% según la métrica histórica.
                  3. Ejecutar 'kubectl rollout restart deployment/<nombre>'.
                """);

        runbookStore.put("CRASHLOOPBACKOFF", """
                [K8S RUNBOOK SRE-102: CrashLoopBackOff (Exit Code 1)]
                - Causa: El contenedor arranca y termina de forma anormal repetidas veces. Kubernetes
                  aplica un retardo exponencial (BackOff) antes de volver a intentar iniciarlo.
                - Patrón Java/Spring Boot: Errores comunes incluyen fallo en Beans de Spring, incompatibilidad
                  de esquema Liquibase/Flyway, o variables de entorno obligatorias ausentes en ConfigMaps/Secrets.
                - Mitigación Inmediata:
                  1. Obtener logs del contenedor anterior al reinicio: 'kubectl logs <pod> --previous'.
                  2. Verificar variables de entorno faltantes o secret keys con 'kubectl describe pod <pod>'.
                  3. Si depende de una base de datos externa, verificar la conectividad de red y resolución CoreDNS.
                """);

        runbookStore.put("NETWORK_DEPENDENCY", """
                [K8S RUNBOOK SRE-103: Network & Dependency Timeout]
                - Causa: El contenedor falla debido a un Connection Refused, SocketTimeoutException o
                  UnknownHostException hacia un servicio interno o RabbitMQ broker.
                - Mitigación Inmediata:
                  1. Verificar que el Service de Kubernetes esté activo: 'kubectl get svc <service-name>'.
                  2. Comprobar resolución DNS ejecutando nslookup desde un pod de debug ephemeral.
                  3. Validar NetworkPolicies que puedan estar bloqueando el puerto de destino.
                """);
    }

    @Override
    public String findRelevantRunbook(String reason, Integer exitCode) {
        log.info("📚 [RAG Knowledge Retriever]: Buscando runbook relevante para razón='{}', exitCode={}", reason, exitCode);

        if (exitCode != null && exitCode == 137) {
            return runbookStore.get("OOMKILLED");
        }
        if (exitCode != null && exitCode == 1) {
            return runbookStore.get("CRASHLOOPBACKOFF");
        }

        String key = reason != null ? reason.toUpperCase() : "CRASHLOOPBACKOFF";
        return runbookStore.getOrDefault(key, runbookStore.get("CRASHLOOPBACKOFF"));
    }
}
