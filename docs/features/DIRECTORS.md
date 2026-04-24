# The Directors: Orchestrators of Emergence

## Design Philosophy

In Caosmos, the simulation works most of the time in a purely deterministic way (mathematics and code rules). However,
for the world to be rich, emergent, and unpredictable, the intervention of Language Models (LLMs) is required.

This is where the **Directors** (Macro-Agents) come in. They are architectural components that act as "Gods" or
invisible orchestrators: they intervene only when the deterministic system has no answer, inject creativity, and then
withdraw after carving their decision in stone.

---

## 1. The Director's Role

A Director follows a specific pattern of **Data Hunt and Creative Fallback**:

1. **Deterministic Attempt**: The Director always tries to resolve the request by first consulting the current state of
   the world in memory (e.g., the `WorldAdapter` or cache).
2. **AI Intervention**: If the answer does not exist (it's a new interaction or an undescribed object), the Director
   formulates a structured request to the AI.
3. **Persistence (Mutation)**: Once the AI returns a creative result, the Director issues a "Mutation" (`StateMutation`)
   to alter the world permanently.

This pattern ensures that AI is used as a content and rule *generator* on demand, not as a real-time processor, keeping
the simulation fast and cost-effective.

---

## 2. Implemented Directors

Currently, the system has specialized orchestrators:

### A. Director Arbitrator (The Judge of Physics)

Responsible for resolving physical interactions through the `USE` action.

- **When it intervenes**: If a citizen attempts an interaction whose semantic signature does not exist in the **Wisdom
  Cache**.
- **What it does**: Decides if the action succeeds and what effects it produces (e.g., fire, destruction, creation).
- **The legacy**: Saves its verdict in the cache so that next time physics acts instantaneously without consulting the
  AI.

### B. Observer Director (The Discoverer)

Responsible for orchestrating the `EXAMINE` action (sensory inspection).

- **When it intervenes**: If a citizen examines an object and it does not have a description assigned in the
  configuration or in memory.
- **What it does**: Sends the object name, category, tags, and current environment to the AI to generate a narrative
  description consistent with the world's style (e.g., "A sword rusted by the rain").
- **The legacy**: Uses the `EffectResolver` to mutate the object in the world (`SET_DESCRIPTION`), so that the object
  remains permanently described for future citizens.

---

## 3. Summary

The Directors are the layer that connects the strict physical engine with the limitless creativity of AI. They do not
control citizens directly, but rather **sculpt the world's rules and narrative** for citizens to live in.
