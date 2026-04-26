# 🗺️ Caosmos Roadmap

Caosmos is a simulation engine where AI agents live, interact, and evolve in a persistent world.

> **Current Status**: Early Development — `v0.1.x` (Pre-release)

## 🎯 Quick Direction (For New Contributors)
Looking to jump in? Here's where you can help the most right now:
- **Engine Hackers (Java)**: Check out the `v0.1.x` Stabilization goals (AI integration, Crafting).
- **Prompt Engineers**: Help us improve AI prompt reliability and reduce hallucinations.
- **World Builders**: Experiment with `zones.json` and `world-objects.json` to create new content.

## ⚡ Current Focus
Our primary goal right now is **Stabilization (`v0.1.x`)**. We are focusing on making the core engine rock-solid, improving AI reliability, and polishing existing features before adding complex new systems like economy or ecosystem dynamics.

This document reflects the intended direction of Caosmos as an open-source project. It is a living document and will evolve as the community grows and the simulation matures. All timelines are estimates and subject to change based on contributor activity and emergent priorities.

Contributions are always welcome! Check the [Contributing Guide](CONTRIBUTING.md) and the [Architecture Docs](docs/ARCHITECTURE.md) to get started.

---

## 🧭 Legend

| Symbol | Meaning |
|:-------|:--------|
| ✅ | Implemented |
| 🔄 | In Progress |
| 🗓️ | Planned |
| 💡 | Under Exploration |

---

## ✅ What's Already Built

Before looking ahead, here is a summary of the features that are already implemented and working in the engine:

<details>
<summary>🧠 <b>Autonomous Cognitive Agents</b></summary>

- Per-agent Virtual Thread (Project Loom) running a full perception → reasoning → action cycle
- LLM-driven decision-making via Google Gemini (Spring AI integration)
- Configurable pulse frequency and biological decay (vitality, hunger, energy, stress)
- Agent manifests defined as hybrid `.md` files (YAML frontmatter + Markdown personality)
- Hot-reload of agent manifests without simulation restart
</details>

<details>
<summary>⏱️ <b>Tick-Based Simulation Engine</b></summary>

- `MasterClock` + `MasterTicker` running a deterministic 1-tick-per-second loop
- `AgentHeartbeat` per citizen, decoupled from the main loop via `Condition` signalling
- Overload detection and configurable time-scale
</details>

<details>
<summary>👁️ <b>Spatial Semantic Perception</b></summary>

- `SpatialHash` for O(1) proximity lookups
- Unified `NearbyElement` model covering both entities and zones
- Zone-based visibility and occlusion rules (Interior Isolation, Exterior Blindness, Gateway Bridges)
- Category-based perception filtering to reduce LLM context noise
- Relative position narration ("5 meters to my right")
</details>

<details>
<summary>🗺️ <b>Mental Map & Knowledge System</b></summary>

- Per-citizen `ExplorationTracker` and `ZoneExplorationState`
- Static element registration as Points of Interest (POIs)
- Dissonance detection when a remembered object is no longer present
</details>

<details>
<summary>🎭 <b>Director System (Creative Arbitration)</b></summary>

- `DirectorArbitrator` — resolves `USE` interactions via AI when no cache entry exists
- `ObserverDirector` — generates narrative descriptions for newly examined objects
- `WisdomCacheService` — SHA-256 fingerprinting of interaction semantics; cache-hit path is fully deterministic and AI-free
- `EffectResolver` — applies `SPAWN`, `DESTROY`, `TRANSFORM`, `ADD_TAG`, `REMOVE_TAG`, `MODIFY_CITIZEN`, `SET_DESCRIPTION` mutations
</details>

<details>
<summary>🏗️ <b>World Hierarchy & Zones</b></summary>

- Nested zone tree: `World > Region > City > District > Building > Room`
- Semantic tag inheritance from parent zones
- `GatewayTransition` for context-shifting zone crossings
- Global coordinate system (`Vector3`) with semantic zone association
</details>

<details>
<summary>🏷️ <b>Tag System</b></summary>

- Data-driven tags loaded from `zones.json` and `world-objects.json`
- Semantic tags driving behavior: `safe`, `workstation`, `locked`, `owner:<uuid>`, `static`, `mining`, `commerce`, `forge`, `woodcutting`, `coin_container`
- Environment impact tags: `WET_ENVIRONMENT`, `DARK_ENVIRONMENT`, `FROZEN_ENVIRONMENT`, `SWELTERING_ENVIRONMENT`, `ACTIVE_WIND`
- Weather tags: `clear`, `rainy`, `storm`, `fog`, `snow`, `winter`, `heatwave`
</details>

<details>
<summary>🎬 <b>Action System (19 Action Handlers)</b></summary>

