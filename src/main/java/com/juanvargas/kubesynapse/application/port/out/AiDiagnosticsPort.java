package com.juanvargas.kubesynapse.application.port.out;

import com.juanvargas.kubesynapse.domain.model.PodStatusInfo;

/**
 * Outbound Port / SPI: Contract for the AI Diagnostic engine.
 * Isolates domain from Spring AI, Ollama, OpenAI or local models.
 */
public interface AiDiagnosticsPort {
    String generateRootCauseAnalysis(PodStatusInfo podInfo, String detectedReason);
    String generateRemediationAdvice(PodStatusInfo podInfo, String detectedReason);
}
