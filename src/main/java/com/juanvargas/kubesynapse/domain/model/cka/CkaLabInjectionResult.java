package com.juanvargas.kubesynapse.domain.model.cka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Resultado de la inyección del estado inicial / defectuoso del laboratorio en Kubernetes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CkaLabInjectionResult {
    private String exerciseId;
    private String status; // "INJECTED", "FAILED"
    private String namespace;
    private String message;
    private Instant injectedAt;
    private List<String> resourcesCreated;
    private String startingCommand;
}
