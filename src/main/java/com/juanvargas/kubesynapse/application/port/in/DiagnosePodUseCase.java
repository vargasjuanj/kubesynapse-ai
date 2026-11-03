package com.juanvargas.kubesynapse.application.port.in;

import com.juanvargas.kubesynapse.domain.model.DiagnosticReport;

/**
 * Inbound Port / Use Case: Triggers automated diagnosis for a specific pod.
 */
public interface DiagnosePodUseCase {
    DiagnosticReport diagnosePod(String namespace, String podName);
}
