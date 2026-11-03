package com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.repository;

import com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.entity.IncidentReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentReportJpaRepository extends JpaRepository<IncidentReportEntity, Long> {
    Optional<IncidentReportEntity> findByIncidentId(String incidentId);
    List<IncidentReportEntity> findTop50ByOrderByAnalyzedAtDesc();
    List<IncidentReportEntity> findByNamespaceAndPodNameOrderByAnalyzedAtDesc(String namespace, String podName);
}
