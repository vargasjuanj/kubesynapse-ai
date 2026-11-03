package com.juanvargas.kubesynapse.application.port.out;

import com.juanvargas.kubesynapse.domain.model.cka.CkaAssertionCheck;
import com.juanvargas.kubesynapse.domain.model.cka.CkaExercise;

import java.util.List;

/**
 * ======================================================================================
 * PUERTO DE SALIDA (Outbound Port / SPI): VERIFICACIÓN DE CLÚSTER CKA
 * ======================================================================================
 * Aísla completamente el dominio de la librería cliente de Kubernetes (Fabric8).
 * Define el contrato que el adaptador de infraestructura debe satisfacer para evaluar
 * las aserciones técnicas de resiliencia y salud del clúster sobre el clúster Minikube.
 * ======================================================================================
 */
public interface CkaClusterVerificationPort {

    /**
     * Evalúa las aserciones específicas del ejercicio contra los recursos reales del clúster.
     *
     * @param exercise Ejercicio CKA con su consigna y namespace objetivo
     * @return Lista de aserciones evaluadas con su estado (passed/failed) y evidencia
     */
    List<CkaAssertionCheck> verifyExercise(CkaExercise exercise);

    /**
     * Limpia un pod específico de forma preventiva sin bloquear.
     */
    void deletePodQuietly(String namespace, String podName);
}
