package com.juanvargas.kubesynapse.infrastructure.adapter.in.web;

import com.juanvargas.kubesynapse.domain.model.ClusterOverview;
import com.juanvargas.kubesynapse.domain.model.DiagnosticReport;
import com.juanvargas.kubesynapse.domain.model.PodStatusInfo;
import com.juanvargas.kubesynapse.domain.model.RemediationResult;
import com.juanvargas.kubesynapse.domain.pattern.command.RestartPodCommand;
import com.juanvargas.kubesynapse.domain.pattern.command.ScaleDeploymentCommand;
import com.juanvargas.kubesynapse.application.port.in.DiagnosePodUseCase;
import com.juanvargas.kubesynapse.application.port.in.GetClusterOverviewUseCase;
import com.juanvargas.kubesynapse.application.port.out.KubernetesPort;
import com.juanvargas.kubesynapse.application.service.RemediationService;
import com.juanvargas.kubesynapse.infrastructure.adapter.in.web.dto.DiagnosePodRequest;
import com.juanvargas.kubesynapse.infrastructure.adapter.in.web.dto.RestartPodRequest;
import com.juanvargas.kubesynapse.infrastructure.adapter.in.web.dto.ScaleDeploymentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ======================================================================================
 * PATRÓN ARQUITECTÓNICO: INBOUND ADAPTER (Driving Adapter / Adaptador Primario)
 * ======================================================================================
 * Rol en Arquitectura Hexagonal:
 * - Es el punto de entrada desde el exterior (Web, Dashboards, BFF, cURL).
 * - Traduce peticiones HTTP (JSON, QueryParams) a llamadas de métodos sobre los
 *   casos de uso de entrada (Inbound Ports: DiagnosePodUseCase, GetClusterOverviewUseCase).
 * - NO contiene reglas de negocio. Su única responsabilidad es:
 *   1. Recibir y validar el DTO de entrada (@Valid).
 *   2. Invocar el puerto de entrada del dominio.
 *   3. Devolver la respuesta en formato HTTP (200 OK, 400 Bad Request, etc.).
 *
 * Referencias educativas:
 * - Alistair Cockburn (Hexagonal Architecture): https://alistair.cockburn.us/hexagonal-architecture/
 * - Martin Fowler (BFF - Backend For Frontend): https://martinfowler.com/articles/bff.html
 * ======================================================================================
 */
@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1")
@Tag(name = "KubeCKA-AI-Agent Engine", description = "Endpoints de Diagnóstico y Operación Inteligente sobre Kubernetes")
public class DiagnosticController {

    private final DiagnosePodUseCase diagnoseUseCase;
    private final GetClusterOverviewUseCase overviewUseCase;
    private final RemediationService remediationService;
    private final KubernetesPort k8sPort;

    public DiagnosticController(
            DiagnosePodUseCase diagnoseUseCase,
            GetClusterOverviewUseCase overviewUseCase,
            RemediationService remediationService,
            KubernetesPort k8sPort) {
        this.diagnoseUseCase = diagnoseUseCase;
        this.overviewUseCase = overviewUseCase;
        this.remediationService = remediationService;
        this.k8sPort = k8sPort;
    }

    @GetMapping("/cluster/overview")
    @Operation(summary = "Obtener vista general y salud de la topología del cluster Minikube")
    public ResponseEntity<ClusterOverview> getOverview() {
        return ResponseEntity.ok(overviewUseCase.getClusterOverview());
    }

    @GetMapping("/cluster/namespaces")
    @Operation(summary = "Listar namespaces disponibles en el cluster")
    public ResponseEntity<List<String>> listNamespaces() {
        return ResponseEntity.ok(k8sPort.listNamespaces());
    }

    @GetMapping("/cluster/pods")
    @Operation(summary = "Listar pods en un namespace específico")
    public ResponseEntity<List<PodStatusInfo>> listPods(@RequestParam(defaultValue = "default") String namespace) {
        return ResponseEntity.ok(k8sPort.listPods(namespace));
    }

    @PostMapping("/diagnostics/pod")
    @Operation(summary = "Ejecutar pipeline de diagnóstico automatizado (Chain of Resp + Strategy + AI) sobre un Pod")
    public ResponseEntity<DiagnosticReport> diagnosePod(@Valid @RequestBody DiagnosePodRequest request) {
        log.info("Solicitud de diagnóstico para pod {}/{}", request.getNamespace(), request.getPodName());
        DiagnosticReport report = diagnoseUseCase.diagnosePod(request.getNamespace(), request.getPodName());
        return ResponseEntity.ok(report);
    }

    @PostMapping("/remediations/restart-pod")
    @Operation(summary = "Ejecutar comando seguro de reinicio de Pod (Command Pattern)")
    public ResponseEntity<RemediationResult> restartPod(@Valid @RequestBody RestartPodRequest request) {
        RestartPodCommand command = new RestartPodCommand(
                request.getNamespace(),
                request.getPodName(),
                k8sPort,
                request.isDryRun()
        );
        RemediationResult result = remediationService.execute(command);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/remediations/scale-deployment")
    @Operation(summary = "Ejecutar comando de escalado de Deployment con soporte de Undo/Rollback (Command Pattern)")
    public ResponseEntity<RemediationResult> scaleDeployment(@Valid @RequestBody ScaleDeploymentRequest request) {
        ScaleDeploymentCommand command = new ScaleDeploymentCommand(
                request.getNamespace(),
                request.getDeploymentName(),
                request.getTargetReplicas(),
                request.getPreviousReplicas(),
                k8sPort,
                request.isDryRun()
        );
        RemediationResult result = remediationService.execute(command);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/remediations/undo")
    @Operation(summary = "Deshacer la última acción de remediación ejecutada (Command Undo)")
    public ResponseEntity<RemediationResult> undoLastRemediation() {
        return ResponseEntity.ok(remediationService.undoLastAction());
    }

    @GetMapping("/remediations/history")
    @Operation(summary = "Obtener el registro histórico de auditoría de comandos ejecutados")
    public ResponseEntity<List<RemediationResult>> getRemediationHistory() {
        return ResponseEntity.ok(remediationService.getExecutionHistory());
    }
}
