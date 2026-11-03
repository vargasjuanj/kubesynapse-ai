package com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence;

import com.juanvargas.kubesynapse.domain.model.RemediationResult;
import com.juanvargas.kubesynapse.application.port.out.AuditLogPort;
import com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.entity.RemediationAuditEntity;
import com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.repository.RemediationAuditJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ======================================================================================
 * OUTBOUND ADAPTER (Driven Adapter): PERSISTENCIA JPA DE AUDITORÍA SRE
 * ======================================================================================
 * Implementa el puerto de salida AuditLogPort del dominio utilizando Spring Data JPA y H2.
 * Cumple con el Principio de Inversión de Dependencias (DIP): el dominio solo conoce la
 * interfaz AuditLogPort; esta clase contiene los detalles de SQL y tablas.
 * ======================================================================================
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class JpaAuditLogAdapter implements AuditLogPort {

    private final RemediationAuditJpaRepository auditRepository;

    @Override
    public void record(RemediationResult result) {
        log.info("💾 [JPA Audit]: Persistiendo comando '{}' sobre '{}' en base de datos...", result.getAction(), result.getTarget());
        RemediationAuditEntity entity = RemediationAuditEntity.fromDomain(result);
        auditRepository.save(entity);
    }

    @Override
    public List<RemediationResult> getRecentHistory() {
        return auditRepository.findTop50ByOrderByExecutedAtDesc()
                .stream()
                .map(RemediationAuditEntity::toDomain)
                .toList();
    }
}
