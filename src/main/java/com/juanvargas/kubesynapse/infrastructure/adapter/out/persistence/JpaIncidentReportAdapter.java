package com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence;

import com.juanvargas.kubesynapse.domain.model.DiagnosticReport;
import com.juanvargas.kubesynapse.application.port.out.IncidentReportRepositoryPort;
import com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.entity.IncidentReportEntity;
import com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.repository.IncidentReportJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * ======================================================================================
 * OUTBOUND ADAPTER (Driven Adapter): PERSISTENCIA JPA DE REPORTES DE INCIDENTES
 * ======================================================================================
 * Implementa IncidentReportRepositoryPort utilizando Spring Data JPA y base de datos persistente.
 * ======================================================================================
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JpaIncidentReportAdapter implements IncidentReportRepositoryPort {

    private final IncidentReportJpaRepository reportRepository;

    @Override
    public void save(DiagnosticReport report) {
        log.info("💾 [JPA Incidents]: Guardando reporte de incidente '{}' para pod '{}/{}' en base de datos...",
                report.getIncidentId(), report.getNamespace(), report.getPodName());
        IncidentReportEntity entity = IncidentReportEntity.fromDomain(report);
        reportRepository.save(entity);
    }

    @Override
    public List<DiagnosticReport> getRecentReports() {
        return reportRepository.findTop50ByOrderByAnalyzedAtDesc()
                .stream()
                .map(IncidentReportEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<DiagnosticReport> findByIncidentId(String incidentId) {
        return reportRepository.findByIncidentId(incidentId)
                .map(IncidentReportEntity::toDomain);
    }
}
