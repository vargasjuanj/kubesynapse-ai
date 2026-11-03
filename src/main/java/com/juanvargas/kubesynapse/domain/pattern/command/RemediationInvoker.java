package com.juanvargas.kubesynapse.domain.pattern.command;

import com.juanvargas.kubesynapse.domain.model.RemediationResult;
import com.juanvargas.kubesynapse.application.port.out.AuditLogPort;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Invoker in the Command Pattern: Manages execution, audit trail, and undo history.
 */
public class RemediationInvoker {

    private final AuditLogPort auditLogPort;
    private final Deque<RemediationCommand> history = new ArrayDeque<>();
    private final List<RemediationResult> executionLog = new ArrayList<>();

    public RemediationInvoker(AuditLogPort auditLogPort) {
        this.auditLogPort = auditLogPort;
    }

    public RemediationResult executeCommand(RemediationCommand command) {
        RemediationResult result = command.execute();

        if (result.isSuccess()) {
            history.push(command);
        }

        executionLog.add(result);
        if (auditLogPort != null) {
            auditLogPort.record(result);
        }

        return result;
    }

    public RemediationResult undoLastCommand() {
        if (history.isEmpty()) {
            return RemediationResult.builder()
                    .commandId("NONE")
                    .action("NONE")
                    .target("NONE")
                    .success(false)
                    .dryRun(false)
                    .message("No hay comandos ejecutados previamente para deshacer.")
                    .build();
        }

        RemediationCommand lastCommand = history.pop();
        RemediationResult undoResult = lastCommand.undo();
        executionLog.add(undoResult);
        if (auditLogPort != null) {
            auditLogPort.record(undoResult);
        }
        return undoResult;
    }

    public List<RemediationResult> getExecutionLog() {
        return new ArrayList<>(executionLog);
    }
}
