package com.juanvargas.kubesynapse.domain.pattern.chain;

import com.juanvargas.kubesynapse.domain.model.DiagnosticSeverity;
import com.juanvargas.kubesynapse.domain.model.PodStatusInfo;

/**
 * Handler 3: Analyzes container restart frequency and exit codes.
 */
public class ContainerRestartHandler extends DiagnosticHandler {

    @Override
    protected void doHandle(DiagnosticContext context) {
        PodStatusInfo info = context.getPodInfo();
        int restarts = info.getRestartCount();
        Integer exitCode = info.getLastExitCode();

        if (restarts > 0) {
            context.setSeverity(DiagnosticSeverity.WARNING);
            context.addFinding("El contenedor se ha reiniciado " + restarts + " veces.");
            context.addTrail("ContainerRestartHandler", "Reinicios detectados: " + restarts + " (ExitCode: " + exitCode + ")");

            if (exitCode != null && exitCode == 137) {
                context.setDetectedReason("CONTAINER_SIGKILL_137");
                context.setSeverity(DiagnosticSeverity.CRITICAL);
                context.addFinding("Código de salida 137 (SIGKILL) detectado. Alta probabilidad de OOMKilled por el kernel de Linux.");
            } else if (exitCode != null && exitCode == 1) {
                context.setDetectedReason("APPLICATION_ERROR_1");
                context.addFinding("Código de salida 1 detectado: error general de ejecución de la aplicación (excepción no controlada).");
            }
        } else {
            context.addTrail("ContainerRestartHandler", "Sin reinicios previos (restartCount = 0).");
        }
    }
}
