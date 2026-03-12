# Instrucciones de Arquitectura y Vibe Coding: Proyecto Caosmos

Este documento sirve como la "fuente de verdad" para la generación de código, refactorización y expansión del servidor de simulación **Caosmos**.

## 1. Identidad del Proyecto
Caosmos es un simulador "Headless" autoritativo de un mundo persistente, donde miles de agentes autónomos (Citizens y Directors) coexisten en un ecosistema orgánico.

## 2. Stack Tecnológico Mandatorio
* **Lenguaje:** Java 25+ (Uso intensivo de Records, Pattern Matching y Virtual Threads).
* **Framework:** Spring Boot 4.0.
* **IA:** Spring AI 2.0 (Modelos de razonamiento para Directors, Inferencia masiva para Citizens).
* **Arquitectura:** Híbrida (Vertical Slicing para módulos raíz + Clean Architecture dentro de cada slice).
* **Persistencia:** PostgreSQL + Snapshotting asíncrono.
* **Modularidad:** Spring Modulith para asegurar fronteras estricta.

## 3. Principios de Codificación (Vibe Rules)

### A. Concurrencia (The Loom Mandate)
* **Prohibido:** El uso de hilos pesados tradicionales o pools de hilos limitados para lógica de agentes.
* **Obligatorio:** Cada entidad (`Citizen` o `Director`) debe operar en su propio **Virtual Thread**.
* **Implementación:** Usar `Executors.newVirtualThreadPerTaskExecutor()` para el ciclo de vida de los agentes.

### B. Estructura de Paquetes (Vertical Slices)
El código debe organizarse en módulos raíz independientes:
1.  `caosmos.common`: Tipos base (Moneda, Ticks, Coordenadas), Kernel compartido.
2.  `caosmos.citizens`: Cerebro micro, necesidades y toma de decisiones.
3.  `caosmos.directors`: Supervisión macro, clima, equilibrio sistémico.
4.  `caosmos.economy`: Mercado, flujos monetarios y fijación de precios.
5.  `caosmos.logistics`: Transporte, inventario físico y movimiento.

### C. Sistema de Manifiestos (Markdown + YAML)
Los agentes se configuran mediante archivos `.md` externos:
* **Frontmatter (YAML):** Datos estadísticos (Jackson YAML parser).
* **Cuerpo (Markdown):** Prompt de personalidad para Spring AI.
* **Estrategia de Carga:** Overlay System. Cargar de `/config/caosmos/manifests` (externo) con fallback en `src/main/resources/manifests`.
* **Hot-Reload:** Implementar `WatchService` en un hilo virtual para detectar cambios en los `.md` sin reiniciar el servidor.

## 4. Patrones de Diseño Internos
Dentro de cada Slice, seguir estas capas:
1.  **Domain:** Entidades puras y reglas de negocio inmutables.
2.  **Application:** Casos de uso (servicios que orquestan la lógica).
3.  **Infrastructure:** Adaptadores (Repositories de JPA, Clientes de Spring AI).

## 5. Integración de IA
* **Function Calling:** Las acciones físicas en el mundo (ej: `minar()`, `comprar()`) deben exponerse como herramientas para que el LLM las invoque tras su deliberación.
* **Contexto:** Los agentes deben recibir solo la información local relevante (su inventario, vecinos cercanos y precios locales) para mantener la eficiencia del prompt.

## 6. Prompt de Inicio para el Modelo de IA
> "Actúa como experto en Java 25 y Spring Boot 4. Vamos a construir/modificar el Proyecto Caosmos. Sigue estrictamente la arquitectura de Vertical Slicing. Prioriza hilos virtuales para la concurrencia y asegúrate de que toda la configuración de los agentes sea desacoplada mediante el sistema de manifiestos Markdown. No generes código que viole las fronteras de Modulith."