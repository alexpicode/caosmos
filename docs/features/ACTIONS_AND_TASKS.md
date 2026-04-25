# Will Cycle: Intentions and Executions

## Design Philosophy

In Caosmos, the behavior engine is based on a hierarchy where the **Action** is the catalyst for all change. A citizen
does not simply "be" in a task; they enter it through a validated action.

---

## 1. Actions (The Catalyst)

An **Action** is the unit of intention that the system (AI or User) sends to the citizen. It is the "Verb" that
initiates any interaction with the world.

- **Entry Point**: All AI decisions are first manifested as an `ActionRequest`.
- **Validation and Gatekeeping**: Before any change occurs, an `ActionHandler` validates if the action is physically
  possible (e.g., Are you near? Do you have the tool?).
- **Dual Natures**: An action can have two natures depending on its purpose:
    - **Atomic Action**: Executes instantaneously and finishes (e.g., `PICKUP` to collect an object).
    - **Generating Action**: If validation is positive, the action "installs" a persistent **Task** in the citizen (
      e.g., `WORK` or `EXPLORE`).

---

## 2. Tasks (The Continuous Execution)

A **Task** is the persistent state a citizen enters after a generating action. It represents a flow of logic that stays
alive across multiple server "ticks", implementing the `executeOnTick` interface.

- **State of Being**: While a task is active, the citizen changes their cognitive state (e.g., `MOVING`, `BUSY`).
- **Direct Modification**: Unlike other systems, Tasks **do not generate internal Actions**. The task manipulates the
  citizen's state directly:
    - Calculates movement step-by-step (Lerp) and updates position (`setPosition`).
    - Gradually consumes energy and hunger by interacting with the citizen's biology.
    - Evaluates when the goal has been reached (e.g., reaching the exploration limit).
- **Interruption**: Tasks return an `ActiveTask` on each tick and can be interrupted by external events (danger) or
  replaced by new actions.

---

## 3. The Flow of Reality

1. **Intention (`ActionRequest`)**: The AI decides: "I want to explore the North".
2. **Handling (`ActionHandler`)**: The engine validates: "Can this citizen start exploring now?".
3. **Activation (`Task`)**: If valid, an `ExploreTask` is instantiated and assigned.
4. **Physical Simulation**: On each tick, the `ExploreTask` mathematically calculates the new position, moves the
   citizen 0.1 meters, reduces their energy, and checks for collisions, without generating new "Actions".
5. **Completion**: The task finishes on its own (goal met) or is replaced by a new action.

> [!IMPORTANT]
> Remember: **No Action, No Task.** The Task is the persistent consequence of a will expressed through an Action.
