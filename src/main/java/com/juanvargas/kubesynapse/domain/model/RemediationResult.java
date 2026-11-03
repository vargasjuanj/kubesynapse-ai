package com.juanvargas.kubesynapse.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Result of a remediation operation executed against the cluster.
 */
@Value
@Builder
public class RemediationResult {
    String commandId;
    String action;
    String target;
    boolean success;
    boolean dryRun;
    String message;
    Instant executedAt;
}
