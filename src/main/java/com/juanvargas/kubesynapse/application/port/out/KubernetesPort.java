package com.juanvargas.kubesynapse.application.port.out;

import com.juanvargas.kubesynapse.domain.model.PodStatusInfo;

import java.util.List;
import java.util.Optional;

/**
 * Outbound Port / SPI: Contract for communicating with the Kubernetes API.
 * Completely isolates the domain from Fabric8 or any specific client.
 */
public interface KubernetesPort {
    Optional<PodStatusInfo> getPodStatus(String namespace, String podName);
    List<PodStatusInfo> listPods(String namespace);
    List<String> listNamespaces();
    List<String> listNodeNames();
    String getPodLogs(String namespace, String podName, int tailLines);
    List<String> getPodEvents(String namespace, String podName);
    boolean restartPod(String namespace, String podName);
    boolean scaleDeployment(String namespace, String deploymentName, int targetReplicas);
    boolean createNamespace(String namespace);
    boolean createSimplePod(String namespace, String podName, String image);
    boolean applyYaml(String namespace, String yamlContent);
    boolean deletePod(String namespace, String podName);
    boolean deleteNamespace(String namespace);
}

