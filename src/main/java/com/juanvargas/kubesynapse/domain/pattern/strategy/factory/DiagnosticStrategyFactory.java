package com.juanvargas.kubesynapse.domain.pattern.strategy.factory;

import com.juanvargas.kubesynapse.domain.pattern.strategy.*;

import java.util.List;

/**
 * ======================================================================================
 * COMBINACIÓN DE PATRONES: FACTORY METHOD + STRATEGY PATTERN
 * ======================================================================================
 * Propósito del Factory Method:
 * - Define una interfaz/método para crear un objeto, pero deja que la lógica determine
 *   qué clase concreta instanciar.
 * - Desacopla al invocador (IncidentTriageService) de las clases concretas de estrategia.
 *
 * Propósito del Strategy Pattern:
 * - Define una familia de algoritmos (OomKilledStrategy, CrashLoopStrategy, NetworkDependencyStrategy),
 *   encapsula cada uno de ellos y los hace intercambiables en tiempo de ejecución.
 * - Cada estrategia sabe cómo estructurar el informe final (DiagnosticReport) con las
 *   recomendaciones y severidad precisas para su tipo de problema.
 *
 * Principio SOLID aplicado:
 * - Open/Closed Principle (OCP): Para soportar un nuevo diagnóstico de Kubernetes (ej. DiskPressure,
 *   NodeMemoryPressure), solo creamos una nueva clase `DiagnosticStrategy` sin modificar el código
 *   de las estrategias existentes.
 *
 * Referencias educativas:
 * - Refactoring.Guru (Strategy): https://refactoring.guru/es/design-patterns/strategy
 * - Refactoring.Guru (Factory Method): https://refactoring.guru/es/design-patterns/factory-method
 * - Gabriel Chaldu (DevTalles / Udemy): Cursos de Patrones de Diseño en Java y Spring Boot
 * ======================================================================================
 */
public class DiagnosticStrategyFactory {

    private final List<DiagnosticStrategy> strategies;
    private final DiagnosticStrategy defaultStrategy;

    public DiagnosticStrategyFactory() {
        this.strategies = List.of(
                new OomKilledStrategy(),
                new CrashLoopStrategy(),
                new NetworkDependencyStrategy()
        );
        this.defaultStrategy = new DefaultHealthyStrategy();
    }

    public DiagnosticStrategy getStrategy(String detectedReason) {
        if (detectedReason == null) {
            return defaultStrategy;
        }

        return strategies.stream()
                .filter(strategy -> strategy.canHandle(detectedReason))
                .findFirst()
                .orElse(defaultStrategy);
    }
}
