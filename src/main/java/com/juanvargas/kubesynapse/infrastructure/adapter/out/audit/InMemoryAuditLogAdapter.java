package com.juanvargas.kubesynapse.infrastructure.adapter.out.audit;

import com.juanvargas.kubesynapse.domain.model.RemediationResult;
import com.juanvargas.kubesynapse.application.port.out.AuditLogPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Outbound Adapter: Thread-safe in-memory audit log recorder for remediation actions.
 * Ready to be swapped with PostgreSQL / JPA without touching domain code.
 */
@Slf4j
@Component
public class InMemoryAuditLogAdapter implements AuditLogPort {

    private final List<RemediationResult> auditTrail = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void record(RemediationResult result) {
        log.info("[AUDIT] Acción: {} | Target: {} | Éxito: {} | DryRun: {} | Msg: {}",
                result.getAction(), result.getTarget(), result.isSuccess(), result.isDryRun(), result.getMessage());
        auditTrail.add(result);
    }

    @Override
    public List<RemediationResult> getRecentHistory() {
        synchronized (auditTrail) {
            return new ArrayList<>(auditTrail);
        }
    }
}
