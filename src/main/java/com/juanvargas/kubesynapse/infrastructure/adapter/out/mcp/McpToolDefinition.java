package com.juanvargas.kubesynapse.infrastructure.adapter.out.mcp;

import java.util.List;
import java.util.Map;

/**
 * ======================================================================================
 * MODEL CONTEXT PROTOCOL (MCP): DEFINICIÓN DE HERRAMIENTA (Tool Definition)
 * ======================================================================================
 * Estructura de metadatos estandarizada conforme a la especificación de MCP
 * (Model Context Protocol) promovida por Anthropic y la comunidad open source.
 * Permite que clientes de IA (Claude Desktop, Cursor, Agentes Autónomos) descubran
 * e invoquen dinámicamente capacidades del clúster de Kubernetes.
 *
 * Referencias educativas:
 * - Curso DevTalles / Gabriel Chaldú: MCP Client & Server en Spring AI 2.0.
 * - Model Context Protocol Specification: https://modelcontextprotocol.io
 * ======================================================================================
 */
public record McpToolDefinition(
        String name,
        String description,
        Map<String, Object> inputSchema,
        List<String> requiredParameters
) {
}
