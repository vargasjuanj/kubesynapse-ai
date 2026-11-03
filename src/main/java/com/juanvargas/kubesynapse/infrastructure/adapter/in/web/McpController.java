package com.juanvargas.kubesynapse.infrastructure.adapter.in.web;

import com.juanvargas.kubesynapse.infrastructure.adapter.out.mcp.KubernetesMcpToolRegistry;
import com.juanvargas.kubesynapse.infrastructure.adapter.out.mcp.McpToolDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ======================================================================================
 * ADAPTADOR PRIMARIO (Driving Inbound Adapter): MCP REST / JSON-RPC ENDPOINT
 * ======================================================================================
 * Expone las capacidades de KubeSynapse conforme a la especificación de
 * Model Context Protocol (MCP).
 *
 * Permite que agentes externos de IA (Claude Desktop, Spring AI MCP Clients, IDEs)
 * descubran herramientas de Kubernetes y las ejecuten de forma segura.
 *
 * Referencias educativas:
 * - Curso DevTalles / Gabriel Chaldú: Spring AI MCP Server y WebMvc endpoints.
 * ======================================================================================
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/mcp")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Model Context Protocol (MCP)", description = "Punto de integración para clientes y agentes MCP")
public class McpController {

    private final KubernetesMcpToolRegistry mcpToolRegistry;

    @GetMapping("/tools")
    @Operation(summary = "Listar herramientas MCP de Kubernetes disponibles")
    public ResponseEntity<List<McpToolDefinition>> getTools() {
        return ResponseEntity.ok(mcpToolRegistry.getAvailableTools());
    }

    @PostMapping("/execute")
    @Operation(summary = "Ejecutar una herramienta MCP de Kubernetes")
    public ResponseEntity<Object> executeTool(@RequestBody McpExecutionRequest request) {
        log.info("📡 [MCP Endpoint]: Recibida petición de ejecución para tool '{}'", request.toolName());
        Object result = mcpToolRegistry.executeTool(request.toolName(), request.arguments() != null ? request.arguments() : Map.of());
        return ResponseEntity.ok(result);
    }

    public record McpExecutionRequest(
            String toolName,
            Map<String, Object> arguments
    ) {}
}
