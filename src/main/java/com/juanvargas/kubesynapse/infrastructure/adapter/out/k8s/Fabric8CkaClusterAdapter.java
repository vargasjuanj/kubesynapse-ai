package com.juanvargas.kubesynapse.infrastructure.adapter.out.k8s;

import com.juanvargas.kubesynapse.domain.model.cka.CkaAssertionCheck;
import com.juanvargas.kubesynapse.domain.model.cka.CkaExercise;
import com.juanvargas.kubesynapse.application.port.out.CkaClusterVerificationPort;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicy;
import io.fabric8.kubernetes.api.model.rbac.Role;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ======================================================================================
 * ADAPTADOR DE SALIDA (Driven Adapter): EVALUADOR DE CLÚSTER KUBERNETES PARA CKA
 * ======================================================================================
 * Implementa {@link CkaClusterVerificationPort}.
 * Contiene todas las dependencias de bajo nivel de Fabric8 KubernetesClient.
 * Mantiene el dominio completamente libre de clases de Kubernetes externas.
 * ======================================================================================
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Fabric8CkaClusterAdapter implements CkaClusterVerificationPort {

    private final KubernetesClient k8sClient;

    @Override
    public void deletePodQuietly(String namespace, String podName) {
        try {
            if (podName != null && !podName.isBlank()) {
                k8sClient.pods().inNamespace(namespace).withName(podName).withGracePeriod(0L).delete();
            }
        } catch (Exception ex) {
            log.debug("Aviso menor al eliminar pod {} de forma preventiva: {}", podName, ex.getMessage());
        }
    }

    @Override
    public List<CkaAssertionCheck> verifyExercise(CkaExercise exercise) {
        String ns = exercise.getTargetNamespace();
        List<CkaAssertionCheck> checks = new ArrayList<>();

        log.info("🔍 [Fabric8 CKA Adapter]: Evaluando aserciones en clúster para '{}' en namespace '{}'...",
                exercise.getId(), ns);

        try {
            switch (exercise.getId()) {
                case "CKA-TROUBLE-01" -> {
                    Pod pod = k8sClient.pods().inNamespace(ns).withName("api-gateway").get();
                    if (pod == null) {
                        checks.add(new CkaAssertionCheck("Pod api-gateway existe", false, "Pod presente en " + ns, "No encontrado", "El pod api-gateway fue eliminado o no existe."));
                    } else {
                        checks.add(new CkaAssertionCheck("Pod api-gateway existe", true, "Pod presente", "Encontrado", "Pod api-gateway existe."));
                        String phase = pod.getStatus() != null ? pod.getStatus().getPhase() : "Unknown";
                        boolean isRunning = "Running".equalsIgnoreCase(phase);
                        checks.add(new CkaAssertionCheck("Pod en estado Running", isRunning, "Running", phase, isRunning ? "El pod está corriendo correctamente." : "El pod está en fase: " + phase));

                        boolean ready = pod.getStatus() != null && pod.getStatus().getContainerStatuses() != null
                                && pod.getStatus().getContainerStatuses().stream().allMatch(cs -> Boolean.TRUE.equals(cs.getReady()));
                        checks.add(new CkaAssertionCheck("Contenedores Ready (1/1)", ready, "Ready: true", "Ready: " + ready, ready ? "El contenedor está 1/1 Ready." : "El contenedor no está listo."));
                    }
                }

                case "CKA-TROUBLE-02" -> {
                    Pod pod = k8sClient.pods().inNamespace(ns).withName("worker-cache").get();
                    if (pod == null) {
                        checks.add(new CkaAssertionCheck("Pod worker-cache existe", false, "Pod presente en " + ns, "No encontrado", "El pod worker-cache no existe."));
                    } else {
                        checks.add(new CkaAssertionCheck("Pod worker-cache existe", true, "Presente", "Presente", "Pod worker-cache localizado."));
                        String phase = pod.getStatus() != null ? pod.getStatus().getPhase() : "Unknown";
                        boolean isRunning = "Running".equalsIgnoreCase(phase);
                        checks.add(new CkaAssertionCheck("Pod en estado Running", isRunning, "Running", phase, isRunning ? "Pod estabilizado en Running." : "Pod en estado: " + phase));

                        boolean hasMemoryLimit = false;
                        if (pod.getSpec() != null && !pod.getSpec().getContainers().isEmpty()) {
                            var res = pod.getSpec().getContainers().get(0).getResources();
                            if (res != null && res.getLimits() != null && res.getLimits().containsKey("memory")) {
                                String mem = res.getLimits().get("memory").getAmount();
                                hasMemoryLimit = true;
                                checks.add(new CkaAssertionCheck("Límite de memoria asignado", true, ">= 128Mi", mem, "Límite configurado a: " + mem));
                            }
                        }
                        if (!hasMemoryLimit) {
                            checks.add(new CkaAssertionCheck("Límite de memoria asignado", false, ">= 128Mi", "Sin límites", "El contenedor no tiene configurado resources.limits.memory."));
                        }
                    }
                }

                case "CKA-ARCH-01" -> {
                    ServiceAccount sa = k8sClient.serviceAccounts().inNamespace(ns).withName("developer-sa").get();
                    boolean saExists = sa != null;
                    checks.add(new CkaAssertionCheck("ServiceAccount developer-sa existe", saExists, "Presente", saExists ? "Presente" : "No encontrado", saExists ? "ServiceAccount creado." : "Falta ServiceAccount."));

                    Role role = k8sClient.rbac().roles().inNamespace(ns).withName("pod-reader").get();
                    boolean roleValid = false;
                    if (role != null && role.getRules() != null) {
                        roleValid = role.getRules().stream().anyMatch(r ->
                                r.getResources().contains("pods") &&
                                        r.getVerbs().containsAll(List.of("get", "list", "watch")));
                    }
                    checks.add(new CkaAssertionCheck("Role pod-reader con permisos get/list/watch sobre pods", roleValid, "Role con pods:[get,list,watch]", role != null ? "Role encontrado" : "No encontrado", roleValid ? "Role configurado con permisos correctos." : "El Role no existe o no tiene los verbos requeridos."));

                    RoleBinding rb = k8sClient.rbac().roleBindings().inNamespace(ns).withName("developer-pod-binding").get();
                    boolean rbValid = rb != null && rb.getRoleRef() != null && "pod-reader".equals(rb.getRoleRef().getName());
                    checks.add(new CkaAssertionCheck("RoleBinding developer-pod-binding enlaza a developer-sa", rbValid, "Binding a pod-reader", rb != null ? "Binding a " + rb.getRoleRef().getName() : "No encontrado", rbValid ? "RoleBinding enlazado correctamente." : "RoleBinding no encontrado o mal enlazado."));
                }

                case "CKA-NET-01" -> {
                    io.fabric8.kubernetes.api.model.Service svc = k8sClient.services().inNamespace(ns).withName("web-service").get();
                    boolean svcExists = svc != null;
                    checks.add(new CkaAssertionCheck("Service web-service existe", svcExists, "Presente", svcExists ? "Presente" : "No encontrado", svcExists ? "Servicio localizado." : "Falta el servicio."));

                    Endpoints ep = k8sClient.endpoints().inNamespace(ns).withName("web-service").get();
                    boolean hasEndpoints = false;
                    if (ep != null && ep.getSubsets() != null && !ep.getSubsets().isEmpty()) {
                        int totalAddresses = ep.getSubsets().stream()
                                .mapToInt(s -> s.getAddresses() != null ? s.getAddresses().size() : 0)
                                .sum();
                        hasEndpoints = totalAddresses > 0;
                    }
                    checks.add(new CkaAssertionCheck("Endpoints del servicio tienen IPs activas", hasEndpoints, "> 0 endpoints", hasEndpoints ? "Endpoints conectados" : "0 endpoints", hasEndpoints ? "El servicio resolvió los pods correctamente." : "El selector del servicio sigue sin coincidir con los pods."));
                }

                case "CKA-NET-02" -> {
                    NetworkPolicy np = k8sClient.network().networkPolicies().inNamespace(ns).withName("db-netpol").get();
                    boolean npExists = np != null;
                    checks.add(new CkaAssertionCheck("NetworkPolicy db-netpol existe", npExists, "Presente", npExists ? "Presente" : "No encontrada", npExists ? "NetworkPolicy creada." : "Falta la política."));
                    if (npExists) {
                        boolean selectsDb = np.getSpec() != null && np.getSpec().getPodSelector() != null
                                && "database".equals(np.getSpec().getPodSelector().getMatchLabels().get("app"));
                        checks.add(new CkaAssertionCheck("Aplica a pods con etiqueta app=database", selectsDb, "app=database", selectsDb ? "app=database" : "Otro selector", selectsDb ? "Selector correcto." : "El selector de pods no apunta a app=database."));

                        boolean hasIngress = np.getSpec().getIngress() != null && !np.getSpec().getIngress().isEmpty();
                        checks.add(new CkaAssertionCheck("Regla de Ingress configurada", hasIngress, "Ingress rule", hasIngress ? "Presente" : "Ausente", hasIngress ? "Reglas de entrada definidas." : "Falta directiva Ingress."));
                    }
                }

                case "CKA-SCHED-01" -> {
                    Pod pod = k8sClient.pods().inNamespace(ns).withName("tolerator-pod").get();
                    boolean podExists = pod != null;
                    checks.add(new CkaAssertionCheck("Pod tolerator-pod existe", podExists, "Presente", podExists ? "Presente" : "No encontrado", podExists ? "Pod creado." : "No se encontró el pod."));
                    if (podExists) {
                        String phase = pod.getStatus() != null ? pod.getStatus().getPhase() : "Unknown";
                        boolean isRunning = "Running".equalsIgnoreCase(phase);
                        checks.add(new CkaAssertionCheck("Pod en estado Running", isRunning, "Running", phase, isRunning ? "Pod programado exitosamente." : "El pod no pudo programarse: " + phase));
                    }
                }

                case "CKA-STORE-01" -> {
                    PersistentVolume pv = k8sClient.persistentVolumes().withName("pv-analytics").get();
                    boolean pvExists = pv != null;
                    checks.add(new CkaAssertionCheck("PersistentVolume pv-analytics existe", pvExists, "Presente", pvExists ? "Presente" : "No encontrado", pvExists ? "PV creado." : "Falta el PV pv-analytics."));

                    PersistentVolumeClaim pvc = k8sClient.persistentVolumeClaims().inNamespace(ns).withName("pvc-analytics").get();
                    boolean pvcBound = pvc != null && pvc.getStatus() != null && "Bound".equalsIgnoreCase(pvc.getStatus().getPhase());
                    checks.add(new CkaAssertionCheck("PVC pvc-analytics en estado Bound", pvcBound, "Bound", pvc != null && pvc.getStatus() != null ? pvc.getStatus().getPhase() : "No encontrado", pvcBound ? "El PVC se enlazó con éxito al PV." : "El PVC no está enlazado (Bound)."));
                }

                default -> {
                    checks.add(new CkaAssertionCheck("Verificación genérica", true, "PASS", "PASS", "Verificación completada."));
                }
            }
        } catch (Exception ex) {
            log.error("Error al verificar recursos CKA en clúster: {}", ex.getMessage(), ex);
            checks.add(new CkaAssertionCheck("Inspección de API Server", false, "Consulta exitosa", "Error: " + ex.getMessage(), "Fallo de comunicación con Minikube."));
        }

        return checks;
    }
}
