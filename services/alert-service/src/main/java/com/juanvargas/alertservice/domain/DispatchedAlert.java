package com.juanvargas.alertservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Modelo de alerta procesada y despachada por el microservicio de alertas SRE.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DispatchedAlert {
    private String alertId;
    private String incidentId;
    private String targetPod;
    private String severity;
    private String summary;
    private String channel; // "SLACK", "PAGERDUTY", "SRE_AUDIT_LOG"
    private boolean deduplicated;
    private Instant dispatchedAt;
}
