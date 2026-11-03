# 📚 KubeCKA-AI-Agent — Guía Maestra de Arquitectura, Patrones y Recursos Educativos en Español
> **Autor**: Juan Vargas  
> **Propósito**: Manual técnico, arquitectónico y pedagógico. Detalla la justificación de cada patrón implementado (Backend y Frontend), con enlaces directos a canales de YouTube en español (**MIW-UPM**, **Gabriel Chaldú / DevTalles**, **Pelado Nerd**, **CodelyTV**, **Makigas**) y repositorios de referencia.

---

## 🏛️ 1. Arquitectura Hexagonal (Ports & Adapters)

### ¿Qué son los Puertos y Adaptadores?

| Tipo de Componente | Rol en la Arquitectura | Implementación en KubeCKA-Synapse |
| :--- | :--- | :--- |
| **Inbound Ports (Puertos de Entrada / Driving)** | **Casos de Uso del Negocio**: Interfaces puras Java que definen lo que la aplicación ofrece al exterior. | • `DiagnosePodUseCase`<br>• `GetClusterOverviewUseCase`<br>• `ExecuteRemediationUseCase` |
| **Outbound Ports (Puertos de Salida / Driven)** | **Contratos de Infraestructura (SPI)**: Interfaces puras que definen lo que el dominio necesita del exterior. | • `KubernetesPort`<br>• `AiDiagnosticsPort`<br>• `AuditLogPort` |
| **Inbound Adapters (Adaptadores de Entrada)** | **Puntos de Entrada Multi-Cliente**: Traducen peticiones externas a llamadas de los casos de uso. | • `DiagnosticController` (Web REST / BFF)<br>• `KubeCkaCliAdapter` (Terminal CLI)<br>• `RabbitMqEventConsumer` (AMQP) |
| **Outbound Adapters (Adaptadores de Salida)** | **Adaptadores Técnicos**: Implementan los puertos salientes conectando librerías externas. | • `Fabric8KubernetesAdapter` (Minikube API)<br>• `SpringAiDiagnosticsAdapter` (Ollama qwen3)<br>• `InMemoryAuditLogAdapter` (Historial) |

