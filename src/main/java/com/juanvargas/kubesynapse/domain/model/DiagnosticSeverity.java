package com.juanvargas.kubesynapse.domain.model;

/**
 * Severity level assigned to an incident by the diagnostic pipeline.
 */
public enum DiagnosticSeverity {
    HEALTHY,
    LOW,
    WARNING,
    CRITICAL
}
