package com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.entity;

import com.juanvargas.kubesynapse.domain.model.DiagnosticReport;
import com.juanvargas.kubesynapse.domain.model.DiagnosticSeverity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * ======================================================================================
 * PERSISTENCIA JPA / ENTIDAD: HISTORIAL DE DIAGNÓSTICOS DE IA
 * ======================================================================================
 * Almacena cada informe técnico generado por el pipeline hexagonal (Chain of Responsibility
 * + Strategy + RAG + Spring AI Model Gateway).
 * ======================================================================================
 */
@Entity
@Table(name = "incident_reports", indexes = {
        @Index(name = "idx_inc_pod", columnList = "namespace, podName"),
        @Index(name = "idx_inc_severity", columnList = "severity")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String incidentId;

    @Column(nullable = false, length = 64)
    private String namespace;

    @Column(nullable = false, length = 128)
    private String podName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DiagnosticSeverity severity;

    @Column(length = 255)
    private String rootCause;

    @Column(columnDefinition = "TEXT")
    private String technicalDetails;

    @Column(columnDefinition = "TEXT")
    private String aiRecommendation;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "incident_suggested_remediations", joinColumns = @JoinColumn(name = "incident_id"))
    @Column(name = "remediation", length = 512)
    @Builder.Default
    private List<String> suggestedRemediations = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "incident_pipeline_trail", joinColumns = @JoinColumn(name = "incident_id"))
    @Column(name = "trail_step", length = 512)
    @Builder.Default
    private List<String> pipelineTrail = new ArrayList<>();

    @Column(nullable = false)
    private Instant analyzedAt;

    public static IncidentReportEntity fromDomain(DiagnosticReport domain) {
        return IncidentReportEntity.builder()
                .incidentId(domain.getIncidentId())
                .namespace(domain.getNamespace())
                .podName(domain.getPodName())
                .severity(domain.getSeverity())
                .rootCause(domain.getRootCause())
                .technicalDetails(domain.getTechnicalDetails())
                .aiRecommendation(domain.getAiRecommendation())
                .suggestedRemediations(domain.getSuggestedRemediations() != null ? new ArrayList<>(domain.getSuggestedRemediations()) : new ArrayList<>())
                .pipelineTrail(domain.getPipelineTrail() != null ? new ArrayList<>(domain.getPipelineTrail()) : new ArrayList<>())
                .analyzedAt(domain.getAnalyzedAt() != null ? domain.getAnalyzedAt() : Instant.now())
                .build();
    }

    public DiagnosticReport toDomain() {
        return DiagnosticReport.builder()
                .incidentId(this.incidentId)
                .namespace(this.namespace)
                .podName(this.podName)
                .severity(this.severity)
                .rootCause(this.rootCause)
                .technicalDetails(this.technicalDetails)
                .aiRecommendation(this.aiRecommendation)
                .suggestedRemediations(this.suggestedRemediations)
                .pipelineTrail(this.pipelineTrail)
                .analyzedAt(this.analyzedAt)
                .build();
    }
}
