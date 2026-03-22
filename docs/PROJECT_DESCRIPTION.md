# Documento de Referencia Maestra para Asistentes de IA (Vibe Coding)

> [!IMPORTANT]
> **INSTRUCCIÓN PARA LA IA LECTORA**: Este documento define las reglas de negocio, la arquitectura y la filosofía del *
*"Proyecto Caosmos"**. Usa esta información como verdad absoluta para cualquier generación de código, diseño de bases de
> datos o lógica de sistemas solicitada por el usuario. No asumas patrones de RPG tradicionales; enfócate en la
> simulación
> sistémica emergente.

---

## 1. Visión Global y Filosofía

Caosmos no es un RPG tradicional, es una **Simulación de Vida Persistente** (Mundo Vivo) y una economía de agentes. El
mundo es un ecosistema autónomo basado en matemáticas, necesidades lógicas y físicas emergentes.

* **Rechazo del "Decorado"**: No hay NPCs estáticos esperando al jugador. Si un árbol cae, el servidor calcula la madera
  y un leñador la recoge, esté el jugador o no.
* **Fase 1 (Baseline Orgánico)**: Construcción del ecosistema base. Economía, logística y sociedad funcionando como un
  reloj mediante agentes autónomos.
* **Fase 2 (La Anomalía)**: Entrada del Jugador. El jugador no es el "héroe predestinado", sino una **Fuerza Disruptiva
  **. Debe insertarse en la cadena de suministro, manipular el mercado o alterar la logística. El mundo reacciona de
  forma sistémica, no guiada por scripts.

## 2. Stack Tecnológico y Arquitectura Base

El sistema opera bajo un modelo **Client-Server Headless**, donde el motor lógico es independiente de la representación
gráfica.

* **Lenguaje**: Java 25.
* **Concurrencia**: **Virtual Threads (Project Loom)** estrictamente. Cada agente tiene su propio hilo virtual para
  procesos cognitivos sin bloquear la CPU física.
* **Framework Core**: Spring Boot 4 + Spring Modulith.
* **Inteligencia Artificial**: **Spring AI 2.0** (Modelos masivos para micro-agentes, Reasoning models para directores,
  Function Calling para acciones físicas).
* **Arquitectura**: Clean Architecture + Vertical Slicing.

## 3. Sistemas Core (Motor de Simulación)

### 3.1. Percepción Semántica y Espacio (`world` / `logistics`)

El LLM de un NPC no puede hacer trigonometría. El servidor debe traducir el mundo 3D a conceptos semánticos.

* **Geometría**: Ejes de Unity (X, Z para plano; Y para elevación). 1 Unidad = 1 Metro.
* **Spatial Hash Grid**: El mundo se divide en *Chunks* para optimizar la búsqueda de entidades cercanas (evitando
  iteración O(N^2)).
* **Buffer Sensorial**: El `PerceptionProvider` convierte las distancias matemáticas en un JSON relativo que se inyecta
  en el prompt del agente (ej. azimut a "Norte", distancia a "Cerca", elevación a "Por encima de ti").

### 3.2. Configuración de Agentes (Manifiestos)

Los NPCs se definen mediante archivos híbridos Markdown (`.md`) almacenados en una carpeta externa para permitir *
*Hot-Reload** sin recompilar el `.jar`.

* **Frontmatter (YAML)**: Parseado con Jackson a Java Records. Contiene datos físicos del NPC.
* **Body (Markdown)**: Define la personalidad y directrices. Se inyecta directamente como `SystemPromptTemplate` en
  Spring AI.

### 3.3. Acciones y Física Semántica (Motor de Consecuencias)

Los agentes no tienen verbos pre-programados rígidos. Funciona por **Affordances (Tags Semánticos)**. Un NPC ve un ID de
objeto y sus tags (ej. `[frágil]`, `[combustible]`).

* **ActionIntent**: El LLM devuelve su intención (`verb`, `targetId`, `toolId`).
* **ActionDispatcher**: Filtra la acción en Java (Sanity Check: rango, raycast de visión).
* **Arbitraje IA**: Si la acción interactúa de forma novedosa (ej. "Quemar" con "Magia" sobre "Agua"), se envía al
  `DirectorArbitrator` (un LLM juez) junto con el estado del entorno.
* **WisdomCache**: Para no saturar la API de IA, el veredicto del Arbitrador se hashea (SHA-256 de la
  Acción+Objetos+Entorno Normalizado) y se guarda. La próxima vez que alguien intente lo mismo, se resuelve
  determinísticamente en milisegundos.

### 3.4. Navegación y Movimiento Continuo

El movimiento no es teletransporte. Es físico y lleva tiempo real.

* **Estado `IN_TRANSIT`**: Un agente en movimiento interpola matemáticamente (Lerp) su posición en cada Tick basándose
  en su `BaseSpeed` y el `DeltaTime`.
* **Interrupciones**: Si un agente en tránsito detecta una amenaza, pasa a `EVALUATING_THREAT`, detiene el cálculo
  matemático y consulta de emergencia al LLM ("Huir" o "Atacar").

## 4. La Jerarquía de Agentes

### 4.1. Micro-Agentes (Citizens)

Son los habitantes del mundo. Orquestados por un `CitizenPulse` (su ciclo de consciencia).

* **Estados de Máquina**: `IDLE`, `IN_TRANSIT`, `THINKING` (esperando al LLM asíncronamente), `BUSY`.
* Solo consultan al LLM cuando es estrictamente necesario (`IDLE` o interrupciones severas).

### 4.2. Macro-Agentes (El Cónclave de los Dioses)

LLMs de escala temporal amplia (semanas/meses) que buscan la homeostasis del mundo, no el bienestar individual.

* **Dios Primordial (El Ecosistema)**: Dicta edictos supremos. Protege la "Anomalía" (El Jugador) asegurando que no se
  quede bloqueado irreversiblemente.
* **Dios de Recursos (La Vida)**: Genera abundancia, pero castiga la sobreexplotación.
* **Dios de Riesgo (El Conflicto/El Depredador)**: Introduce bandidos, lobos o amenazas en rutas comerciales para evitar
  el estancamiento utópico.
* **Dios de Mercado (El Economista)**: Induce recesiones o inflación según el flujo del oro global.

## 5. Glosario Técnico para la IA

| Término                     | Definición                                                                                                                                     |
|:----------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------|
| **Tick**                    | El pulso temporal del servidor.                                                                                                                |
| **Slice**                   | Módulo aislado de la Clean Architecture según Spring Modulith.                                                                                 |
| **Affordance / Tag**        | Etiqueta descriptiva (ej. `[pesado]`) que permite al LLM inferir usos de objetos sin código específico.                                        |
| **WisdomCache**             | El sistema de aprendizaje del servidor que convierte fallos de caché (consultas LLM costosas) en aciertos de caché (determinismo instantáneo). |
| **Vector Social / Hobbies** | Metadatos de NPCs que el jugador puede explotar para ingeniería social.                                                                        |

---

**Fin del Contexto.** Actúa basándote en estos principios para las siguientes instrucciones.
