package com.juanvargas.kubesynapse.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.juanvargas.kubesynapse.domain.model.PodIncidentEvent;
import com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.entity.OutboxEventEntity;
import com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.repository.OutboxEventJpaRepository;
import com.juanvargas.kubesynapse.infrastructure.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * ======================================================================================
 * WORKER ASÍNCRONO: OUTBOX EVENT RELAY (SCHEDULER DE RESILIENCIA Y REINTENTOS)
 * ======================================================================================
 * Se ejecuta periódicamente en segundo plano:
 * 1. Busca eventos en 'outbox_events' con estado PENDING.
 * 2. Si RabbitMQ estuvo caído y volvió a conectarse, reintenta su publicación.
 * 3. Al tener éxito, actualiza el estado a PUBLISHED.
 * 4. Si supera el límite de reintentos (max 5), marca como FAILED para revisión SRE.
 * ======================================================================================
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class OutboxEventRelayScheduler {

    private final OutboxEventJpaRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRIES = 5;

    @Scheduled(fixedDelayString = "${kubesynapse.messaging.outbox.relay-interval-ms:15000}", initialDelay = 10000)
    public void processPendingOutboxEvents() {
        List<OutboxEventEntity> pendingEvents = outboxRepository.findTop20ByStatusOrderByCreatedAtAsc("PENDING");
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("🔄 [Outbox Relay]: Procesando {} eventos pendientes de entrega a RabbitMQ...", pendingEvents.size());

        for (OutboxEventEntity event : pendingEvents) {
            if (event.getRetryCount() >= MAX_RETRIES) {
                log.error("❌ [Outbox Relay]: Evento {} superó el límite de {} reintentos. Marcado como FAILED.",
                        event.getAggregateId(), MAX_RETRIES);
                event.setStatus("FAILED");
                outboxRepository.save(event);
                continue;
            }

            try {
                PodIncidentEvent incident = objectMapper.readValue(event.getPayload(), PodIncidentEvent.class);
                rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, event.getRoutingKey(), incident);

                event.setStatus("PUBLISHED");
                event.setPublishedAt(Instant.now());
                outboxRepository.save(event);

                log.info("✅ [Outbox Relay]: Evento {} entregado exitosamente a RabbitMQ tras {} reintentos.",
                        event.getAggregateId(), event.getRetryCount() + 1);
            } catch (Exception ex) {
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError("Reintento fallido: " + ex.getMessage());
                outboxRepository.save(event);
                log.warn("⏳ [Outbox Relay]: Falló reintento #{} para evento {}. RabbitMQ aún no responde.",
                        event.getRetryCount(), event.getAggregateId());
            }
        }
    }
}
