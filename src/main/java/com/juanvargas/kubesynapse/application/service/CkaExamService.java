package com.juanvargas.kubesynapse.application.service;

import com.juanvargas.kubesynapse.domain.model.cka.*;
import com.juanvargas.kubesynapse.application.port.in.CkaExamUseCase;
import com.juanvargas.kubesynapse.application.port.out.CkaClusterVerificationPort;
import com.juanvargas.kubesynapse.application.port.out.CkaExerciseRepositoryPort;
import com.juanvargas.kubesynapse.application.port.out.KubernetesPort;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;

/**
 * ======================================================================================
 * SERVICIO DE APLICACIÓN: Motor de Laboratorio y Desafíos de Troubleshooting (POJO PURO - ARQUITECTURA HEXAGONAL)
 * ======================================================================================
 * Cumple estrictamente con las reglas de Gabriel Chaldú y Clean Architecture:
 * - Cero anotaciones de framework en el dominio y puertos desacoplados.
 * - Soporte para repositorio NoSQL MongoDB para ejercicios dinámicos y extensibles.
 * - Núcleo de orquestación puro de la lógica de evaluación y entrenamiento CKA.
 * ======================================================================================
 */
@Slf4j
public class CkaExamService implements CkaExamUseCase {

    private final KubernetesPort k8sPort;
    private final CkaClusterVerificationPort clusterVerificationPort;
    private final CkaExerciseRepositoryPort exerciseRepo;

    public CkaExamService(KubernetesPort k8sPort,
                          CkaClusterVerificationPort clusterVerificationPort,
                          CkaExerciseRepositoryPort exerciseRepo) {
        this.k8sPort = k8sPort;
        this.clusterVerificationPort = clusterVerificationPort;
        this.exerciseRepo = exerciseRepo;
    }

    @Override
    public List<CkaExercise> getAllExercises(Optional<CkaDomain> domainFilter) {
        if (domainFilter.isPresent()) {
            return exerciseRepo.findByDomain(domainFilter.get());
        }
        return exerciseRepo.findAll();
    }

    @Override
    public Optional<CkaExercise> getExerciseById(String id) {
        return exerciseRepo.findById(id);
    }

    @Override
    public CkaExercise createExercise(CkaExercise exercise) {
        log.info("🍃 [CKA Engine]: Guardando ejercicio dinámico en MongoDB: {}", exercise.getId());
        return exerciseRepo.save(exercise);
    }

