package com.juanvargas.kubesynapse.infrastructure.adapter.out.k8s;

import com.juanvargas.kubesynapse.domain.model.PodStatusInfo;
import com.juanvargas.kubesynapse.application.port.out.KubernetesPort;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ======================================================================================
 * PATRÓN ARQUITECTÓNICO: OUTBOUND ADAPTER (Driven Adapter / Adaptador Secundario)
 * ======================================================================================
 * Rol en Arquitectura Hexagonal:
 * - Implementa el puerto de salida del dominio (KubernetesPort).
 * - Aísla completamente la lógica del negocio de los detalles técnicos y de la librería
 *   específica de infraestructura (en este caso, Fabric8 Kubernetes Java Client).
 * - Si en el futuro se migra a la librería oficial de Kubernetes (io.kubernetes:client-java),
 *   al cliente de Amazon EKS SDK, o a un Mock para pruebas de rendimiento, el dominio
 *   (IncidentTriageService, DiagnosticPipeline) permanece 100% INTACTO sin cambios.
 *
 * Flujo de ejecución:
 * 1. El dominio invoca un método neutral del puerto: k8sPort.getPodStatus(ns, podName).
 * 2. Este adaptador traduce la llamada a la sintaxis fluida de Fabric8:
 *    client.pods().inNamespace(ns).withName(podName).get().
 * 3. Mapea el objeto complejo de Kubernetes (io.fabric8.kubernetes.api.model.Pod) a una
 *    entidad inmutable y limpia del dominio (PodStatusInfo).
 *
 * Referencias educativas:
 * - Refactoring.Guru (Adapter Pattern): https://refactoring.guru/es/design-patterns/adapter
 * - Fabric8 Kubernetes Client Docs: https://github.com/fabric8io/kubernetes-client
 * - MIW-UPM Design Patterns: https://github.com/miw-upm
 * ======================================================================================
 */
@Slf4j
@Component
public class Fabric8KubernetesAdapter implements KubernetesPort {

    private final KubernetesClient client;

    public Fabric8KubernetesAdapter(KubernetesClient client) {
        this.client = client;
    }

