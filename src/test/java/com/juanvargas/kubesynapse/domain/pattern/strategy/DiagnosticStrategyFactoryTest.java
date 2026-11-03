package com.juanvargas.kubesynapse.domain.pattern.strategy;

import com.juanvargas.kubesynapse.domain.pattern.strategy.factory.DiagnosticStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("Pattern Test: Strategy & Factory Method - Diagnostic Strategies")
class DiagnosticStrategyFactoryTest {

    private DiagnosticStrategyFactory factory;

    @BeforeEach
    void setUp() {
        factory = new DiagnosticStrategyFactory();
    }

    @Test
    @DisplayName("Debe resolver OomKilledStrategy ante razón OOMKILLED")
    void shouldResolveOomStrategy() {
        DiagnosticStrategy strategy = factory.getStrategy("OOMKILLED");
        assertInstanceOf(OomKilledStrategy.class, strategy);
    }

    @Test
    @DisplayName("Debe resolver CrashLoopStrategy ante razón SPRING_BEAN_INITIALIZATION_ERROR")
    void shouldResolveCrashLoopStrategy() {
        DiagnosticStrategy strategy = factory.getStrategy("SPRING_BEAN_INITIALIZATION_ERROR");
        assertInstanceOf(CrashLoopStrategy.class, strategy);
    }

    @Test
    @DisplayName("Debe resolver NetworkDependencyStrategy ante razón NETWORK_DEPENDENCY_FAILURE")
    void shouldResolveNetworkStrategy() {
        DiagnosticStrategy strategy = factory.getStrategy("NETWORK_DEPENDENCY_FAILURE");
        assertInstanceOf(NetworkDependencyStrategy.class, strategy);
    }

    @Test
    @DisplayName("Debe resolver DefaultHealthyStrategy ante razón desconocida o nula")
    void shouldResolveDefaultStrategy() {
        DiagnosticStrategy strategy = factory.getStrategy("UNKNOWN");
        assertInstanceOf(DefaultHealthyStrategy.class, strategy);

        DiagnosticStrategy nullStrategy = factory.getStrategy(null);
        assertInstanceOf(DefaultHealthyStrategy.class, nullStrategy);
    }
}
