package com.juanvargas.alertservice.consumer;

import com.juanvargas.alertservice.domain.PodIncidentEvent;
import com.juanvargas.alertservice.service.AlertDeduplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * ======================================================================================
 * CONSUMIDOR DE EVENTOS ASÍNCRONOS (RABBITMQ LISTENER)
 * ======================================================================================
 * Recibe los incidentes publicados por el Core Kube-CKA-AI-Assistant a través de RabbitMQ.
 * Desacoplamiento total: Si este servicio se apaga o se actualiza, los mensajes
 * se acumulan en la cola de RabbitMQ y se procesan al volver sin perder ningún fallo.
 * ======================================================================================
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IncidentAlertConsumer {

    private final AlertDeduplicationService alertService;

    @RabbitListener(queues = "${alertservice.queue-name:k8s.incidents.queue}")
    public void onIncidentReceived(PodIncidentEvent event) {
        log.info("📥 [RabbitMQ Consumer]: Evento de incidente {} recibido desde cola 'k8s.incidents.queue'.",
                event.incidentId());
        alertService.processIncident(event);
    }
}