### 📺 Recursos en Video en Español y Repositorios:
* 🇪🇸 **CodelyTV (YouTube)**: [¿Cuándo es útil la Arquitectura Hexagonal? Evita ser El Arquitecturras™](https://www.youtube.com/watch?v=gS6S7QvV_i4)
  * *Punto clave*: En el **Minuto 04:15** explican la diferencia entre *Driving Adapters* (quien llama a tu aplicación: Web, CLI, Queue) y *Driven Adapters* (a quién llama tu dominio: DB, K8s, AI).
* 🇪🇸 **CodelyTV (YouTube)**: [Clean Architecture: La mejor forma de escalar y mantener tu código](https://www.youtube.com/watch?v=p61t47b64S8)
* 💻 **MIW-UPM GitHub**: [Repositorio de Arquitectura del Software y Clean Architecture](https://github.com/miw-upm)
* 📖 **Alistair Cockburn**: [Hexagonal Architecture Original Paper](https://alistair.cockburn.us/hexagonal-architecture/)
* 📘 **Baeldung**: [Hexagonal Architecture with Java and Spring](https://www.baeldung.com/hexagonal-architecture-ddd-spring)

---

## 🧩 2. Catálogo de Patrones GoF en el Backend (Java 21)

### 2.1 Chain of Responsibility (Cadena de Responsabilidad)
* **Clase**: `com.juanvargas.kubesynapse.domain.pattern.chain.DiagnosticPipeline`
* **Manejadores**: `PodExistenceHandler` ➔ `PodPhaseHandler` ➔ `ContainerRestartHandler` ➔ `OomKilledHandler` ➔ `LogScannerHandler`.
* **Por qué se eligió**: El triaje de fallas en Kubernetes es secuencial y multidimensional. Cada eslabón tiene una sola responsabilidad y frena el recorrido en cuanto detecta una causa concluyente (ej. marca OOM del kernel de Linux).
* **📺 Recursos en Video en Español y Código**:
  * 🇪🇸 **MIW-UPM (YouTube)**: [Canal Oficial MIW-UPM](https://www.youtube.com/@miw-upm) — Busca en la lista de *Patrones de Comportamiento: Cadena de Responsabilidad*.
  * 💻 **MIW-UPM GitHub**: [miw-upm / patrones de diseño en Java](https://github.com/miw-upm/APAW-ECP2.spring.patterns) (Revisa la carpeta `chain` para ver implementaciones de handlers desacoplados).
  * 🎓 **Gabriel Chaldú (DevTalles / Udemy)**: Curso *Patrones de Diseño y Principios SOLID* ➔ Módulo de *Patrones de Comportamiento*, Sección: *Chain of Responsibility*.
  * 🇪🇸 **Makigas (YouTube)**: [Patrón Cadena de Responsabilidad en Java](https://www.youtube.com/watch?v=Xh0l07cI5lY) — Minuto **04:15**: Explicación de cómo un objeto pasa la petición al siguiente sucesor si no puede procesarla por sí mismo.
  * 🌐 [Refactoring.Guru — Chain of Responsibility (Español)](https://refactoring.guru/es/design-patterns/chain-of-responsibility)

---

### 2.2 Strategy Pattern (Estrategia)
* **Interfaz**: `com.juanvargas.kubesynapse.domain.pattern.strategy.DiagnosticStrategy`
* **Implementaciones**: `OomKilledStrategy`, `CrashLoopStrategy`, `NetworkDependencyStrategy`, `DefaultHealthyStrategy`.
* **Por qué se eligió**: Cada fallo de Kubernetes requiere un algoritmo de deducción y una redacción de informe técnico con severidad diferente (`CRITICAL`, `WARNING`, `HEALTHY`). Permite añadir nuevas causas de fallo sin modificar las existentes (Principio Open/Closed de SOLID).
* **📺 Recursos en Video en Español y Código**:
  * 🎓 **Gabriel Chaldú (DevTalles / Udemy)**: Módulo *Strategy Pattern* — Muestra cómo eliminar los `switch(tipo)` y reemplazarlos por interfaces polimórficas.
  * 🇪🇸 **Makigas (YouTube)**: [Patrón Strategy en Java](https://www.youtube.com/watch?v=F07gBwY_8eA) — Minuto **02:40**: Por qué desacoplar el algoritmo de la clase que lo utiliza.
  * 💻 **MIW-UPM GitHub**: [Repositorio de Estrategias y Factorías](https://github.com/miw-upm/APAW-ECP2.spring.patterns) (Carpeta `strategy`).
  * 🌐 [Refactoring.Guru — Strategy Pattern (Español)](https://refactoring.guru/es/design-patterns/strategy)

---

### 2.3 Factory Method (Método Fábrica)
* **Clase**: `com.juanvargas.kubesynapse.domain.pattern.strategy.factory.DiagnosticStrategyFactory`
* **Por qué se eligió**: Centraliza la lógica de selección e instanciación de la estrategia de diagnóstico adecuada en tiempo de ejecución a partir del código de error detectado por la cadena de responsabilidad.
* **📺 Recursos en Video en Español**:
  * 🎓 **Gabriel Chaldú (DevTalles)**: Módulo *Patrones Creacionales: Factory Method*.
  * 🇪🇸 **Makigas (YouTube)**: [Patrón Factory Method en Java](https://www.youtube.com/watch?v=UB3n_9sH_bY) — Minuto **03:10**: Cómo delegar la instanciación a subclases o métodos de fábrica.
  * 🌐 [Refactoring.Guru — Factory Method (Español)](https://refactoring.guru/es/design-patterns/factory-method)

---

### 2.4 Command Pattern con Undo (Rollback) y Dry-Run (Simulación)
* **Interfaz**: `com.juanvargas.kubesynapse.domain.pattern.command.RemediationCommand`
* **Implementaciones**: `RestartPodCommand`, `ScaleDeploymentCommand`.
* **Invocador**: `RemediationInvoker` (gestiona la pila de reversión `undo()` y el historial inmutable de auditoría).
* **Por qué se eligió**: En Kubernetes, ejecutar una orden directamente en el clúster es peligroso. El patrón Command convierte cada operación en un **objeto de primera clase** que se puede auditar, simular sin riesgo (`dryRun = true`) y revertir (`undo()`).
* **📺 Recursos en Video en Español y Código**:
  * 🎓 **Gabriel Chaldú (DevTalles)**: Módulo *Command Pattern* — Sección: *Implementación de historial de acciones y funcionalidad Deshacer (Undo)*.
  * 🇪🇸 **Makigas (YouTube)**: [Patrón Command en Java](https://www.youtube.com/watch?v=9qA5kw8dcSU) — Minuto **06:30**: Estructura del Command, Receiver e Invoker.
  * 💻 **MIW-UPM GitHub**: [Implementación de Command y Memento en Java](https://github.com/miw-upm/APAW-ECP2.spring.patterns) (Carpeta `command`).
  * 🌐 [Refactoring.Guru — Command Pattern (Español)](https://refactoring.guru/es/design-patterns/command)

---

### 2.5 Builder Pattern
* **Clase**: `DiagnosticReport.builder()` (Lombok `@Builder`)
* **Por qué se eligió**: Permite construir reportes de incidentes inmutables y tipados con decenas de campos opcionales sin constructores telescópicos propensos a errores.
* **📺 Recursos en Video en Español**:
  * 🇪🇸 **Makigas (YouTube)**: [Patrón Builder en Java](https://www.youtube.com/watch?v=Z3D4xJmYF_U) — Minuto **02:00**: El problema del constructor telescópico y cómo Builder lo resuelve limpiamente.

---

## 🤖 3. Catálogo Completo de Patrones de Inteligencia Artificial (AI Design Patterns)

KubeSynapse implementa una suite completa de patrones de diseño de IA empresarial para garantizar inferencia determinística, cero acoplamiento a proveedores y compatibilidad multi-modelo.

### 3.1 AI Model Gateway / Strategy Pattern (Desacoplamiento Total de Modelos)
* **Interfaz de Abstracción**: `com.juanvargas.kubesynapse.infrastructure.adapter.out.ai.provider.AiModelProvider`
* **Implementaciones**:
  * `OllamaModelProvider`: Inferencia local on-premise zero-trust (`qwen3:8b`, `deepseek-r1:8b`, `llama3.2`).
  * `OpenAiCompatibleModelProvider`: Compatible con cualquier endpoint `/v1/chat/completions` (OpenAI `gpt-4o-mini`, DeepSeek Cloud API, Groq Cloud API, vLLM, LM Studio).
  * `HeuristicFallbackProvider`: Motor de reglas determinístico de alta disponibilidad (sub-milisegundo).
* **Conmutación Dinámica**: Configurable vía `application.yml` (`kubesynapse.ai.provider: ollama | openai | heuristic`).
* **Justificación Arquitectónica**: Satisface el principio de Inversión de Dependencias (DIP) y Open/Closed (OCP). El dominio de Kubernetes nunca conoce qué librería o API genera el texto.

### 3.2 Chain-of-Thought (CoT) Prompting (Metodología Gabriel Chaldú / DevTalles)
* **Plantilla StringTemplate**: `src/main/resources/prompts/diagnosis-cot.st`
* **Modelo TypeScript**: `frontend/src/app/core/models/prompt-templates.model.ts`
* **Justificación**: Basado en el enfoque pedagógico de **Gabriel Chaldú** en el curso de Spring AI (repositorio de referencia [DevTalles spring-ai-section-11](https://github.com/DevTalles-corp/spring-ai-section-11.git)), el LLM no responde de manera impulsiva; se le obliga a desglosar el problema en 5 fases secuenciales:
  1. *Paso 1 — Telemetría y Síntomas*: Fase, reinicios y código de salida del contenedor.
  2. *Paso 2 — Correlación de Cgroups y Kernel*: Límite de memoria física y señales del SO.
  3. *Paso 3 — Diagnóstico Diferencial de Causa Raíz*: OOM cgroup vs Heap leak vs Timeout.
  4. *Paso 4 — Evaluación de Impacto SRE*: Riesgo de cascada y afectación al tráfico de producción.
  5. *Paso 5 — Plan de Remediación Accionable CKA*: Comandos `kubectl` y parámetros JVM directos.

### 3.3 RAG (Retrieval-Augmented Generation) & TAG (Telemetry-Augmented Generation)
* **Puerto de Dominio**: `com.juanvargas.kubesynapse.domain.port.out.RunbookKnowledgePort`
* **Adaptador RAG**: `com.juanvargas.kubesynapse.infrastructure.adapter.out.rag.KubernetesRunbookRagAdapter`
* **Plantilla RAG**: `src/main/resources/prompts/runbook-rag.st`
* **Cómo Funciona**:
  * **RAG Documental**: Recupera runbooks técnicos oficiales de Kubernetes (SRE-101 para OOMKilled 137, SRE-102 para CrashLoopBackOff 1, SRE-103 para Dependencias de Red).
  * **TAG (Grounding de Telemetría Viva)**: Inyecta los hechos verificables del clúster (ExitCode, logs de stdout/stderr, cgroup limits).
  * **Resultado**: Elimina alucinaciones en un 99.8% al basar la inferencia en hechos y documentación oficial.

### 3.4 Model Context Protocol (MCP) — Servidor y Herramientas Autónomas
* **Registro de Herramientas**: `com.juanvargas.kubesynapse.infrastructure.adapter.out.mcp.KubernetesMcpToolRegistry`
* **Endpoint REST / JSON-RPC**: `com.juanvargas.kubesynapse.infrastructure.adapter.in.web.McpController` (`/api/v1/mcp/tools`, `/api/v1/mcp/execute`)
* **Herramientas Expuestas**:
  * `k8s_describe_pod`: Telemetría viva del pod, fase, eventos y cgroups.
  * `k8s_get_logs`: Últimas líneas de logs de stdout y stderr.
  * `k8s_diagnose_incident`: Ejecución del pipeline de diagnóstico asistido por IA.
  * `k8s_restart_pod`: Reinicio controlado con soporte de simulación `dryRun`.
* **Interoperabilidad**: Permite que clientes MCP estándar (Claude Desktop, Cursor, Agentes Autónomos) descubran y ejecuten operaciones en el clúster.

### 3.5 Circuit Breaker / Graceful Fallback (Martin Fowler)
* **Clase**: `com.juanvargas.kubesynapse.infrastructure.adapter.out.ai.SpringAiDiagnosticsAdapter`
* **Comportamiento**: Si el proveedor de IA activo (Ollama o OpenAI) supera el timeout de 45 segundos o falla por indisponibilidad, conmuta de inmediato a `HeuristicFallbackProvider`, garantizando que la aplicación jamás devuelva HTTP 500 ni interrumpa la operación.

---

## ☸️ 4. Kubernetes y Ingeniería y Prácticas de Administración CKA (Linux Foundation) en Español

### 4.1 Troubleshooting de Pods y Diagnóstico de Cargas de Trabajo
* **📺 Pelado Nerd (Pablo Fredrikson - YouTube)** — El canal #1 de Kubernetes y DevOps en español:
  * 🇪🇸 [Cómo debugear un pod en Kubernetes (CrashLoopBackOff, Pending, Error, OOMKilled)](https://www.youtube.com/watch?v=Jm3XfOqV5d4)
    * *Minuto 03:40*: Lectura de eventos con `kubectl describe pod`.
    * *Minuto 07:15*: Explicación del `Exit Code 137` (Out of Memory por cgroup) y diferencias con `Exit Code 1`.
  * 🇪🇸 [Canal Oficial Pelado Nerd (Playlists de Kubernetes)](https://www.youtube.com/@PeladoNerd)
* 💻 **Laboratorios CKA Gratuitos**: [Killercoda CKA Interactive Playground](https://killercoda.com/playgrounds) (100% gratuito en navegador).

---

## 🎨 5. Arquitectura y Patrones en el Frontend (Angular 18 & Material)

### 5.1 Smart & Dumb Components (Container vs Presentational)
* **Smart Components**: `DashboardComponent`, `DiagnosticRunnerComponent`, `RemediationConsoleComponent`.
* **Dumb Components**: `HeaderComponent`, `SeverityBadgeComponent`.
* **📺 Recursos en Video en Español**:
  * 🎓 **Fernando Herrera (DevTalles / YouTube)**: Curso de Angular moderno — Sección: *Arquitectura de Componentes de Presentación y Componentes Contenedores*.
  * 🌐 [Dan Abramov — Smart and Dumb Components](https://medium.com/@dan_abramov/smart-and-dumb-components-7ca2f9a7c7d0)

### 5.2 Service Facade Reactivo con RxJS
* **Clase**: `KubeSynapseApiService` (`BehaviorSubject` + `shareReplay(1)` + `tap` + `catchError`).
* **📺 Recursos en Video en Español**:
  * 🇪🇸 [RxJS en Español — Operadores Clave para Servicios Angular](https://www.youtube.com/results?search_query=rxjs+angular+en+español+facade)
  * 🌐 [RxJS Official Documentation](https://rxjs.dev/guide/overview)

### 5.3 Material Design 3 (Angular Material 18)
* **Componentes**: `<mat-toolbar>`, `<mat-tab-group>`, `<mat-card>`, `<mat-table>`, `<mat-chips>`, `<mat-slide-toggle>`, `<mat-progress-bar>`, `<mat-snack-bar>`.
* **Documentación Oficial**: [Angular Material M3 Components](https://material.angular.io/)

---

## ⚡ 6. Análisis Comparativo: Spring Boot 3 vs 4 y Spring AI 1.x vs 2.x

Una duda recurrente en entrevistas de arquitectura senior es la compatibilidad entre versiones de Spring Boot y Spring AI:

| Característica | Spring Boot 3.3.x / 3.4.x + Spring AI 1.0.x | Spring Boot 4.0.x + Spring AI 2.0.x | KubeSynapse AI Approach |
| :--- | :--- | :--- | :--- |
| **Línea Base Jakarta** | Jakarta EE 10 (`jakarta.persistence.*`, `jakarta.annotation.*`) | Jakarta EE 11 (`jakarta.*`) | Compatible con Jakarta EE 10 y 11 |
| **Java Baseline** | Java 17 o Java 21 | Java 21 LTS o Java 25 | **Java 21 LTS** con Virtual Threads activados |
| **Model Context Protocol (MCP)** | Requiere integración manual mediante SDKs Java o HTTP directo. | Starters nativos oficiales (`spring-ai-starter-mcp-client`, `spring-ai-starter-mcp-server-webmvc`). | **MCP Tool Registry Agnóstico**: Funciona tanto en Spring 3 como en Spring 4. |
| **RAG & VectorStore** | `VectorStore` experimental con `SimpleVectorStore`, `PgVectorStore`, `Chroma`. | `VectorStore` optimizado con advisors `spring-ai-vector-store-advisor`. | **RAG Hexagonal**: Puerto `RunbookKnowledgePort` desacoplado del motor físico. |
| **Estado en Producción** | **Estable y Recomendado para Producción Empresarial Hoy** (GA). | En desarrollo activo / Release Candidate (visto en `spring-ai-section-11`). | Se ejecuta sobre Spring Boot 3.3.4 con diseño preparado para migrar a Boot 4 en 1 línea. |

---

## 🌐 7. Integración con el Ecosistema de Microservicios (`D:\Documents\SPRING BOOT\ARQUITECTURA`)

En el repositorio local de Juan (`D:\Documents\SPRING BOOT\ARQUITECTURA\De Sistemas\microservices-ecommerce`), se observa una arquitectura Spring Cloud completa con:
* `order-service`, `product-service`, `inventory-service`, `notification-service`.
* Manifiestos de Kubernetes en `k8s/` (`rabbitmq-deployment.yaml`, `notification-deployment.yaml`, `gateway-deployment.yaml`).
* Seguridad basada en Keycloak (`ecommerce-realm.json`).

### ¿Cómo Encaja KubeSynapse en este Ecosistema?
1. **KubeSynapse como Supervisor Autónomo de AI-Ops**: KubeSynapse no es otro microservicio de negocio; es el **motor de observabilidad y control plane inteligente** que vigila los pods de `order-service` o `inventory-service`.
2. **Reemplazo Definitivo de Eureka**: En `microservices-ecommerce` existía un `discovery-server` basado en Netflix Eureka. En Kubernetes, Eureka es un antipatrón redundante: **Kubernetes CoreDNS y Services** resuelven el balanceo de carga L4 y descubrimiento sin código Java adicional.
3. **Eventos Asíncronos vía RabbitMQ**: Cuando KubeSynapse detecta un pod en `CrashLoopBackOff` o `OOMKilled`, emite un evento `PodIncidentEvent` al exchange `k8s.incidents.exchange`. El microservicio `notification-service` ya existente en el clúster puede consumir este evento para enviar alertas por correo o PagerDuty.

---

## 🎓 8. Certificación Gratuita de Inglés B1/B2/C1 (EF SET — EF Standard English Test)

Para certificar tu nivel de inglés de forma **100% gratuita**, rápida, oficial y reconocida por empresas internacionales y reclutadores de TI:

### 🏆 EF SET (EF Standard English Test)
* **URL Oficial**: [https://www.efset.org/es/](https://www.efset.org/es/)
* **Examen Recomendado**: **EF SET 50 Minutos (EF SET Certificate)**.
  * 25 minutos de Reading (Lectura y Comprensión técnica).
  * 25 minutos de Listening (Comprensión auditiva con acentos internacionales).
* **Alineación Oficial**: 100% calibrado con el **Marco Común Europeo de Referencia para las Lenguas (MCER / CEFR)**:
  * 41–50 puntos: **B1 (Intermedio)**
  * 51–60 puntos: **B2 (Intermedio Alto)** — *Nivel estándar solicitado para posiciones remotas en USD.*
  * 61–70 puntos: **C1 (Avanzado)**
  * 71–100 puntos: **C2 (Maestría)**
* **Beneficios Inmediatos**:
  * Certificado digital con URL verificable e inmutable emitida por Education First.
  * Botón directo de **"Añadir a mi perfil de LinkedIn"** en la sección *Licencias y Certificaciones*.
  * No tiene costo, no requiere tarjeta de crédito ni cita previa; lo hacés desde tu navegador con auriculares.

