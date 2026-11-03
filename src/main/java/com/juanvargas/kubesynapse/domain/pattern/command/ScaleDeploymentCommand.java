package com.juanvargas.kubesynapse.domain.pattern.command;

import com.juanvargas.kubesynapse.domain.model.RemediationResult;
import com.juanvargas.kubesynapse.application.port.out.KubernetesPort;

import java.time.Instant;
import java.util.UUID;

/**
 * Concrete Command: Scales a Deployment up or down, supporting undo (rolling back to prior replica count).
 */
public class ScaleDeploymentCommand implements RemediationCommand {

    private final String namespace;
    private final String deploymentName;
    private final int targetReplicas;
    private final int previousReplicas;
    private final KubernetesPort k8sPort;
    private final boolean dryRun;

    public ScaleDeploymentCommand(String namespace, String deploymentName, int targetReplicas, int previousReplicas, KubernetesPort k8sPort, boolean dryRun) {
        this.namespace = namespace;
        this.deploymentName = deploymentName;
        this.targetReplicas = targetReplicas;
        this.previousReplicas = previousReplicas;
        this.k8sPort = k8sPort;
        this.dryRun = dryRun;
    }

    @Override
    public RemediationResult execute() {
        String cmdId = "CMD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        if (dryRun) {
            return RemediationResult.builder()
                    .commandId(cmdId)
                    .action("SCALE_DEPLOYMENT")
                    .target(namespace + "/" + deploymentName)
                    .success(true)
                    .dryRun(true)
                    .message("[SIMULACIÓN / DRY-RUN] El Deployment " + deploymentName + " sería escalado a " + targetReplicas + " réplicas.")
                    .executedAt(Instant.now())
                    .build();
        }

        boolean success = k8sPort.scaleDeployment(namespace, deploymentName, targetReplicas);
        return RemediationResult.builder()
                .commandId(cmdId)
                .action("SCALE_DEPLOYMENT")
                .target(namespace + "/" + deploymentName)
                .success(success)
                .dryRun(false)
                .message(success ? "Deployment escalado a " + targetReplicas + " réplicas."
                                 : "Error al escalar deployment " + deploymentName)
                .executedAt(Instant.now())
                .build();
    }

    @Override
    public RemediationResult undo() {
        String cmdId = "UNDO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        boolean success = k8sPort.scaleDeployment(namespace, deploymentName, previousReplicas);
        return RemediationResult.builder()
                .commandId(cmdId)
                .action("UNDO_SCALE_DEPLOYMENT")
                .target(namespace + "/" + deploymentName)
                .success(success)
                .dryRun(false)
                .message("Rollback de escala: Deployment restaurado a " + previousReplicas + " réplicas.")
                .executedAt(Instant.now())
                .build();
    }

    @Override
    public String getDescription() {
        return "ScaleDeploymentCommand -> Deployment: " + deploymentName + " a " + targetReplicas + " réplicas (DryRun: " + dryRun + ")";
    }

    @Override
    public boolean isDryRun() {
        return dryRun;
    }
}
