package com.juanvargas.kubesynapse.application.port.out;

import com.juanvargas.kubesynapse.domain.model.DiagnosticReport;

import java.util.List;
import java.util.Optional;

/**
 * Outbound Port / SPI: Contract for persisting and retrieving AI incident diagnostic reports.
 */
public interface IncidentReportRepositoryPort {
    void save(DiagnosticReport report);
    List<DiagnosticReport> getRecentReports();
    Optional<DiagnosticReport> findByIncidentId(String incidentId);
}
