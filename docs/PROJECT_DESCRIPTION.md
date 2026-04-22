# Master Reference Document for AI Assistants (Vibe Coding)

> [!IMPORTANT]
> **INSTRUCTION FOR READER AI**: This document defines the business rules, architecture, and philosophy of the *
*"Caosmos Project"**. Use this information as absolute truth for any code generation, database design, or system
> logic requested by the user. Do not assume traditional RPG patterns; focus on emergent
> systemic simulation.

---

## 1. Global Vision and Philosophy

Caosmos is not a traditional RPG, it is a **Persistent Life Simulation** (Living World) and an agent economy. The
world is an autonomous ecosystem based on mathematics, logical needs, and emergent physics.

* **Rejection of "Props"**: There are no static NPCs waiting for the player. If a tree falls, the server calculates the wood
  and a woodcutter collects it, whether the player is present or not.
* **Phase 1 (Organic Baseline)**: Construction of the base ecosystem. Economy, logistics, and society functioning like a
  clock through autonomous agents.
* **Phase 2 (The Anomaly)**: Player Entry. The player is not the "predestined hero," but a **Disruptive Force
  **. They must insert themselves into the supply chain, manipulate the market, or alter logistics. The world reacts
  systemically, not guided by scripts.

## 2. Technology Stack and Base Architecture

The system operates under a **Headless Client-Server** model, where the logic engine is independent of the graphical
representation.

* **Language**: Java 25.
* **Concurrency**: **Virtual Threads (Project Loom)** strictly. Each agent has its own virtual thread for
  cognitive processes without blocking the physical CPU.
* **Core Framework**: Spring Boot 4 + Spring Modulith.
* **Artificial Intelligence**: **Spring AI 2.0** (Massive models for micro-agents, Reasoning models for directors,
  Function Calling for physical actions).
* **Architecture**: Clean Architecture + Vertical Slicing.

## 3. Core Systems (Simulation Engine)

### 3.1. Semantic Perception and Space (`world` / `logistics`)

An NPC's LLM cannot do trigonometry. The server must translate the 3D world into semantic concepts.

* **Geometry**: Unity axes (X, Z for plane; Y for elevation). 1 Unit = 1 Meter.
* **Spatial Hash Grid**: The world is divided into *Chunks* to optimize nearby entity search (avoiding
  O(N^2) iteration).
* **Sensory Buffer**: The `PerceptionProvider` converts mathematical distances into a relative JSON that is injected
  into the agent's prompt (e.g., azimuth to "North", distance to "Near", elevation to "Above you").

### 3.2. Agent Configuration (Manifests)

NPCs are defined via hybrid Markdown files (`.md`) stored in an external folder to allow *
*Hot-Reload** without recompiling the `.jar`.

* **Frontmatter (YAML)**: Parsed with Jackson to Java Records. Contains NPC physical data.
* **Body (Markdown)**: Defines personality and guidelines. Injected directly as `SystemPromptTemplate` in
  Spring AI.

### 3.3. Actions and Semantic Physics (Consequence Engine)

Agents have no rigid pre-programmed verbs. It works by **Affordances (Semantic Tags)**. An NPC sees an object ID
and its tags (e.g., `[fragile]`, `[flammable]`).

* **ActionIntent**: The LLM returns its intention (`verb`, `targetId`, `toolId`).
* **ActionDispatcher**: Filters the action in Java (Sanity Check: range, vision raycast).
* **AI Arbitration**: If the action interacts in a novel way (e.g., "Burn" with "Magic" on "Water"), it is sent to
  `DirectorArbitrator` (an LLM judge) along with the environment state.
* **WisdomCache**: To avoid saturating the AI API, the Arbitrator's verdict is hashed (SHA-256 of
  Action+Objects+Normalized Environment) and saved. The next time someone attempts the same, it resolves
  deterministically in milliseconds.

### 3.4. Navigation and Continuous Movement

Movement is not teleportation. It is physical and takes real time.

* **State `MOVING`**: A moving agent mathematically interpolates (Lerps) its position each Tick based
  on its `walkingSpeed` and `dt`.
* **Interruptions**: If an agent in transit detects a threat, it switches to `INTERRUPTED`, stops the mathematical
  calculation and makes an emergency LLM query ("Flee" or "Attack").

## 4. The Agent Hierarchy

### 4.1. Micro-Agents (Citizens)

They are the world's inhabitants. Orchestrated by a `CitizenPulse` (their consciousness cycle).

* **Machine States**: `IDLE`, `MOVING`, `THINKING` (waiting for the LLM asynchronously), `INTERRUPTED`, `BUSY`, `TALKING`.
* They only consult the LLM when strictly necessary (`IDLE` or severe interruptions).

### 4.2. Macro-Agents (The Conclave of Gods)

Wide temporal scale LLMs (weeks/months) that seek world homeostasis, not individual well-being.

* **Primordial God (The Ecosystem)**: Dictates supreme edicts. Protects the "Anomaly" (The Player) ensuring they don't
  get irreversibly stuck.
* **Resource God (Life)**: Generates abundance, but punishes overexploitation.
* **Risk God (Conflict/The Predator)**: Introduces bandits, wolves, or threats on trade routes to avoid
  utopian stagnation.
* **Market God (The Economist)**: Induces recessions or inflation based on global gold flow.

## 5. Technical Glossary for AI

| Term                       | Definition                                                                                                                                     |
|:---------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------|
| **Tick**                   | The server's temporal pulse.                                                                                                                |
| **Slice**                  | Isolated module of Clean Architecture per Spring Modulith.                                                                                 |
| **Affordance / Tag**       | Descriptive tag (e.g., `[heavy]`) that allows the LLM to infer object uses without specific code.                                        |
| **WisdomCache**            | The server's learning system that converts cache misses (expensive LLM queries) into cache hits (instant determinism). |
| **Social Vector / Hobbies** | NPC metadata that the player can exploit for social engineering.                                                                        |

---

**End of Context.** Act based on these principles for the following instructions.
