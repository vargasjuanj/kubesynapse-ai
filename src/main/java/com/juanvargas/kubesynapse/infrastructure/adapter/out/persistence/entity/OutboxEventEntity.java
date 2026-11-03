package com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * ======================================================================================
 * PATRÓN ARQUITECTÓNICO: TRANSACTIONAL OUTBOX PATTERN (RESILIENCIA ANTE CAÍDAS DE RABBITMQ)
 * ======================================================================================
 * Si RabbitMQ se cae, se reinicia o sufre latencia de red, los eventos de incidentes
 * NO se pierden en el aire. Se persisten de forma transaccional en la tabla 'outbox_events'
 * con estado PENDING. Un worker en segundo plano reintenta su publicación cuando el broker
 * vuelve a la vida (Garantía de entrega At-Least-Once).
 * ======================================================================================
 */
@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_outbox_status", columnList = "status"),
        @Index(name = "idx_outbox_created", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEventEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, length = 64)
    private String aggregateType;

    @Column(nullable = false, length = 64)
    private String aggregateId;

    @Column(nullable = false, length = 128)
    private String routingKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false, length = 32)
    private String status; // "PENDING", "PUBLISHED", "FAILED"

    @Column(nullable = false)
    @Builder.Default
    private int retryCount = 0;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant publishedAt;
}
