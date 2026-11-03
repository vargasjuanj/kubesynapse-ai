package com.juanvargas.kubesynapse.domain.pattern.chain;

import com.juanvargas.kubesynapse.domain.model.DiagnosticSeverity;
import com.juanvargas.kubesynapse.domain.model.PodStatusInfo;

import java.util.List;

/**
 * Handler 5: Scans stdout/stderr logs for common Spring Boot and JVM exceptions.
 */
public class LogScannerHandler extends DiagnosticHandler {

    @Override
    protected void doHandle(DiagnosticContext context) {
        PodStatusInfo info = context.getPodInfo();
        List<String> logs = info.getRawLogs();

        if (logs == null || logs.isEmpty()) {
            context.addTrail("LogScannerHandler", "Sin logs disponibles para inspeccionar.");
            return;
        }

        String combinedLogs = String.join("\n", logs).toLowerCase();

        if (combinedLogs.contains("outofmemoryerror") || combinedLogs.contains("java heap space")) {
            context.setSeverity(DiagnosticSeverity.CRITICAL);
            context.setDetectedReason("JAVA_HEAP_OOM");
            context.addFinding("Excepción crítica detectada en logs: java.lang.OutOfMemoryError (Java heap space).");
            context.addTrail("LogScannerHandler", "Heap OOM localizado en stacktrace.");
            context.setConclusive(true);
        } else if (combinedLogs.contains("beancreationexception") || combinedLogs.contains("nosuchbeandefinitionexception")) {
            context.setSeverity(DiagnosticSeverity.CRITICAL);
            context.setDetectedReason("SPRING_BEAN_INITIALIZATION_ERROR");
            context.addFinding("Falla de inicialización de contexto Spring Boot (BeanCreationException / NoSuchBeanDefinitionException).");
            context.addTrail("LogScannerHandler", "Error de contexto Spring localizado.");
            context.setConclusive(true);
        } else if (combinedLogs.contains("connection refused") || combinedLogs.contains("connectiontimeout") || combinedLogs.contains("unknownhostexception")) {
            context.setSeverity(DiagnosticSeverity.WARNING);
            context.setDetectedReason("NETWORK_DEPENDENCY_FAILURE");
            context.addFinding("Fallas de conectividad hacia base de datos o microservicio dependiente (Connection refused / Timeout).");
            context.addTrail("LogScannerHandler", "Falla de red / dependencia detectada en logs.");
        } else {
            context.addTrail("LogScannerHandler", "Escaneo de logs finalizado sin patrones de excepción conocidos.");
        }
    }
}