- **Movement**: `EXPLORE`, `TRAVEL_TO`
- **Consumption & Rest**: `EAT`, `DRINK`, `REST`, `SLEEP`
- **Inventory & Equipment**: `PICKUP`, `DROP`, `EQUIP`, `UNEQUIP`
- **Interaction**: `USE`, `INTERACT`, `EXAMINE`
- **Creation & Work**: `CRAFT`, `WORK`
- **Social**: `TALK`
- **Property**: `CLAIM_PROPERTY`
- **Continuity**: `WAIT`, `CONTINUE`
</details>

<details>
<summary>💬 <b>Social & Conversation System</b></summary>

- Multi-participant conversation sessions (`ConversationSession`)
- Session lifecycle: `INITIATED → ACTIVE → STALE → ENDED`
- `SocialHeuristicsEngine` — decides whether a citizen should respond, based on contextual relevance
- `SpeechElement` with configurable TTL for in-world dialogue perception
</details>

<details>
<summary>🏡 <b>Ownership & Property System</b></summary>

- `owner:<uuid>` tag for property and item ownership
- `ClaimPropertyActionHandler` for agents to acquire spaces
- `unowned` tag marking available properties
</details>

<details>
<summary>🏭 <b>Work & Crafting</b></summary>

- `WorkTask` for sustained productivity at tagged workstations (`forge`, `mining`, `woodcutting`, `commerce`)
- `fully_staffed` dynamic tag preventing overcrowding
- Basic `CraftActionHandler` with tool and crafting-tag validation
- `EconomyManager` and `coin_container` for basic currency handling
</details>

<details>
<summary>⚙️ <b>Infrastructure & DevOps</b></summary>

- Docker & Docker Compose deployment
- Multi-platform Docker image CI/CD via GitHub Actions (GHCR)
- Swagger UI / OpenAPI 3 for live simulation inspection
- Structured GitHub Issue Forms (bug reports & feature requests)
</details>

---

## 🚀 Short-Term — `v0.1.x` Stabilization

> **Goal**: Make Caosmos a solid, well-documented, and contributor-friendly foundation.

These items focus on correctness, reliability, and removing known rough edges before the project gets wider exposure.

### 🧠 AI & Action Reliability
- [x] **Smarter Actions**: Ensure agents always provide complete information when taking action (e.g., specifying a direction when exploring).

### 🌍 Smarter World Navigation
- [ ] **Smoother Exploration**: Make agents better at transitioning from familiar places into unknown territories.
- [ ] **Territory Awareness**: Give agents an understanding of regions, so they know when they are operating within a specific city or forest.
- [x] **Visualizing Exploration**: Allow external tools (like the UI) to see exactly what parts of the world an agent has mapped out.

### 🔄 State Integrity & Hot-Reloading
- [x] **Seamless Updates**: Allow tweaking an agent's personality on the fly without interrupting what they are currently doing (keeping their inventory, current tasks, and conversations intact). *(Needs improvement)*
- [x] **Memory Persistence**: Ensure agents don't forget the areas they've explored when the system updates.

### 🛠️ Crafting & Professions
- [ ] **Expanded Crafting**: Allow agents to craft a wider variety of items, moving beyond the current simple recipes.
- [ ] **Specialized Workplaces**: Add support for specific job sites like bakeries, blacksmith forges, or alchemy tables.
- [ ] **Job Alignment**: Ensure citizens naturally gravitate towards workplaces that match their specific skills and roles.

### 🤝 Flexible AI Support
- [ ] **Bring Your Own AI**: Make it easy to plug in different AI models (Google Gemini, Ollama, OpenAI).
- [ ] **Multi-Model Universes**: Allow the simulation to use multiple AI models at once (e.g., a fast local model for simple tasks, and a powerful cloud model for complex world arbitration).

### 🧪 Stability & Engine Polish
- [ ] **Core Testing**: Add comprehensive tests for the action execution pipeline and the Wisdom Cache to prevent regressions.
- [ ] **Clearer Logging**: Improve system logs so developers and server admins can easily track what the AI and the engine are doing.

### 📝 Better Documentation
- [ ] **Code Documentation**: Improve inline comments and Javadocs for core interfaces.
- [ ] **AI Prompt Guide**: Document the logic behind the citizen prompts to help prompt engineers contribute.
- [ ] **World Design Guide**: Create a tutorial on how to build custom zones and objects for the simulation.

---

## 🌱 Medium-Term — `v0.2.x` — A Living Society

> **Goal**: Expand the depth of agent interactions and introduce the first layer of emergent social and economic systems.

### 🧠 Cognitive Evolution
- [ ] **Improved Agent Memory**: Implement short and medium-term memory systems so citizens remember their previous decisions and most relevant world interactions.

