package com.juanvargas.kubesynapse.application.port.in;

import com.juanvargas.kubesynapse.domain.model.cka.*;

import java.util.List;
import java.util.Optional;

/**
 * ======================================================================================
 * PUERTO DE ENTRADA (Inbound Port): CASO DE USO DEL SIMULADOR CKA
 * ======================================================================================
 * Define las operaciones que el núcleo de la aplicación expone hacia los adaptadores
 * primarios (Controlador REST, CLI, Tests):
 * - Consulta del catálogo bilingüe de práctica CKA.
 * - Registro e importación dinámica de ejercicios en MongoDB.
 * - Inyección de caos / escenarios rotos en Minikube.
 * - Evaluación automatizada de la solución del estudiante.
 * - Limpieza de laboratorios.
 * ======================================================================================
 */
public interface CkaExamUseCase {

    List<CkaExercise> getAllExercises(Optional<CkaDomain> domainFilter);

    Optional<CkaExercise> getExerciseById(String id);

    CkaExercise createExercise(CkaExercise exercise);

    CkaLabInjectionResult injectLab(String exerciseId);

    CkaVerificationResult verifyExercise(String exerciseId);

    boolean resetLab(String exerciseId);

    boolean openLocalTerminal(String exerciseId);
}
