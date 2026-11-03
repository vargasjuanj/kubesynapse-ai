package com.juanvargas.kubesynapse.domain.pattern.command;

import com.juanvargas.kubesynapse.domain.model.RemediationResult;

/**
 * ======================================================================================
 * PATRÓN DE COMPORTAMIENTO: COMMAND PATTERN (con soporte de Undo y Dry-Run)
 * ======================================================================================
 * Propósito:
 * - Encapsula una petición operativa como un objeto independiente (RestartPodCommand,
 *   ScaleDeploymentCommand), permitiendo parametrizar clientes con diferentes peticiones,
 *   encolar o registrar peticiones para auditoría y soportar operaciones que se pueden deshacer.
 *
 * Características clave en KubeSynapse AI:
 * 1. execute(): Ejecuta la operación real en el cluster a través del puerto KubernetesPort.
 * 2. undo(): Revierte la operación (ej. si escalamos de 2 a 5 réplicas y el sistema empeoró,
 *    el undo revierte de 5 a 2 réplicas automáticamente).
 * 3. isDryRun(): Permite simular de forma 100% segura la operación antes de tocar producción.
 *
 * Invocador (Invoker):
 * - RemediationInvoker mantiene una pila Deque<RemediationCommand> con el historial de comandos,
 *   lo que permite implementar un sistema de auditoría corporativa y reversión atómica.
 *
 * Referencias educativas:
 * - Refactoring.Guru (Command Pattern): https://refactoring.guru/es/design-patterns/command
 * - MIW-UPM Command Pattern: https://github.com/miw-upm
 * ======================================================================================
 */
public interface RemediationCommand {
    RemediationResult execute();
    RemediationResult undo();
    String getDescription();
    boolean isDryRun();
}
