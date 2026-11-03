package com.juanvargas.kubesynapse.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.juanvargas.kubesynapse.domain.model.PodIncidentEvent;
import com.juanvargas.kubesynapse.application.port.out.EventPublisherPort;
import com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.entity.OutboxEventEntity;
import com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.repository.OutboxEventJpaRepository;
import com.juanvargas.kubesynapse.infrastructure.config.RabbitMqConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * ======================================================================================
 * PATRÓN ARQUITECTÓNICO: TRANSACTIONAL OUTBOX PUBLISHER
 * ======================================================================================
 * Resuelve el problema de fallos en el broker de mensajería (RabbitMQ caído):
 * 1. Cada evento se persiste primero en la base de datos ('outbox_events') como PENDING.
 * 2. Se intenta despachar a RabbitMQ de forma inmediata.
 * 3. Si RabbitMQ responde, se marca como PUBLISHED.
 * 4. Si RabbitMQ está caído o desconectado, el evento queda en la base de datos sin perderse.
 * 5. {@link OutboxEventRelayScheduler} lo reintentará automáticamente cuando el broker reviva.
 * ======================================================================================
 */
@Slf4j
@Component
public class RabbitMqEventPublisherAdapter implements EventPublisherPort {

    private final RabbitTemplate rabbitTemplate;
    private final OutboxEventJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Value("${kubesynapse.messaging.rabbitmq.enabled:true}")
    private boolean rabbitEnabled;

    public RabbitMqEventPublisherAdapter(RabbitTemplate rabbitTemplate,
                                         OutboxEventJpaRepository outboxRepository,
                                         ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishIncidentEvent(PodIncidentEvent event) {
        String routingKey = "k8s.incident." + (event.severity() != null ? event.severity().toLowerCase() : "unknown");
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            payloadJson = String.format("{\"incidentId\":\"%s\",\"podName\":\"%s\"}", event.incidentId(), event.podName());
        }

        // 1. Guardar atómicamente en Outbox (Base de Datos)
        OutboxEventEntity outbox = OutboxEventEntity.builder()
                .id(UUID.randomUUID().toString())
                .aggregateType("POD_INCIDENT")
                .aggregateId(event.incidentId())
                .routingKey(routingKey)
                .payload(payloadJson)
                .status("PENDING")
                .retryCount(0)
                .createdAt(Instant.now())
                .build();

        outbox = outboxRepository.save(outbox);
        log.info("💾 [Transactional Outbox]: Evento {} persistido en BD (outbox_events) con estado PENDING.", event.incidentId());

        if (!rabbitEnabled) {
            log.info("RabbitMQ desactivado por configuración. El evento queda resguardado en base de datos.");
            return;
        }

        // 2. Intentar entrega al Broker
        try {
            log.info("📡 [RabbitMQ Publisher]: Entregando evento {} hacia exchange '{}' con routingKey '{}'...",
                    event.incidentId(), RabbitMqConfig.EXCHANGE_NAME, routingKey);

            rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, routingKey, event);

            // 3. Confirmar entrega exitosa
            outbox.setStatus("PUBLISHED");
            outbox.setPublishedAt(Instant.now());
            outboxRepository.save(outbox);

            log.info("✅ [Transactional Outbox]: Evento {} entregado a RabbitMQ y actualizado a PUBLISHED en BD.", event.incidentId());
        } catch (Exception ex) {
            outbox.setLastError(ex.getMessage());
            outboxRepository.save(outbox);
            log.warn("⚠️ [Transactional Outbox Alert]: RabbitMQ no responde ({}). " +
                     "El evento {} QUEDA A SALVO en la tabla outbox_events para reintento automático.",
                    ex.getMessage(), event.incidentId());
        }
    }
}
