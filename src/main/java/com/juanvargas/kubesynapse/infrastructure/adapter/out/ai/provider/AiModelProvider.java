package com.juanvargas.kubesynapse.infrastructure.adapter.out.ai.provider;

/**
 * ======================================================================================
 * PATRÓN DE DISEÑO DE IA: AI MODEL PROVIDER STRATEGY (Abstracción de Proveedor de LLM)
 * ======================================================================================
 * Propósito:
 * - Desacopla la lógica de diagnóstico de cualquier proveedor o SDK específico de IA.
 * - Permite conmutar en caliente o por configuración entre:
 *   1. Modelos Locales en Ollama (qwen3:8b, llama3.2, deepseek-r1).
 *   2. APIs en la nube compatibles con OpenAI (OpenAI, DeepSeek, Groq, vLLM, LM Studio).
 *   3. Motores heurísticos determinísticos de alta disponibilidad.
 * - Satisface el principio de Inversión de Dependencias (DIP) y Abierto/Cerrado (OCP).
 *
 * Referencias educativas:
 * - Enterprise AI Design Patterns (Lak Lakshmanan / O'Reilly): Model Gateway Pattern.
 * - Martin Fowler (Strategy Pattern): https://refactoring.guru/es/design-patterns/strategy
 * ======================================================================================
 */
public interface AiModelProvider {

    /**
     * Identificador único del proveedor (ej. 'ollama', 'openai', 'heuristic').
     */
    String getProviderName();

    /**
     * Ejecuta una inferencia textual enviando un System Prompt (rol) y un User Prompt (contexto telemetría).
     *
     * @param systemPrompt Instrucción del sistema definiendo el rol del agente (SRE / CKA / Architect).
     * @param userPrompt   Contexto técnico y hechos recolectados del Pod en Kubernetes.
     * @return Texto generado por el modelo de IA o null si ocurrió una falla.
     */
    String generateText(String systemPrompt, String userPrompt);
}
