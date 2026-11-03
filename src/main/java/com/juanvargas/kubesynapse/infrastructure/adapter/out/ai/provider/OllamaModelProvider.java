package com.juanvargas.kubesynapse.infrastructure.adapter.out.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * ======================================================================================
 * PROVEEDOR DE IA: OLLAMA LOCAL ENGINE (Zero-Trust On-Premise LLM)
 * ======================================================================================
 * Implementa {@link AiModelProvider} para conectarse con la API de Ollama (/api/generate).
 * Soporta cualquier modelo descargado localmente:
 * - 'qwen3:8b' (Reasoning & Tool Calling)
 * - 'llama3.2:3b' / 'llama3:8b'
 * - 'deepseek-r1:8b'
 * - 'mistral:7b'
 * ======================================================================================
 */
@Slf4j
@Component
public class OllamaModelProvider implements AiModelProvider {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${kubesynapse.ai.ollama.base-url:http://localhost:11434}")
    private String baseUrl;

    @Value("${kubesynapse.ai.ollama.model:qwen3:8b}")
    private String modelName;

    @Value("${kubesynapse.ai.timeout-seconds:45}")
    private int timeoutSeconds;

    public OllamaModelProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public String getProviderName() {
        return "ollama";
    }

    @Override
    public String generateText(String systemPrompt, String userPrompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("system", systemPrompt);
            requestBody.put("prompt", userPrompt);
            requestBody.put("stream", false);
            requestBody.put("options", Map.of(
                    "num_predict", 150,
                    "temperature", 0.1,
                    "top_k", 20
            ));

            String jsonPayload = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/generate"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            log.info("🤖 [Ollama Provider]: Enviando inferencia a {} [Modelo: {}]...", baseUrl, modelName);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String generatedText = "";
                if (root.has("response") && !root.get("response").asText().isBlank()) {
                    generatedText = root.get("response").asText().trim();
                } else if (root.has("thinking") && !root.get("thinking").asText().isBlank()) {
                    generatedText = root.get("thinking").asText().trim();
                }

                if (!generatedText.isBlank()) {
                    log.info("✅ [Ollama Provider]: Inferencia generada exitosamente ({} chars).", generatedText.length());
                    return generatedText;
                }
            } else {
                log.warn("Ollama respondió con código HTTP: {}", response.statusCode());
            }
        } catch (Exception ex) {
            log.warn("⚠️ [Ollama Provider]: Falla o timeout en inferencia: {}", ex.getMessage());
        }
        return null;
    }
}
