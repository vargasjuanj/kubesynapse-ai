package com.juanvargas.kubesynapse.application.port.in;

import com.juanvargas.kubesynapse.domain.model.RemediationResult;
import com.juanvargas.kubesynapse.domain.pattern.command.RemediationCommand;

/**
 * Inbound Port / Use Case: Executes or simulates a remediation command.
 */
public interface ExecuteRemediationUseCase {
    RemediationResult execute(RemediationCommand command);
}