    @Override
    public CkaLabInjectionResult injectLab(String exerciseId) {
        Optional<CkaExercise> opt = getExerciseById(exerciseId);
        if (opt.isEmpty()) {
            return CkaLabInjectionResult.builder()
                    .exerciseId(exerciseId)
                    .status("FAILED")
                    .message("Ejercicio no encontrado en el catálogo CKA: " + exerciseId)
                    .injectedAt(Instant.now())
                    .build();
        }

        CkaExercise ex = opt.get();
        String ns = ex.getTargetNamespace();
        List<String> created = new ArrayList<>();

        log.info("🧪 [CKA Lab Engine]: Inyectando escenario '{}' en namespace '{}'...", ex.getId(), ns);
        k8sPort.createNamespace(ns);
        created.add("Namespace: " + ns);

        // Limpieza preventiva previa para asegurar idempotencia total ante re-inyecciones
        if (ex.getTargetResource() != null && !ex.getTargetResource().isBlank()) {
            clusterVerificationPort.deletePodQuietly(ns, ex.getTargetResource());
        }

        try {
            switch (ex.getId()) {
                case "CKA-TROUBLE-01" -> {
                    String yaml = """
                            apiVersion: v1
                            kind: Pod
                            metadata:
                              name: api-gateway
                              namespace: cka-troubleshoot
                              labels:
                                app: api-gateway
                            spec:
                              restartPolicy: Always
                              containers:
                              - name: gateway
                                image: busybox:1.36
                                command: ["sh", "-c", "cat /non-existent-secret-config.yaml && sleep 3600"]
                            """;
                    k8sPort.applyYaml(ns, yaml);
                    created.add("Pod: api-gateway (CrashLoopBackOff / ExitCode 1)");
                }

                case "CKA-TROUBLE-02" -> {
                    String yaml = """
                            apiVersion: v1
                            kind: Pod
                            metadata:
                              name: worker-cache
                              namespace: cka-troubleshoot
                              labels:
                                app: worker-cache
                            spec:
                              restartPolicy: Always
                              containers:
                              - name: cache
                                image: redis:7.2-alpine
                                resources:
                                  limits:
                                    memory: "16Mi"
                            """;
                    k8sPort.applyYaml(ns, yaml);
                    created.add("Pod: worker-cache (OOMKilled - límite insuficiente 16Mi)");
                }

                case "CKA-ARCH-01" -> {
                    String yaml = """
                            apiVersion: v1
                            kind: Namespace
                            metadata:
                              name: cka-security
                            """;
                    k8sPort.applyYaml(ns, yaml);
                    created.add("Namespace: cka-security (Objetivo: Crear ServiceAccount, Role y RoleBinding)");
                }

                case "CKA-NET-01" -> {
                    String yaml = """
                            apiVersion: v1
                            kind: Pod
                            metadata:
                              name: web-backend
                              namespace: cka-networking
                              labels:
                                app: web-app
                            spec:
                              containers:
                              - name: nginx
                                image: nginx:alpine
                                ports:
                                - containerPort: 80
                            ---
                            apiVersion: v1
                            kind: Service
                            metadata:
                              name: web-service
                              namespace: cka-networking
                            spec:
                              selector:
                                app: incorrect-selector-name
                              ports:
                              - protocol: TCP
                                port: 80
                                targetPort: 80
                            """;
                    k8sPort.applyYaml(ns, yaml);
                    created.add("Pod: web-backend & Service: web-service con selector roto");
                }

                case "CKA-NET-02" -> {
                    String yaml = """
                            apiVersion: v1
                            kind: Pod
                            metadata:
                              name: database-pod
                              namespace: cka-network-policy
                              labels:
                                app: database
                            spec:
                              containers:
                              - name: db
                                image: postgres:15-alpine
                                env:
                                - name: POSTGRES_PASSWORD
                                  value: secretpass
                            """;
                    k8sPort.applyYaml(ns, yaml);
                    created.add("Pod: database-pod (Objetivo: Aislar con NetworkPolicy)");
                }

                case "CKA-SCHED-01" -> {
                    String yaml = """
                            apiVersion: v1
                            kind: Namespace
                            metadata:
                              name: cka-scheduling
                            """;
                    k8sPort.applyYaml(ns, yaml);
                    created.add("Namespace: cka-scheduling (Objetivo: Tolerations y NodeSelector)");
                }

                case "CKA-STORE-01" -> {
                    String yaml = """
                            apiVersion: v1
                            kind: PersistentVolume
                            metadata:
                              name: pv-analytics
                            spec:
                              capacity:
                                storage: 1Gi
                              accessModes:
                                - ReadWriteOnce
                              hostPath:
                                path: /mnt/data/analytics
                            """;
                    k8sPort.applyYaml(ns, yaml);
                    created.add("PersistentVolume: pv-analytics (Objetivo: Vincular PVC en cka-storage)");
                }

                default -> log.warn("Ejercicio sin inyección de caos específica: {}", ex.getId());
            }

            return CkaLabInjectionResult.builder()
                    .exerciseId(ex.getId())
                    .status("INJECTED")
                    .namespace(ns)
                    .message("Laboratorio CKA inyectado exitosamente en Minikube.")
                    .resourcesCreated(created)
                    .startingCommand("kubectl config set-context --current --namespace=" + ns)
                    .injectedAt(Instant.now())
                    .build();

        } catch (Exception exErr) {
            log.error("Fallo al inyectar escenario CKA {}: {}", ex.getId(), exErr.getMessage(), exErr);
            return CkaLabInjectionResult.builder()
                    .exerciseId(ex.getId())
                    .status("ERROR")
                    .namespace(ns)
                    .message("Error al inyectar laboratorio en el clúster: " + exErr.getMessage())
                    .resourcesCreated(created)
                    .injectedAt(Instant.now())
                    .build();
        }
    }

