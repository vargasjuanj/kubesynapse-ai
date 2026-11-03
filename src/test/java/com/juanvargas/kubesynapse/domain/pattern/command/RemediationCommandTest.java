package com.juanvargas.kubesynapse.domain.pattern.command;

import com.juanvargas.kubesynapse.domain.model.RemediationResult;
import com.juanvargas.kubesynapse.application.port.out.AuditLogPort;
import com.juanvargas.kubesynapse.application.port.out.KubernetesPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pattern Test: Command Pattern - Remediation Invoker & Undo")
class RemediationCommandTest {

    @Mock
    private KubernetesPort k8sPort;

    @Mock
    private AuditLogPort auditLogPort;

    private RemediationInvoker invoker;

    @BeforeEach
    void setUp() {
        invoker = new RemediationInvoker(auditLogPort);
    }

    @Test
    @DisplayName("Debe ejecutar RestartPodCommand en modo simulación (Dry-Run) sin invocar el puerto K8s")
    void shouldExecuteRestartInDryRunMode() {
        RestartPodCommand command = new RestartPodCommand("default", "failing-pod", k8sPort, true);

        RemediationResult result = invoker.executeCommand(command);

        assertTrue(result.isSuccess());
        assertTrue(result.isDryRun());
        assertEquals("RESTART_POD", result.getAction());
        verify(k8sPort, never()).restartPod(anyString(), anyString());
        verify(auditLogPort, times(1)).record(result);
    }

    @Test
    @DisplayName("Debe ejecutar ScaleDeploymentCommand real y permitir deshacer (Undo) al estado previo")
    void shouldExecuteScaleAndSupportUndo() {
        when(k8sPort.scaleDeployment("default", "orders-api", 5)).thenReturn(true);
        when(k8sPort.scaleDeployment("default", "orders-api", 2)).thenReturn(true);

        ScaleDeploymentCommand command = new ScaleDeploymentCommand("default", "orders-api", 5, 2, k8sPort, false);

        // 1. Execute Command (scale from 2 to 5)
        RemediationResult executeResult = invoker.executeCommand(command);
        assertTrue(executeResult.isSuccess());
        assertFalse(executeResult.isDryRun());
        verify(k8sPort, times(1)).scaleDeployment("default", "orders-api", 5);

        // 2. Undo Command (rollback from 5 to 2)
        RemediationResult undoResult = invoker.undoLastCommand();
        assertTrue(undoResult.isSuccess());
        assertEquals("UNDO_SCALE_DEPLOYMENT", undoResult.getAction());
        verify(k8sPort, times(1)).scaleDeployment("default", "orders-api", 2);

        // 3. Verify execution log contains 2 entries
        assertEquals(2, invoker.getExecutionLog().size());
    }
}