### 🤝 Economy & Trade
- [ ] Enable item exchange and trading between agents (`TRADE` action handler)
- [ ] Introduce price negotiation logic driven by the `DirectorArbitrator` or a new `EconomyDirector`
- [ ] Expand `EconomyManager` to support barter-based systems or advanced trade logic

### 💀 Life Cycle Mechanics
- [ ] Introduce basic birth/death mechanics driven by biological threshold crossings
- [ ] Define configurable mortality conditions (starvation, extreme stress, combat damage)
- [ ] Trigger death events that produce persistent world artifacts (e.g., a gravestone object)
- [ ] Enable population self-regulation through birth/death balance

### 🌌 Dynamic World Generation
- [ ] Introduce a **Geography Director** responsible for expanding the world as agents explore unknown territories
- [ ] Generate new zones, resources, and structures dynamically based on agent activity and exploration demand
- [ ] Define a procedural generation contract (`GeographyPort`) so the community can plug in custom world generators

### 🌐 World Events & Ecosystem
- [ ] Add a **Population Director** to manage creature spawning (animals, neutral mobs, monsters)
- [ ] Implement basic ecosystem balance (predators, prey, resource respawning)

### 🔧 Engine Scalability
- [ ] Benchmark and optimize the perception pipeline for 500+ simultaneous agents
- [ ] Profile `SpatialHash` under high-density scenarios and introduce chunked updates
- [ ] Add configurable agent priority tiers so background agents consume fewer ticks

---

## 🌌 Long-Term — `v0.3.x` — Engine Maturity

> **Goal**: Finalize the core "Olympus of Directors", ensure data persistence, and open the engine to external clients.

### 🏛️ The Full Olympus of Directors
- [ ] **Conflict Director** — Orchestrates wars, faction rivalries, and political tension between agent groups
- [ ] **Narrative Director** — Weaves emergent events into coherent story arcs that influence future agent behavior
- [ ] **Culture Director** — Tracks emergent customs, languages, and shared memory across agent populations
- [ ] **World Events Director** — Orchestrates regional and global events (droughts, festivals, migrations) and dynamic world conditions (e.g., droughts, famines)

### 🗺️ World Persistence & API
- [ ] Persist full simulation state to disk (snapshots) for long-running universes
- [ ] Publish a stable, versioned REST API contract for external clients (games, visualization tools, AI experiments)
- [ ] Implement a WebSocket-based event stream for real-time simulation observation
- [ ] Introduce a changelog of world mutations auditable by external tools

---

## 🔭 Future Considerations (Beyond Versioning)

> **Goal**: Moonshot ideas and massive expansions that are being considered for the distant future of the project.

### 🧬 Advanced Agent Cognition
- [ ] Introduce long-term episodic memory beyond the current mental map (relationships, grudges, life history)
- [ ] Implement agent-to-agent trust and reputation systems driven by conversation history
- [ ] Enable agents to form groups, factions, and guilds organically
- [ ] Allow agents to set long-term personal goals (e.g., "become the wealthiest merchant in the city")

### 🎮 Game Engine Bridge
- [ ] Define an official protocol for connecting Caosmos to 3D game engines (Unity, Godot, Unreal)
- [ ] Provide reference integration for the [Caosmos UI](https://github.com/alexpicode/caosmos-ui)
- [ ] Enable bidirectional event flow: game engine sends world mutations → Caosmos reacts

### 🌍 Multi-World Federation
- [ ] Support running multiple simulation instances ("shards") that can exchange agents and events
- [ ] Introduce cross-world travel mechanics driven by the Geography Director
- [ ] Define a federation protocol for distributed Caosmos deployments

### 🛠️ Advanced Modding & Tooling
- [ ] Support a plugin system so external modules can register new `ActionHandler` or `Director` implementations at runtime
- [ ] Support time-travel / replay of past simulation states for debugging and storytelling

---

## 🤝 How to Contribute

We welcome contributors of all kinds — engine hackers, AI prompt engineers, world builders, and documentation writers!

- 🐛 **Found a bug?** → [Open a Bug Report](https://github.com/alexpicode/caosmos/issues/new/choose)
- 💡 **Have an idea?** → [Request a Feature](https://github.com/alexpicode/caosmos/issues/new/choose)
- 🏗️ **Want to build?** → Read the [Contributing Guide](CONTRIBUTING.md) and the [Architecture Docs](docs/ARCHITECTURE.md)
- 📧 **Want to talk?** → Reach us at [alexpicode@proton.me](mailto:alexpicode@proton.me)

> *The universe of Caosmos is built by the stories of its contributors. What will you add?*
