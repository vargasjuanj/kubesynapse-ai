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
import java.util.List;
import java.util.Map;

/**
 * ======================================================================================
 * PROVEEDOR DE IA: OPENAI COMPATIBLE CLOUD / LOCAL GATEWAY
 * ======================================================================================
 * Implementa {@link AiModelProvider} para cualquier endpoint compatible con /v1/chat/completions:
 * - OpenAI (gpt-4o, gpt-4o-mini)
 * - DeepSeek Cloud API (deepseek-chat, deepseek-coder)
 * - Groq Cloud API (Llama 3.3 70B a ultra-alta velocidad)
 * - Servidores locales: vLLM, LM Studio, Ollama con endpoint /v1
 * ======================================================================================
 */
@Slf4j
@Component
public class OpenAiCompatibleModelProvider implements AiModelProvider {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${kubesynapse.ai.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${kubesynapse.ai.openai.api-key:mock-key}")
    private String apiKey;

    @Value("${kubesynapse.ai.openai.model:gpt-4o-mini}")
    private String modelName;

    @Value("${kubesynapse.ai.timeout-seconds:45}")
    private int timeoutSeconds;

    public OpenAiCompatibleModelProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public String getProviderName() {
        return "openai";
    }

    @Override
    public String generateText(String systemPrompt, String userPrompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            ));
            requestBody.put("temperature", 0.2);

            String jsonPayload = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            log.info("☁️ [OpenAI Provider]: Enviando inferencia a {} [Modelo: {}]...", baseUrl, modelName);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                if (root.has("choices") && root.get("choices").isArray() && !root.get("choices").isEmpty()) {
                    String content = root.get("choices").get(0).get("message").get("content").asText().trim();
                    log.info("✅ [OpenAI Provider]: Inferencia generada exitosamente ({} chars).", content.length());
                    return content;
                }
            } else {
                log.warn("OpenAI endpoint respondió con código HTTP: {}", response.statusCode());
            }
        } catch (Exception ex) {
            log.warn("⚠️ [OpenAI Provider]: Falla o timeout en inferencia: {}", ex.getMessage());
        }
        return null;
    }
}
