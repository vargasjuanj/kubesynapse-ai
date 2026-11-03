package com.juanvargas.kubesynapse.domain.model.cka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ======================================================================================
 * MODELO DE DOMINIO: EJERCICIO OFICIAL CKA (Certified Kubernetes Administrator)
 * ======================================================================================
 * Representa una escenario técnico práctico de administración y resiliencia con soporte 100% bilingüe
 * (Inglés técnico estándar de CNCF + Español para análisis y estudio profundo).
 * ======================================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CkaExercise {
    private String id;
    private CkaDomain domain;
    private CkaDifficulty difficulty;
    private int weightPoints;
    private int timeLimitMinutes;

    // Títulos Bilingües
    private String titleEn;
    private String titleEs;

    // Enunciado / Escenario Bilingüe (Formato exacto Killer.sh / CNCF PSI)
    private String scenarioEn;
    private String scenarioEs;

    // Recursos objetivos
    private String startingContext;
    private String targetNamespace;
    private String targetResource;

    // Soluciones Oficiales y Comandos Imperativos
    private String officialSolutionEn;
    private String officialSolutionEs;
    private String yamlSolution;

    // Mejores prácticas de ingeniería y resolución rápida de incidentes
    private String fastExamTipEn;
    private String fastExamTipEs;

    // Criterios de aceptación evaluados por el motor
    private List<String> verificationCriteria;
}
