# Tag System

## Design Philosophy

The Caosmos engine relies heavily on a **Tag System** to drive world logic, AI behavior, and entity interactions.
Instead of relying on rigid class hierarchies or complex conditional trees, we use semantic strings (tags) to decorate
zones, objects, and items.

### Why Tags?

- **Decoupling**: The AI doesn't need to know exactly what an object *is*, only what it *can do* (e.g., it doesn't
  matter if it's a "Forge" or a "Kitchen", if it has the `workstation` tag, a citizen can work there).
- **Flexibility**: Behavior can be changed dynamically at runtime by adding or removing tags without modifying code.
- **Data-Driven**: Most tags are loaded from JSON configurations (`zones.json`, `world-objects.json`), allowing
  designers to create complex behaviors without developer intervention.

---

## Special Semantic Tags

The following tags are explicitly checked by the engine to trigger specific behaviors:

### 1. Security & Safety

- `safe`: Defines a zone where citizens feel secure.
    - **Effect**: Citizens in a `safe` zone reduce stress faster during repose or wait tasks.

### 2. Property & Ownership

- `owner:` (Prefix): Used for property ownership. Following the prefix is the citizen's UUID.
    - **Effect**: Restricts usage or allows the owner to perform specific management actions.
- `unowned`: Explicitly marks a property as available for claiming.

### 3. Access Control

- `locked`: Prevents any interaction with the object or zone.
    - **Effect**: Citizens will fail to use or enter elements with this tag until it is removed.

### 4. Work & Industry

- `workstation`: Identifies an element as a valid place for a `WorkTask`.
- `fully_staffed`: A dynamic tag applied to workstations when they reach maximum capacity.
- **Work Types**: Used to match a citizen's job or skills with a workplace:
    - `mining`
    - `commerce`
    - `forge`
    - `woodcutting`

### 5. Item Capabilities

Items use tags to define what they can be used for:

- `tool`: General identifier for usable equipment.
- `crafting`: Required for craft-related actions.
- `mining`: Required to interact with mineral resources.
- `woodcutting`: Required to interact with trees/wood sources.
- `coin_container`: Identifies objects that contain coins. Picking these up automatically adds coins to the citizen's
  balance.

### 6. Perception & Optimization

- `static`: Used for performance and logic filtering.
    - **Effect**: Identifies elements that are permanent or rarely change. The system uses this to optimize perception
      updates and filter out background "noise" from active citizen decision-making.

### 7. Environment Logic

The system uses both static and dynamic environment tags to influence world state:

- **EnvironmentImpactTag (Enum)**:
    - `WET_ENVIRONMENT`: Affects movement and health.
    - `DARK_ENVIRONMENT`: Affects visibility and stress.
    - `ACTIVE_WIND`: Affects projectile physics or movement.
    - `FROZEN_ENVIRONMENT` / `SWELTERING_ENVIRONMENT`: Temperature-based impacts.
- **Dynamic Context**:
    - `city` / `urban`: Contextual tags for AI.
    - `day` / `night`: Time-based behavior modifiers.
- **Weather Status**:
    - `clear`
    - `rainy`
    - `storm`
    - `fog`
    - `snow`
    - `winter`
    - `heatwave`

---

## Usage in Code

To check for a tag in the domain layer, use the `WorldElement.getTags()` method. In the application layer, use the
`CitizenPort` or `WorldPort` methods.

```java
// Example: Checking if a citizen is in a safe place
if (citizenPort.isInZoneWithTag(citizenId, "safe")) {
    // reduce stress
}

// Example: Filtering for workstations
entities.stream()
    .filter(e -> e.getTags().contains("workstation"))
    .collect(Collectors.toList());
```

> [!TIP]
> Always prefer using semantic tags to drive logic instead of checking for specific object IDs or types.
