# Wisdom Cache: The World's Memory

## Design Philosophy

In a world based on **Semantic Physics**, the possibilities for interaction are infinite. We cannot program every
possible combination of objects, tools, and environments. The **Wisdom Cache** is the mechanism that allows the world
to "learn" and remember how these interactions should be resolved, turning creative AI judgments into deterministic and
efficient physical laws.

---

## 1. The Dilemma: Creativity vs. Determinism

- **The Problem**: Consulting a Language Model (LLM) for every physical interaction (e.g., "What happens if I use a fire
  axe on a frozen tree?") is slow, expensive, and can be inconsistent.
- **The Solution**: The Wisdom Cache acts as a filter. If the world has already decided once what happens in that
  specific situation, it doesn't need to "think" again; it simply applies the already established law.

---

## 2. The Learning Process

When a citizen attempts a complex interaction (usually through the `USE` action), the system follows this flow:

### A. Semantic Intent Capture

The "footprints" of the interaction are collected:

- **Verb**: The action (e.g., `USE`).
- **Tool Tags**: (e.g., `[tool]`, `[woodcutting]`).
- **Target Tags**: (e.g., `[static]`, `[locked]`).
- **Environment Tags**: (e.g., `[rainy]`, `[night]`).

### B. The Fingerprint

The system sorts and normalizes these tags to generate a unique **SHA-256 Hash**. This hash represents the "signature"
of that specific physical interaction.

### C. The Fast Path (Cache HIT)

If the hash already exists in the Wisdom Cache, the result is returned in milliseconds. The world behaves
deterministically: same conditions, same result.

### D. The Creative Path (Cache MISS)

If it's the first time this combination occurs:

1. An **AI Judge (DirectorArbitrator)** analyzes the semantics and issues a verdict (e.g., "The tree thaws but does not
   burn due to humidity").
2. The verdict is executed.
3. **The result is stored in the Wisdom Cache.** From this moment on, the world has "learned" a new physical rule.

---

## 3. Benefits of Collective Wisdom

- **Zero Latency**: Once an interaction is common (e.g., chopping wood), the system stops using AI and becomes as fast
  as a traditional game engine.
- **Coherent Emergence**: Allows unexpected (emergent) situations to occur without breaking the world's coherence.
- **Engine Evolution**: The Wisdom Cache can persist across sessions, allowing the simulation engine to become "wiser"
  and more complex the more it is played.

---

## 4. Practical Example

> **Action**: `USE` (Tool: *Torch*) on (Target: *Powder Barrel*) in (Environment: *Coal Mine*).
>
> 1. **First attempt**: The AI dictates a massive explosion and structural damage. The result is saved.
> 2. **Subsequent attempts**: Any citizen doing the same will trigger the same explosion instantly, without consulting
     the AI.

> [!TIP]
> The Wisdom Cache is what allows Caosmos to feel like a living, logical world without sacrificing the performance of a
> high-traffic server.
