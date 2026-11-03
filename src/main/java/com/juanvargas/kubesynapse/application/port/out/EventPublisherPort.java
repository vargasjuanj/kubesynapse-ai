package com.juanvargas.kubesynapse.application.port.out;

import com.juanvargas.kubesynapse.domain.model.PodIncidentEvent;

/**
 * ======================================================================================
 * PATRÓN ARQUITECTÓNICO: OUTBOUND PORT (Driven Port / SPI de Mensajería Asíncrona)
 * ======================================================================================
 * Rol en Arquitectura Hexagonal:
 * - Define el contrato que el Dominio exige para publicar eventos hacia el exterior.
 * - El Dominio no sabe si el broker subyacente es RabbitMQ, Apache Kafka, AWS SQS o GCP Pub/Sub.
 * - Permite probar el dominio con mocks unitarios sin levantar servidores de mensajería.
 *
 * Referencias educativas:
 * - Alistair Cockburn (Hexagonal Architecture Ports): https://alistair.cockburn.us/hexagonal-architecture/
 * ======================================================================================
 */
public interface EventPublisherPort {

    /**
     * Publica un evento de incidente detectado hacia el broker de mensajería.
     *
     * @param event El evento inmutable con los detalles del incidente.
     */
    void publishIncidentEvent(PodIncidentEvent event);
}
