package com.juanvargas.kubesynapse.domain.pattern.command;

import com.juanvargas.kubesynapse.domain.model.RemediationResult;
import com.juanvargas.kubesynapse.application.port.out.KubernetesPort;

import java.time.Instant;
import java.util.UUID;

/**
 * Concrete Command: Safely terminates a troubled pod, allowing the Deployment controller
 * or ReplicaSet to spawn a fresh, healthy replica.
 */
public class RestartPodCommand implements RemediationCommand {

    private final String namespace;
    private final String podName;
    private final KubernetesPort k8sPort;
    private final boolean dryRun;

    public RestartPodCommand(String namespace, String podName, KubernetesPort k8sPort, boolean dryRun) {
        this.namespace = namespace;
        this.podName = podName;
        this.k8sPort = k8sPort;
        this.dryRun = dryRun;
    }

    @Override
    public RemediationResult execute() {
        String cmdId = "CMD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        if (dryRun) {
            return RemediationResult.builder()
                    .commandId(cmdId)
                    .action("RESTART_POD")
                    .target(namespace + "/" + podName)
                    .success(true)
                    .dryRun(true)
                    .message("[SIMULACIÓN / DRY-RUN] El pod " + podName + " sería eliminado para forzar recreation.")
                    .executedAt(Instant.now())
                    .build();
        }

        boolean success = k8sPort.restartPod(namespace, podName);
        return RemediationResult.builder()
                .commandId(cmdId)
                .action("RESTART_POD")
                .target(namespace + "/" + podName)
                .success(success)
                .dryRun(false)
                .message(success ? "Pod " + podName + " reiniciado exitosamente en Minikube."
                                 : "Error al reiniciar el pod " + podName)
                .executedAt(Instant.now())
                .build();
    }

    @Override
    public RemediationResult undo() {
        String cmdId = "UNDO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        if (dryRun) {
            return RemediationResult.builder()
                    .commandId(cmdId)
                    .action("UNDO_RESTART_POD")
                    .target(namespace + "/" + podName)
                    .success(true)
                    .dryRun(true)
                    .message("[SIMULACIÓN / DRY-RUN] Reversión simulada exitosamente: Se restauraría el estado previo del pod " + podName + ".")
                    .executedAt(Instant.now())
                    .build();
        }

        boolean success = k8sPort.restartPod(namespace, podName);
        return RemediationResult.builder()
                .commandId(cmdId)
                .action("UNDO_RESTART_POD")
                .target(namespace + "/" + podName)
                .success(success)
                .dryRun(false)
                .message(success ? "Reversión ejecutada: Ciclo de vida del pod " + podName + " restablecido por el controlador de réplicas."
                                 : "Error al aplicar reversión sobre el pod " + podName)
                .executedAt(Instant.now())
                .build();
    }

    @Override
    public String getDescription() {
        return "RestartPodCommand -> Namespace: " + namespace + ", Pod: " + podName + ", DryRun: " + dryRun;
    }

    @Override
    public boolean isDryRun() {
        return dryRun;
    }
}
