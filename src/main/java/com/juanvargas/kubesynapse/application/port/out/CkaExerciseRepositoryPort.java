package com.juanvargas.kubesynapse.application.port.out;

import com.juanvargas.kubesynapse.domain.model.cka.CkaDomain;
import com.juanvargas.kubesynapse.domain.model.cka.CkaExercise;

import java.util.List;
import java.util.Optional;

/**
 * ======================================================================================
 * PUERTO DE SALIDA (Outbound Port): REPOSITORIO DE EJERCICIOS CKA (MONGODB / NOSQL)
 * ======================================================================================
 * Permite almacenar y consultar dinámicamente escenarios de simulación, resiliencia y troubleshooting
 * para que no sean fijas, permitiendo agregar nuevas y generadas por IA.
 * ======================================================================================
 */
public interface CkaExerciseRepositoryPort {

    List<CkaExercise> findAll();

    List<CkaExercise> findByDomain(CkaDomain domain);

    Optional<CkaExercise> findById(String id);

    CkaExercise save(CkaExercise exercise);

    void saveAll(List<CkaExercise> exercises);

    long count();
}
