package com.juanvargas.alertservice.service;

import com.juanvargas.alertservice.domain.DispatchedAlert;
import com.juanvargas.alertservice.domain.PodIncidentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ======================================================================================
 * SERVICIO DE DEDUPLICACIÓN DE ALERTAS SRE (Flapping Suppression Pattern)
 * ======================================================================================
 * Si un contenedor entra en CrashLoopBackOff y reinicia 20 veces en 2 minutos,
 * este servicio evita el spam al equipo de guardia:
 * 1. Comprueba si ya se emitió una alerta reciente para el mismo pod dentro de la ventana de tiempo.
 * 2. Si es un duplicado flapping, lo registra pero marca como deduplicado.
 * 3. Si es un incidente nuevo o expiró la ventana, emite la alerta hacia el canal SRE.
 * ======================================================================================
 */
@Slf4j
@Service
public class AlertDeduplicationService {

    @Value("${alertservice.deduplication-window-seconds:60}")
    private long windowSeconds;

    // Cache de último evento por pod: key = "namespace/podName", value = epochSecond
    private final Map<String, Long> lastSeenPodMap = new ConcurrentHashMap<>();

    // Buffer de las últimas 50 alertas despachadas para consulta REST
    private final List<DispatchedAlert> recentAlerts = new CopyOnWriteArrayList<>();

    public DispatchedAlert processIncident(PodIncidentEvent event) {
        String podKey = event.namespace() + "/" + event.podName();
        long now = Instant.now().getEpochSecond();
        Long lastSeen = lastSeenPodMap.get(podKey);

        boolean isFlappingDuplicate = (lastSeen != null) && (now - lastSeen < windowSeconds);
        lastSeenPodMap.put(podKey, now);

        String alertId = "ALT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        DispatchedAlert alert = DispatchedAlert.builder()
                .alertId(alertId)
                .incidentId(event.incidentId())
                .targetPod(podKey)
                .severity(event.severity())
                .summary(String.format("Alerta SRE: Fallo en pod '%s' [%s] - Causa: %s",
                        event.podName(), event.severity(), event.rootCause()))
                .channel(isFlappingDuplicate ? "SUPPRESSED_FLAPPING" : "SLACK_PAGERDUTY_DISPATCH")
                .deduplicated(isFlappingDuplicate)
                .dispatchedAt(Instant.now())
                .build();

        // Mantener hasta 50 alertas recientes
        if (recentAlerts.size() >= 50) {
            recentAlerts.remove(0);
        }
        recentAlerts.add(alert);

        if (isFlappingDuplicate) {
            log.warn("🛡️ [Alert Flapping Suppressed]: Pod {} volvió a fallar en < {}s. Alerta agrupada (ID: {}).",
                    podKey, windowSeconds, alertId);
        } else {
            log.info("🚨 [SRE Alert Dispatched]: ¡Alerta {} enviada a guardia para {}! Severidad: [{}].",
                    alertId, podKey, event.severity());
        }

        return alert;
    }

    public List<DispatchedAlert> getRecentAlerts() {
        return Collections.unmodifiableList(recentAlerts);
    }
}
