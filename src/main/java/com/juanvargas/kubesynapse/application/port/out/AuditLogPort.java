package com.juanvargas.kubesynapse.application.port.out;

import com.juanvargas.kubesynapse.domain.model.RemediationResult;

import java.util.List;

/**
 * Outbound Port / SPI: Contract for storing remediation audit records.
 */
public interface AuditLogPort {
    void record(RemediationResult result);
    List<RemediationResult> getRecentHistory();
}
