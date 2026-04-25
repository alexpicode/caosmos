# Context Geography: Hierarchy and Thresholds

## Design Philosophy

The world of Caosmos is not an infinite plain, but an onion of **Semantic Containers**. Space organization follows a
logical hierarchy where each level inherits properties from the one above, allowing the simulation to be scalable and
coherent.

---

## 1. The World as a Matryoshka (Zones)

Every element in the world (citizen, object, building) exists within a **Zone**. Zones can contain other zones, creating
a hierarchy tree:

`World > Region > City > District > Building > Room`

- **Semantic Inheritance**: If the "City" zone has the `urban` tag, all citizens within its child zones know they are in
  an urban context, as they inherit tags from parent zones.
- **Ownership**: The hierarchy naturally defines who owns what. Owning a "Building" implies having authority over all
  the "Rooms" it contains.

---

## 2. Thresholds and Transitions (GatewayTransition)

**Gates** (internally implemented via `GatewayTransition`) are objects in the world that act as connection points
between zones.

- **Reality Change**: Crossing a Gate is not just moving; the world adapter (`WorldAdapter`) evaluates the object's
  `targetZoneId` to move the citizen from one context to another (e.g., from the public exterior to the private
  interior).
- **Access Semantics**: A Gate can be `locked`, requiring the AI to reason about how to gain access (keys, permissions,
  or force). A citizen in an exterior cannot perceive objects inside an interior unless they cross the appropriate Gate.

---

## 3. Global Coordinates and Local Logic

Although the hierarchy suggests containers, the physical engine (`WorldAdapter`) uses a **Global Coordinate System** (
`Vector3`) for all elements.

- **Absolute Positioning**: An object's position is not an offset (local X, Y) from the room's center, but its absolute
  coordinate in the world. The association with the room (`zoneId`) is used purely for semantic logic and visibility (as
  seen in the previous section).
- **Route Inference (Semantic Navigation)**: The engine code does not implement an automatic pathfinding algorithm
  through zone hierarchies (e.g., A* for room nodes). The engine only exposes immediate transitions (`Gateways`). **It
  is the responsibility of the AI (the LLM)** to infer the logical route. For example, the AI must reason: *"If I am
  in 'Room A' and I want to go to the 'Street', I must first use the door object to exit the room"*.

---

## 4. Context as a Behavior Engine

The zone hierarchy dictates what behaviors are appropriate:

- **Danger Zones**: A zone with the `wilderness` tag triggers alert behaviors.
- **Rest Zones**: A zone with the `safe` tag (inside a house) allows citizens to reduce their stress.
- **Work Zones**: Workshops and forges are zones that "enable" actions that would be impossible in a public square.

> [!TIP]
> When designing the world, first think about what **context layers** the citizen needs to understand where they are.
> Geometry is secondary to the semantics of space.
