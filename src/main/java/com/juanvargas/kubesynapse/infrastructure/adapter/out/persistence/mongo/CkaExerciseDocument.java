package com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.mongo;

import com.juanvargas.kubesynapse.domain.model.cka.CkaDifficulty;
import com.juanvargas.kubesynapse.domain.model.cka.CkaDomain;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * ======================================================================================
 * DOCUMENTO MONGODB (NoSQL): CATÁLOGO DINÁMICO DE EJERCICIOS CKA
 * ======================================================================================
 * Permite que las preguntas no sean fijas, sino persistidas dinámicamente en MongoDB.
 * ======================================================================================
 */
@Document(collection = "cka_exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CkaExerciseDocument {

    @Id
    private String id;

    private CkaDomain domain;
    private String titleEn;
    private String titleEs;
    private String scenarioEn;
    private String scenarioEs;
    private String startingContext;
    private String targetNamespace;
    private String targetResource;
    private int timeLimitMinutes;
    private int weightPoints;
    private CkaDifficulty difficulty;
    private String fastExamTipEn;
    private String fastExamTipEs;
    private String officialSolutionEn;
    private String officialSolutionEs;
    private String yamlSolution;
    private List<String> verificationCriteria;

    @Builder.Default
    private boolean dynamicGenerated = false;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
