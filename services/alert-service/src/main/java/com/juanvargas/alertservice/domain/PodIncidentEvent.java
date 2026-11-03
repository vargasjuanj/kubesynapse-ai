package com.juanvargas.alertservice.domain;

import java.io.Serializable;
import java.time.Instant;

/**
 * Evento inmutable (Java Record) que viaja por el broker RabbitMQ.
 */
public record PodIncidentEvent(
        String incidentId,
        String namespace,
        String podName,
        String severity,
        String rootCause,
        Instant timestamp
) implements Serializable {}
