package com.juanvargas.kubesynapse.domain.pattern.chain;

import com.juanvargas.kubesynapse.domain.model.DiagnosticSeverity;

/**
 * Handler 1: Verifies if the target pod exists in the cluster metadata.
 */
public class PodExistenceHandler extends DiagnosticHandler {

    @Override
    protected void doHandle(DiagnosticContext context) {
        if (context.getPodInfo() == null) {
            context.setSeverity(DiagnosticSeverity.CRITICAL);
            context.setDetectedReason("POD_NOT_FOUND");
            context.addFinding("El Pod '" + context.getPodName() + "' no existe en el namespace '" + context.getNamespace() + "'.");
            context.addTrail("PodExistenceHandler", "Pod no encontrado en el cluster. Diagnóstico conclusivo.");
            context.setConclusive(true);
        } else {
            context.addTrail("PodExistenceHandler", "Pod localizado exitosamente en el cluster.");
        }
    }
}
