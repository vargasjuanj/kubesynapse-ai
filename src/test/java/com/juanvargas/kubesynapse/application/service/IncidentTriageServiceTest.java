package com.juanvargas.kubesynapse.application.service;

import com.juanvargas.kubesynapse.domain.model.DiagnosticReport;
import com.juanvargas.kubesynapse.domain.model.DiagnosticSeverity;
import com.juanvargas.kubesynapse.domain.model.PodStatusInfo;
import com.juanvargas.kubesynapse.application.port.out.AiDiagnosticsPort;
import com.juanvargas.kubesynapse.application.port.out.KubernetesPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Hexagonal Domain Service Test: IncidentTriageService")
class IncidentTriageServiceTest {

    @Mock
    private KubernetesPort k8sPort;

    @Mock
    private AiDiagnosticsPort aiPort;

    private IncidentTriageService triageService;

    @BeforeEach
    void setUp() {
        triageService = new IncidentTriageService(k8sPort, aiPort);
    }

    @Test
    @DisplayName("Debe orquestar el flujo hexagonal completo para un pod con falla OOMKilled")
    void shouldOrchestrateDiagnosticFlowForOomPod() {
        PodStatusInfo oomPod = PodStatusInfo.builder()
                .name("billing-job-99")
                .namespace("finance")
                .phase("Running")
                .restartCount(5)
                .lastExitCode(137)
                .lastTerminationReason("OOMKilled")
                .ready(false)
                .memoryLimit("256Mi")
                .clusterEvents(List.of("[Warning] OOMKilled"))
                .rawLogs(Collections.emptyList())
                .build();

        when(k8sPort.getPodStatus("finance", "billing-job-99")).thenReturn(Optional.of(oomPod));
        when(aiPort.generateRemediationAdvice(any(), eq("OOMKILLED"))).thenReturn("Sugerencia IA: Duplicar memory limits.");

        DiagnosticReport report = triageService.diagnosePod("finance", "billing-job-99");

        assertNotNull(report);
        assertEquals("billing-job-99", report.getPodName());
        assertEquals("finance", report.getNamespace());
        assertEquals(DiagnosticSeverity.CRITICAL, report.getSeverity());
        assertTrue(report.getRootCause().contains("OOMKilled"));
        assertEquals("Sugerencia IA: Duplicar memory limits.", report.getAiRecommendation());
        assertFalse(report.getSuggestedRemediations().isEmpty());
    }

    @Test
    @DisplayName("Debe manejar de forma resiliente cuando el pod no existe en el cluster")
    void shouldHandleNonExistentPod() {
        when(k8sPort.getPodStatus("default", "ghost-pod")).thenReturn(Optional.empty());

        DiagnosticReport report = triageService.diagnosePod("default", "ghost-pod");

        assertNotNull(report);
        assertEquals(DiagnosticSeverity.CRITICAL, report.getSeverity());
        assertTrue(report.getTechnicalDetails().contains("no existe"));
    }
}
