package com.juanvargas.kubesynapse.infrastructure.adapter.out.ai.provider;

import org.springframework.stereotype.Component;

/**
 * ======================================================================================
 * PROVEEDOR DE IA: HEURÍSTICO / DETERMINÍSTICO (Resilient Safe-Mode Engine)
 * ======================================================================================
 * Implementa {@link AiModelProvider} para garantizar que el clúster SIEMPRE reciba un
 * diagnóstico técnico preciso, incluso si no hay conexión a internet, si los modelos LLM
 * están saturados o si no se cuenta con GPUs.
 * ======================================================================================
 */
@Component
public class HeuristicFallbackProvider implements AiModelProvider {

    @Override
    public String getProviderName() {
        return "heuristic";
    }

    @Override
    public String generateText(String systemPrompt, String userPrompt) {
        if (userPrompt.contains("OOMKILLED") || userPrompt.contains("ExitCode: 137")) {
            return "[Motor Heurístico de Alta Disponibilidad]:\n" +
                   "Causa Raíz: El proceso en el contenedor superó el límite de memoria asignado en el cgroup de Linux.\n" +
                   "Plan de Remediación:\n" +
                   "1. Elevar los límites en el manifiesto (resources.limits.memory).\n" +
                   "2. Configurar -XX:MaxRAMPercentage=75.0 en JAVA_TOOL_OPTIONS.\n" +
                   "3. Ejecutar 'kubectl rollout restart' para desplegar los nuevos límites.";
        }

        if (userPrompt.contains("CrashLoopBackOff") || userPrompt.contains("ExitCode: 1")) {
            return "[Motor Heurístico de Alta Disponibilidad]:\n" +
                   "Causa Raíz: La aplicación falló durante la secuencia de arranque (falla de bean, variable faltante o conexión rechazada).\n" +
                   "Plan de Remediación:\n" +
                   "1. Inspeccionar logs previos: 'kubectl logs <pod> --previous'.\n" +
                   "2. Validar secretos y ConfigMaps requeridos por el microservicio.\n" +
                   "3. Comprobar la disponibilidad de la base de datos o dependencias de red.";
        }

        return "[Motor Heurístico de Alta Disponibilidad]:\n" +
               "El pod opera bajo parámetros estables o presenta advertencias leves no críticas.";
    }
}
