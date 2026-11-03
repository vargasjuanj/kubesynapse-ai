package com.juanvargas.kubesynapse.infrastructure.adapter.in.web;

import com.juanvargas.kubesynapse.application.port.out.PdfReportPort;
import com.juanvargas.kubesynapse.domain.exception.ExerciseNotFoundException;
import com.juanvargas.kubesynapse.domain.model.cka.*;
import com.juanvargas.kubesynapse.application.port.in.CkaExamUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ======================================================================================
 * ADAPTADOR PRIMARIO (Driving Adapter): CONTROLADOR REST DEL SIMULADOR CKA
 * ======================================================================================
 * Sigue la regla de Dependencia de Gabriel Chaldú:
 * - Depende del puerto de entrada {@link CkaExamUseCase}.
 * - Depende del puerto de salida {@link PdfReportPort} para descarga de certificados PDF.
 * ======================================================================================
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/cka")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Kubernetes Troubleshooting & SRE Challenge Engine", description = "Motor de Troubleshooting y Laboratorio de Desafíos Kubernetes y verificación en vivo")
public class CkaExamController {

    private final CkaExamUseCase ckaExamUseCase;
    private final PdfReportPort pdfReportPort;

    @GetMapping("/exercises")
    @Operation(summary = "Listar ejercicios de práctica CKA con filtro opcional por dominio")
    public ResponseEntity<List<CkaExercise>> listExercises(@RequestParam(required = false) CkaDomain domain) {
        return ResponseEntity.ok(ckaExamUseCase.getAllExercises(Optional.ofNullable(domain)));
    }

    @GetMapping("/exercises/{id}")
    @Operation(summary = "Obtener detalle completo de un ejercicio CKA")
    public ResponseEntity<CkaExercise> getExercise(@PathVariable String id) {
        return ckaExamUseCase.getExerciseById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ExerciseNotFoundException(id));
    }

    @PostMapping("/exercises")
    @Operation(summary = "Crear o importar un ejercicio dinámico CKA en MongoDB")
    public ResponseEntity<CkaExercise> createExercise(@RequestBody CkaExercise exercise) {
        log.info("🍃 [CKA Controller]: Registrando nuevo ejercicio dinámico en MongoDB: {}", exercise.getId());
        return ResponseEntity.ok(ckaExamUseCase.createExercise(exercise));
    }

    @PostMapping("/inject/{id}")
    @Operation(summary = "Inyectar el laboratorio / estado roto del ejercicio en el clúster Minikube")
    public ResponseEntity<CkaLabInjectionResult> injectLab(@PathVariable String id) {
        log.info("🧪 Solicitud de inyección de laboratorio CKA: {}", id);
        return ResponseEntity.ok(ckaExamUseCase.injectLab(id));
    }

    @PostMapping("/verify/{id}")
    @Operation(summary = "Verificar la solución del estudiante contra el clúster de Kubernetes en tiempo real")
    public ResponseEntity<CkaVerificationResult> verifySolution(@PathVariable String id) {
        log.info("🔍 Solicitud de verificación de solución CKA: {}", id);
        return ResponseEntity.ok(ckaExamUseCase.verifyExercise(id));
    }

    @PostMapping("/reset/{id}")
    @Operation(summary = "Limpiar y reiniciar el namespace del laboratorio CKA")
    public ResponseEntity<Void> resetLab(@PathVariable String id) {
        log.info("🧹 Solicitud de reinicio de laboratorio CKA: {}", id);
        ckaExamUseCase.resetLab(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/terminal/{id}")
    @Operation(summary = "Abrir terminal interactiva nativa (PowerShell/CMD) preconfigurada para el ejercicio")
    public ResponseEntity<Map<String, Object>> openTerminal(@PathVariable String id) {
        log.info("💻 Solicitud de apertura de terminal interactiva para el ejercicio CKA: {}", id);
        boolean launched = ckaExamUseCase.openLocalTerminal(id);
        return ResponseEntity.ok(Map.of(
                "exerciseId", id,
                "terminalLaunched", launched,
                "message", launched ? "Terminal iniciada con el contexto del ejercicio fijado." : "No se pudo iniciar la terminal local."
        ));
    }

    @GetMapping(value = "/exercises/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Exportar informe/certificado de evaluación CKA en formato PDF")
    public ResponseEntity<byte[]> downloadResultPdf(@PathVariable String id,
                                                    @RequestParam(defaultValue = "Operador SRE") String studentName) {
        Optional<CkaExercise> opt = ckaExamUseCase.getExerciseById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        CkaExercise ex = opt.get();
        CkaVerificationResult result = ckaExamUseCase.verifyExercise(id);
        byte[] pdfBytes = pdfReportPort.generateCkaResultPdf(ex, result, studentName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cka-report-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
