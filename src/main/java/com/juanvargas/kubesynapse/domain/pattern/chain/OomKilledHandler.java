package com.juanvargas.kubesynapse.domain.pattern.chain;

import com.juanvargas.kubesynapse.domain.model.DiagnosticSeverity;
import com.juanvargas.kubesynapse.domain.model.PodStatusInfo;

/**
 * Handler 4: Specifically checks for OOMKilled condition in container status and events.
 */
public class OomKilledHandler extends DiagnosticHandler {

    @Override
    protected void doHandle(DiagnosticContext context) {
        PodStatusInfo info = context.getPodInfo();
        String reason = info.getLastTerminationReason();

        boolean isOom = "OOMKilled".equalsIgnoreCase(reason) ||
                info.getClusterEvents().stream().anyMatch(e -> e.toLowerCase().contains("oomkilled"));

        if (isOom) {
            context.setSeverity(DiagnosticSeverity.CRITICAL);
            context.setDetectedReason("OOMKILLED");
            context.addFinding("Contenedor terminado por el kernel OOM Killer (cgroups memory limit superado). Límite configurado: "
                    + (info.getMemoryLimit() != null ? info.getMemoryLimit() : "Sin límite explícito"));
            context.addTrail("OomKilledHandler", "Evidencia definitiva de OOMKilled encontrada.");
            context.setConclusive(true);
        } else {
            context.addTrail("OomKilledHandler", "No se detectaron marcas directas de OOMKilled.");
        }
    }
}
