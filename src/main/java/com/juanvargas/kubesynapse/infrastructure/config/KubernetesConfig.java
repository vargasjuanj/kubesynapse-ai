package com.juanvargas.kubesynapse.infrastructure.config;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Configuration: Configures the Fabric8 Kubernetes Client.
 * Automatically loads credentials and endpoint from ~/.kube/config (Minikube).
 */
@Slf4j
@Configuration
public class KubernetesConfig {

    @Bean
    public KubernetesClient kubernetesClient() {
        log.info("Inicializando Fabric8 KubernetesClient desde la configuración local (~/.kube/config)...");
        try {
            KubernetesClient client = new KubernetesClientBuilder().build();
            log.info("Conexión con Kubernetes API Server establecida: {}", client.getMasterUrl());
            return client;
        } catch (Exception e) {
            log.warn("No se pudo conectar al cluster local de Kubernetes ({}) - Se usará cliente en modo degradado.", e.getMessage());
            return new KubernetesClientBuilder().build();
        }
    }
}
