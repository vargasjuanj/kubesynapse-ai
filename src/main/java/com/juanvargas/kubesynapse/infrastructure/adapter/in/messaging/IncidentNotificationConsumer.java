package com.juanvargas.kubesynapse.infrastructure.adapter.in.messaging;

import com.juanvargas.kubesynapse.domain.model.PodIncidentEvent;
import com.juanvargas.kubesynapse.infrastructure.config.RabbitMqConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * ======================================================================================
 * PATRÓN ARQUITECTÓNICO: INBOUND ADAPTER (Driving / Secondary Microservice Consumer)
 * ======================================================================================
 * Rol en la arquitectura de Microservicios:
 * - Demuestra el desacoplamiento asíncrono de eventos (Event-Driven Architecture).
 * - Simula el rol de un microservicio satélite (ej. Servicio de Notificaciones SRE,
 *   Slack / PagerDuty o Auto-Remediación) que escucha la cola de RabbitMQ.
 * - 'autoStartup' configurable: Por defecto apagado para no reintentar conexiones si
 *   el broker no está iniciado en local, activable con un flag cuando RabbitMQ esté corriendo.
 *
 * Referencias educativas:
 * - Enterprise Integration Patterns (Message Driven Consumer):
 *   https://www.enterpriseintegrationpatterns.com/patterns/messaging/EventDrivenConsumer.html
 * ======================================================================================
 */
@Slf4j
@Component
public class IncidentNotificationConsumer {

    @RabbitListener(
            queues = RabbitMqConfig.QUEUE_NAME,
            autoStartup = "${kubesynapse.messaging.rabbitmq.consumer.enabled:false}"
    )
    public void consumeIncidentEvent(PodIncidentEvent event) {
        log.info("🔔 [Microservicio Satélite Consumidor]: Evento {} recibido desde RabbitMQ.", event.incidentId());
        log.info("   Target: Pod '{}/{}' | Severidad: [{}]", event.namespace(), event.podName(), event.severity());
        log.info("   Causa: {}", event.rootCause());
        log.info("   Acción automática: Notificación SRE despachada.");
    }
}
