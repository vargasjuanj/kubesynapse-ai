package com.juanvargas.kubesynapse.domain.pattern.chain;

import com.juanvargas.kubesynapse.domain.model.DiagnosticSeverity;
import com.juanvargas.kubesynapse.domain.model.PodStatusInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pattern Test: Chain of Responsibility - Diagnostic Pipeline")
class DiagnosticPipelineTest {

    private DiagnosticPipeline pipeline;

    @BeforeEach
    void setUp() {
        pipeline = new DiagnosticPipeline();
    }

    @Test
    @DisplayName("Debe detectar OOMKilled cuando el motivo de terminación es OOMKilled")
    void shouldDetectOomKilledCorrectly() {
        PodStatusInfo oomPod = PodStatusInfo.builder()
                .name("payment-service-xyz")
                .namespace("default")
                .phase("Running")
                .restartCount(3)
                .lastExitCode(137)
                .lastTerminationReason("OOMKilled")
                .ready(false)
                .memoryLimit("512Mi")
                .clusterEvents(List.of("[Warning] OOMKilled: Container exceeded memory limit"))
                .rawLogs(Collections.emptyList())
                .build();

        DiagnosticContext result = pipeline.execute("default", "payment-service-xyz", oomPod);

        assertEquals("OOMKILLED", result.getDetectedReason());
        assertEquals(DiagnosticSeverity.CRITICAL, result.getSeverity());
        assertTrue(result.isConclusive());
        assertTrue(result.getPipelineTrail().stream().anyMatch(t -> t.contains("OomKilledHandler")));
    }

    @Test
    @DisplayName("Debe detectar Java Heap OutOfMemory en los logs de la aplicación")
    void shouldDetectJavaHeapOomInLogs() {
        PodStatusInfo crashingPod = PodStatusInfo.builder()
                .name("order-service-abc")
                .namespace("production")
                .phase("Running")
                .restartCount(1)
                .lastExitCode(1)
                .lastTerminationReason("Error")
                .ready(false)
                .clusterEvents(Collections.emptyList())
                .rawLogs(List.of(
                    "2026-09-28 10:00:00.123 ERROR [main] c.j.order.OrderService : Processing order failed",
                    "java.lang.OutOfMemoryError: Java heap space",
                    "   at java.base/java.util.Arrays.copyOf(Arrays.java:3512)"
                ))
                .build();

        DiagnosticContext result = pipeline.execute("production", "order-service-abc", crashingPod);

        assertEquals("JAVA_HEAP_OOM", result.getDetectedReason());
        assertEquals(DiagnosticSeverity.CRITICAL, result.getSeverity());
        assertTrue(result.isConclusive());
    }

    @Test
    @DisplayName("Debe clasificar como HEALTHY un pod sin reinicios y en fase Running")
    void shouldHandleHealthyPodCorrectly() {
        PodStatusInfo healthyPod = PodStatusInfo.builder()
                .name("auth-service-123")
                .namespace("default")
                .phase("Running")
                .restartCount(0)
                .lastExitCode(null)
                .ready(true)
                .clusterEvents(Collections.emptyList())
                .rawLogs(List.of("Tomcat started on port 8080 (http)"))
                .build();

        DiagnosticContext result = pipeline.execute("default", "auth-service-123", healthyPod);

        assertEquals(DiagnosticSeverity.HEALTHY, result.getSeverity());
        assertFalse(result.isConclusive());
    }
}