    @Override
    public CkaVerificationResult verifyExercise(String exerciseId) {
        Optional<CkaExercise> opt = getExerciseById(exerciseId);
        if (opt.isEmpty()) {
            return CkaVerificationResult.builder()
                    .exerciseId(exerciseId)
                    .passed(false)
                    .scoreEarned(0)
                    .maxScore(0)
                    .statusMessage("Ejercicio desconocido: " + exerciseId)
                    .checks(List.of())
                    .verifiedAt(Instant.now())
                    .build();
        }

        CkaExercise ex = opt.get();
        log.info("🔍 [CKA Lab Engine]: Verificando solución para '{}' a través de CkaClusterVerificationPort...", ex.getId());

        List<CkaAssertionCheck> checks = clusterVerificationPort.verifyExercise(ex);
        boolean passed = !checks.isEmpty() && checks.stream().allMatch(CkaAssertionCheck::isPassed);

        int scoreEarned = passed ? ex.getWeightPoints() : 0;
        String msg = passed
                ? "🎉 ¡EJERCICIO APROBADO CON ÉXITO! (+ " + scoreEarned + " puntos CKA)"
                : "⚠️ La configuración actual no cumple con todas las aserciones de resiliencia requeridas. Revisa las aserciones fallidas abajo.";

        return CkaVerificationResult.builder()
                .exerciseId(ex.getId())
                .passed(passed)
                .scoreEarned(scoreEarned)
                .maxScore(ex.getWeightPoints())
                .statusMessage(msg)
                .checks(checks)
                .studentTips(ex.getFastExamTipEs())
                .verifiedAt(Instant.now())
                .build();
    }

    @Override
    public boolean resetLab(String exerciseId) {
        Optional<CkaExercise> opt = getExerciseById(exerciseId);
        if (opt.isEmpty()) return false;
        String ns = opt.get().getTargetNamespace();
        log.info("🧹 [CKA Lab Engine]: Limpiando namespace de laboratorio '{}'...", ns);
        return k8sPort.deleteNamespace(ns);
    }

    @Override
    public boolean openLocalTerminal(String exerciseId) {
        Optional<CkaExercise> opt = getExerciseById(exerciseId);
        String ns = opt.map(CkaExercise::getTargetNamespace).orElse("default");
        String title = opt.map(CkaExercise::getId).orElse("CKA-EXAM");

        try {
            log.info("💻 [CKA Engine]: Abriendo terminal interactiva para el ejercicio '{}' (ns: {})...", title, ns);
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("win")) {
                String cmd = String.format(
                        "cmd.exe /c start powershell.exe -NoExit -Command \"" +
                        "Write-Host '=====================================================' -ForegroundColor Cyan; " +
                        "Write-Host '  Kube-CKA-AI-Assistant — Terminal de Práctica CKA' -ForegroundColor Yellow; " +
                        "Write-Host '  Ejercicio: %s | Namespace: %s' -ForegroundColor Green; " +
                        "Write-Host '=====================================================' -ForegroundColor Cyan; " +
                        "kubectl config set-context --current --namespace=%s; " +
                        "Write-Host 'Contexto fijado en namespace: %s'; " +
                        "kubectl get all -n %s;" +
                        "\"",
                        title, ns, ns, ns, ns
                );
                Runtime.getRuntime().exec(cmd);
                return true;
            } else {
                log.info("Terminal nativa no soportada en este entorno: {}", os);
                return false;
            }
        } catch (Exception ex) {
            log.error("No se pudo abrir la terminal interactiva: {}", ex.getMessage());
            return false;
        }
    }
}
