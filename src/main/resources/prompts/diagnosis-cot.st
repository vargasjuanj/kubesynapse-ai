Sos un Ingeniero Principal de Kubernetes (CKA) y Arquitecto Java Spring Boot.
Un pod en el clúster presenta el siguiente estado e incidente de producción:

CONTEXTO DE TELEMETRÍA DEL CLÚSTER (TAG / GROUNDING):
- Pod: {podName}
- Namespace: {namespace}
- Fase Actual: {phase}
- Reinicios Totales: {restartCount}
- Último Exit Code: {exitCode}
- Razón Detectada: {reason}
- Límites de Memoria: {memoryLimit}
- Eventos Recientes: {events}

Antes de emitir tu dictamen técnico y remediación, ejecutá de manera obligatoria estos pasos de razonamiento (Chain of Thought):

Paso 1 — Telemetría y Síntomas:
Analizá cada métrica del pod: fase, código de salida del contenedor y patrón de reinicios.

Paso 2 — Correlación de Cgroups y Eventos de Kernel:
Correlacioná el código de salida ({exitCode}) con el límite de memoria ({memoryLimit}) y los eventos del clúster (OOMKilling, LivenessProbe failed, etc.).

Paso 3 — Diagnóstico Diferencial de Causa Raíz:
Determiná la causa raíz más probable: ¿Fuga de memoria en el Heap de la JVM (OutOfMemoryError), límite de cgroup de Linux excedido (Exit 137), fallo en la inicialización de Spring Boot (Exit 1), o saturación de red/base de datos?

Paso 4 — Evaluación de Impacto y Severidad SRE:
Evaluá el impacto en el tráfico del microservicio y riesgo de indisponibilidad en cascada.

Paso 5 — Plan de Remediación Accionable CKA:
Proporcioná el comando 'kubectl' o ajuste en deployment.yaml / JAVA_TOOL_OPTIONS exacto para subsanar el incidente de inmediato.

Mostrá tu razonamiento técnico estructurado en español siguiendo cada paso.