    @Override
    public Optional<PodStatusInfo> getPodStatus(String namespace, String podName) {
        try {
            Pod pod = client.pods().inNamespace(namespace).withName(podName).get();
            if (pod == null) {
                return Optional.empty();
            }
            return Optional.of(mapToDomainPod(pod));
        } catch (Exception e) {
            log.error("Error al consultar el pod {}/{} en Kubernetes: {}", namespace, podName, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<PodStatusInfo> listPods(String namespace) {
        try {
            List<Pod> pods = client.pods().inNamespace(namespace).list().getItems();
            return pods.stream()
                    .map(this::mapToDomainPod)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al listar pods en namespace {}: {}", namespace, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> listNamespaces() {
        try {
            return client.namespaces().list().getItems().stream()
                    .map(ns -> ns.getMetadata().getName())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al listar namespaces: {}", e.getMessage());
            return List.of("default");
        }
    }

    @Override
    public List<String> listNodeNames() {
        try {
            return client.nodes().list().getItems().stream()
                    .map(node -> node.getMetadata().getName())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error al listar nodos: {}", e.getMessage());
            return List.of("minikube");
        }
    }

    @Override
    public String getPodLogs(String namespace, String podName, int tailLines) {
        try {
            return client.pods().inNamespace(namespace).withName(podName)
                    .tailingLines(tailLines)
                    .getLog();
        } catch (Exception e) {
            log.warn("No se pudieron obtener logs para {}/{}: {}", namespace, podName, e.getMessage());
            return "";
        }
    }

    @Override
    public List<String> getPodEvents(String namespace, String podName) {
        try {
            return client.v1().events().inNamespace(namespace).list().getItems().stream()
                    .filter(ev -> ev.getInvolvedObject() != null && podName.equals(ev.getInvolvedObject().getName()))
                    .map(ev -> String.format("[%s] %s: %s", ev.getType(), ev.getReason(), ev.getMessage()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("No se pudieron obtener eventos para {}/{}: {}", namespace, podName, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public boolean restartPod(String namespace, String podName) {
        try {
            log.info("Eliminando pod {}/{} para provocar restart por el ReplicaSet...", namespace, podName);
            var deleted = client.pods().inNamespace(namespace).withName(podName).delete();
            return deleted != null && !deleted.isEmpty();
        } catch (Exception e) {
            log.error("Falla al reiniciar pod {}/{}: {}", namespace, podName, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean scaleDeployment(String namespace, String deploymentName, int targetReplicas) {
        try {
            log.info("Escalando deployment {}/{} a {} réplicas...", namespace, deploymentName, targetReplicas);
            client.apps().deployments().inNamespace(namespace).withName(deploymentName).scale(targetReplicas);
            return true;
        } catch (Exception e) {
            log.error("Falla al escalar deployment {}/{}: {}", namespace, deploymentName, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean createNamespace(String namespace) {
        try {
            if (client.namespaces().withName(namespace).get() == null) {
                client.namespaces().resource(
                        new NamespaceBuilder().withNewMetadata().withName(namespace).endMetadata().build()
                ).create();
                log.info("📦 Namespace '{}' creado exitosamente.", namespace);
            }
            return true;
        } catch (Exception e) {
            log.error("Error al crear namespace '{}': {}", namespace, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean createSimplePod(String namespace, String podName, String image) {
        try {
            createNamespace(namespace);
            // Si el pod ya existe, se elimina previamente para recreación limpia
            client.pods().inNamespace(namespace).withName(podName).delete();

            String effectiveImage = (image != null && !image.isBlank()) ? image : "nginx:alpine";
            var pod = new PodBuilder()
                    .withNewMetadata().withName(podName).withNamespace(namespace).endMetadata()
                    .withNewSpec()
                        .addNewContainer()
                            .withName(podName)
                            .withImage(effectiveImage)
                        .endContainer()
                        .withRestartPolicy("Always")
                    .endSpec()
                    .build();

            client.pods().inNamespace(namespace).resource(pod).create();
            log.info("🚀 Pod '{}/{}' creado exitosamente en Minikube con imagen '{}'", namespace, podName, effectiveImage);
            return true;
        } catch (Exception e) {
            log.error("Error al crear Pod '{}/{}': {}", namespace, podName, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean applyYaml(String namespace, String yamlContent) {
        try {
            createNamespace(namespace);
            try (var is = new ByteArrayInputStream(yamlContent.getBytes(StandardCharsets.UTF_8))) {
                client.load(is).inNamespace(namespace).createOrReplace();
                log.info("📄 Manifiesto YAML aplicado en namespace '{}'", namespace);
                return true;
            }
        } catch (Exception e) {
            log.error("Error aplicando YAML en namespace '{}': {}", namespace, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deletePod(String namespace, String podName) {
        try {
            var deleted = client.pods().inNamespace(namespace).withName(podName).delete();
            return deleted != null && !deleted.isEmpty();
        } catch (Exception e) {
            log.error("Error al borrar pod '{}/{}': {}", namespace, podName, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteNamespace(String namespace) {
        try {
            var deleted = client.namespaces().withName(namespace).delete();
            return deleted != null && !deleted.isEmpty();
        } catch (Exception e) {
            log.error("Error al borrar namespace '{}': {}", namespace, e.getMessage());
            return false;
        }
    }

    private PodStatusInfo mapToDomainPod(Pod pod) {
        String ns = pod.getMetadata().getNamespace();
        String name = pod.getMetadata().getName();
        String node = pod.getSpec() != null ? pod.getSpec().getNodeName() : "unknown";
        String phase = pod.getStatus() != null ? pod.getStatus().getPhase() : "Unknown";

        int restarts = 0;
        Integer exitCode = null;
        String termReason = null;
        boolean ready = false;
        String image = "unknown";
        String memLimit = null;
        String memReq = null;

        if (pod.getStatus() != null && pod.getStatus().getContainerStatuses() != null) {
            for (ContainerStatus cs : pod.getStatus().getContainerStatuses()) {
                restarts += cs.getRestartCount();
                image = cs.getImage();
                if (Boolean.TRUE.equals(cs.getReady())) {
                    ready = true;
                }
                if (cs.getState() != null && cs.getState().getTerminated() != null) {
                    exitCode = cs.getState().getTerminated().getExitCode();
                    termReason = cs.getState().getTerminated().getReason();
                } else if (cs.getLastState() != null && cs.getLastState().getTerminated() != null) {
                    exitCode = cs.getLastState().getTerminated().getExitCode();
                    termReason = cs.getLastState().getTerminated().getReason();
                }
            }
        }

        if (pod.getSpec() != null && pod.getSpec().getContainers() != null && !pod.getSpec().getContainers().isEmpty()) {
            var container = pod.getSpec().getContainers().get(0);
            if (container.getResources() != null) {
                if (container.getResources().getLimits() != null && container.getResources().getLimits().containsKey("memory")) {
                    memLimit = container.getResources().getLimits().get("memory").getAmount();
                }
                if (container.getResources().getRequests() != null && container.getResources().getRequests().containsKey("memory")) {
                    memReq = container.getResources().getRequests().get("memory").getAmount();
                }
            }
        }

        // Fetch small snippet of logs and events
        String logs = getPodLogs(ns, name, 50);
        List<String> rawLogsList = logs != null && !logs.isBlank() ? Arrays.asList(logs.split("\n")) : Collections.emptyList();
        List<String> eventsList = getPodEvents(ns, name);

        return PodStatusInfo.builder()
                .name(name)
                .namespace(ns)
                .nodeName(node)
                .phase(phase)
                .restartCount(restarts)
                .lastExitCode(exitCode)
                .lastTerminationReason(termReason)
                .ready(ready)
                .containerImage(image)
                .rawLogs(rawLogsList)
                .clusterEvents(eventsList)
                .memoryLimit(memLimit)
                .memoryRequest(memReq)
                .build();
    }
}
