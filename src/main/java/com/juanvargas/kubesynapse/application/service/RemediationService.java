package com.juanvargas.kubesynapse.application.service;

import com.juanvargas.kubesynapse.domain.model.RemediationResult;
import com.juanvargas.kubesynapse.domain.pattern.command.RemediationCommand;
import com.juanvargas.kubesynapse.domain.pattern.command.RemediationInvoker;
import com.juanvargas.kubesynapse.application.port.in.ExecuteRemediationUseCase;
import com.juanvargas.kubesynapse.application.port.out.AuditLogPort;

import java.util.List;

/**
 * Domain Service: Implements ExecuteRemediationUseCase.
 * Delegates execution to the RemediationInvoker for auditability.
 */
public class RemediationService implements ExecuteRemediationUseCase {

    private final RemediationInvoker invoker;

    public RemediationService(AuditLogPort auditLogPort) {
        this.invoker = new RemediationInvoker(auditLogPort);
    }

    @Override
    public RemediationResult execute(RemediationCommand command) {
        return invoker.executeCommand(command);
    }

    public RemediationResult undoLastAction() {
        return invoker.undoLastCommand();
    }

    public List<RemediationResult> getExecutionHistory() {
        return invoker.getExecutionLog();
    }
}
