package com.juanvargas.kubesynapse.domain.pattern.chain;

import com.juanvargas.kubesynapse.domain.model.DiagnosticSeverity;
import com.juanvargas.kubesynapse.domain.model.PodStatusInfo;

/**
 * Handler 2: Analyzes pod lifecycle phase (Pending, Failed, Running, etc.).
 */
public class PodPhaseHandler extends DiagnosticHandler {

    @Override
    protected void doHandle(DiagnosticContext context) {
        PodStatusInfo info = context.getPodInfo();
        String phase = info.getPhase();

        if ("Pending".equalsIgnoreCase(phase)) {
            context.setSeverity(DiagnosticSeverity.WARNING);
            context.setDetectedReason("POD_PENDING");
            context.addFinding("El Pod se encuentra en estado 'Pending'. Posible falta de recursos en nodos o fallas de scheduling.");
            context.addTrail("PodPhaseHandler", "Fase Pending detectada.");
        } else if ("Failed".equalsIgnoreCase(phase)) {
            context.setSeverity(DiagnosticSeverity.CRITICAL);
            context.setDetectedReason("POD_FAILED");
            context.addFinding("El Pod ha fallado de forma terminal.");
            context.addTrail("PodPhaseHandler", "Fase Failed detectada.");
        } else {
            context.addTrail("PodPhaseHandler", "Fase '" + phase + "' detectada. Continuando análisis...");
        }
    }
}
