# The Sensory Horizon: Perception and Knowledge

## Design Philosophy

For a Caosmos agent, the world is not a set of raw data, but a collection of **Nearby Semantics**. A citizen does not "
know" where all the server's objects are; they only know what falls within their **Sensory Horizon**.

Perception is the bridge between the motor's physical reality and the AI's mental map.

---

## 1. The Immediate Space (Nearby Elements)

The perception system generates a buffer of "Nearby Elements" (`NearbyElements`). This buffer is dynamic and is updated
in each cognitive cycle of the agent.

- **Finitude**: The agent has a distance limit. What is beyond is "the void" until they move or explore.
- **Relativity**: Positions are not processed as global coordinates, but as distances and directions relative to the
  agent ("5 meters to my right", "Behind me").

---

## 2. Cognitive Filtering and POIs

Not all perceived information is useful. To avoid cognitive (and processing) overload, the system applies a semantic
filter:

- **Categorization**: Objects are grouped into functional categories (Beds, Workstations, Containers, Other Citizens).
- **Background Noise vs. Points of Interest (POIs)**: Elements marked as `static` (e.g., a forge, a tree, a mountain)
  are perceived and **permanently registered** in the Mental Map (Zone Memory) as Points of Interest. Dynamic elements (
  other citizens, dropped objects) are not stored in long-term memory, remaining only in the immediate perception
  buffer, thus preventing the agent from filling its memory with transient "noise".
- **Relevance Priority**: An agent will evaluate its reflexes on immediate perception (e.g., "There is food in front of
  me") based on its current needs.

---

## 3. Spatial Visibility Rules

Instead of a costly "Line of Sight" (LoS) calculation based on physics and geometry (raycasting), Caosmos simulates
visibility using **Zone Isolation Rules** (`WorldAdapter.isAccessible`).

An object is only perceptible to a citizen if it is within their vision radius AND meets the containment logic:

- **Interior Isolation**: If an object or citizen is within a zone categorized as `INTERIOR`, it is completely invisible
  to anyone outside it (in an exterior or another interior). Walls conceptually block the view.
- **Exterior Blindness**: A citizen inside an `INTERIOR` cannot perceive generic objects from the exterior. Their
  immediate world is reduced to the room's walls.
- **The Threshold Bridge (Gateways)**: The only exception to these isolation rules are *Gateway* objects (doors). A
  Gateway is the only element that is visible *simultaneously* from both zones it connects (e.g., a house door is
  visible from both the street and the hallway), allowing the citizen to know an exit exists.

---

## 4. From Seeing to Knowing (Memory)

Perception is ephemeral, but **Knowledge** is persistent:

- **Discovery**: When a citizen perceives something new, they register it in their mental map.
- **Obsolescence**: If a citizen remembers an object in a place, but when they return it's no longer there, a "
  Dissonance" occurs that forces the AI to rethink its current task (e.g., "I was going to use this forge, but it's
  gone").

> [!IMPORTANT]
> Perception is the only source of truth for the AI. If you want a citizen to react to something, make sure that "
> something" is semantically visible to them.
