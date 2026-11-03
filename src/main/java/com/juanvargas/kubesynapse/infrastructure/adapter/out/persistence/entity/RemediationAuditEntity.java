package com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.entity;

import com.juanvargas.kubesynapse.domain.model.RemediationResult;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * ======================================================================================
 * PERSISTENCIA JPA / ENTIDAD DE BASE DE DATOS: AUDITORÍA DE REMEDIACIONES SRE
 * ======================================================================================
 * Almacena de forma inmutable cada comando de remediación ejecutado sobre el clúster
 * (Restart, Scale, Undo, Dry-Run) para auditoría de cumplimiento normativo (SOX / ISO 27001).
 * ======================================================================================
 */
@Entity
@Table(name = "remediation_audit", indexes = {
        @Index(name = "idx_rem_target", columnList = "target"),
        @Index(name = "idx_rem_executed_at", columnList = "executedAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemediationAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String commandId;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(nullable = false, length = 128)
    private String target;

    @Column(nullable = false)
    private boolean success;

    @Column(nullable = false)
    private boolean dryRun;

    @Column(length = 1024)
    private String message;

    @Column(nullable = false)
    private Instant executedAt;

    public static RemediationAuditEntity fromDomain(RemediationResult domain) {
        return RemediationAuditEntity.builder()
                .commandId(domain.getCommandId())
                .action(domain.getAction())
                .target(domain.getTarget())
                .success(domain.isSuccess())
                .dryRun(domain.isDryRun())
                .message(domain.getMessage())
                .executedAt(domain.getExecutedAt() != null ? domain.getExecutedAt() : Instant.now())
                .build();
    }

    public RemediationResult toDomain() {
        return RemediationResult.builder()
                .commandId(this.commandId)
                .action(this.action)
                .target(this.target)
                .success(this.success)
                .dryRun(this.dryRun)
                .message(this.message)
                .executedAt(this.executedAt)
                .build();
    }
}
