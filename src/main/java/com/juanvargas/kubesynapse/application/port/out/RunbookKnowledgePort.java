package com.juanvargas.kubesynapse.application.port.out;

/**
 * ======================================================================================
 * PUERTO DE SALIDA (Outbound Port): RUNBOOK KNOWLEDGE BASE (RAG)
 * ======================================================================================
 * Abstracción de dominio para el patrón RAG (Retrieval-Augmented Generation).
 * Permite que el pipeline de diagnóstico recupere artículos técnicos de runbooks
 * oficiales de Kubernetes (SRE playbooks) sin acoplarse al motor de vector store
 * subyacente (PgVector, Chroma, o In-Memory Store).
 *
 * Referencias educativas:
 * - Curso DevTalles / Gabriel Chaldú: RAG y Vector Stores con Spring AI.
 * - Retrieval-Augmented Generation for Knowledge-Intensive NLP Tasks (Lewis et al.).
 * ======================================================================================
 */
public interface RunbookKnowledgePort {

    /**
     * Recupera el runbook o guía de resolución más relevante para el tipo de incidente detectado.
     *
     * @param reason     Razón de la falla (ej. "OOMKILLED", "CrashLoopBackOff", "NetworkDependency").
     * @param exitCode   Código de salida del proceso del contenedor (ej. 137, 1, 143).
     * @return Contenido del runbook técnico en texto plano para enriquecer el contexto del LLM.
     */
    String findRelevantRunbook(String reason, Integer exitCode);
}
