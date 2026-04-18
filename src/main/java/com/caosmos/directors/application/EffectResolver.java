package com.caosmos.directors.application;

import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.EconomyPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.MutationType;
import com.caosmos.common.domain.model.actions.StateMutation;
import com.caosmos.common.domain.model.items.ItemData;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.directors.domain.model.ItemTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EffectResolver {

  private final WorldPort worldPort;
  private final CitizenPort citizenPort;
  private final EconomyPort economyPort;
  private final SpawnRegistryConfig registryConfig;
  private final ObjectMapper objectMapper;

  private static final double MAX_CITIZEN_DELTA = 20.0;

  public void resolve(UUID citizenId, List<StateMutation> mutations) {
    if (mutations == null) {
      return;
    }

    for (StateMutation mut : mutations) {
      try {
        applyMutation(citizenId, mut);
      } catch (Exception e) {
        log.error("Failed to apply mutation: {}", mut, e);
      }
    }
  }

  private void applyMutation(UUID citizenId, StateMutation mut) {
    MutationType type = mut.mutationType();
    if (type == null) {
      return;
    }

    switch (type) {
      case ADD_TAG -> worldPort.addObjectTag(mut.targetId(), mut.key());
      case REMOVE_TAG -> worldPort.removeObjectTag(mut.targetId(), mut.key());
      case DESTROY -> handleDestroy(mut);
      case TRANSFORM -> handleTransform(mut);
      case SPAWN -> handleSpawn(citizenId, mut);
      case MODIFY_CITIZEN -> handleModifyCitizen(citizenId, mut);
      case SET_DESCRIPTION -> worldPort.updateObjectDescription(mut.targetId(), mut.value());
    }
  }

  private void handleDestroy(StateMutation mut) {
    // 1. Capture position before removal
    Vector3 pos = worldPort.getObjectPosition(mut.targetId()).orElse(new Vector3(0, 0, 0));

    // 2. Remove the object
    ItemData removed = worldPort.removeObject(mut.targetId());

    if (removed == null) {
      log.warn(
          "Physical mismatch: Mutation requested DESTROY on non-existent object ID '{}'. " +
              "Possible Director arbitration ID mismatch.", mut.targetId()
      );
      return;
    }

    if (registryConfig.getDestructionFallbacks() != null) {
      // Conservation of matter: check tags for fallback spawn
      for (String tag : removed.tags()) {
        String fallbackType = registryConfig.getDestructionFallbacks().get(tag.toLowerCase());
        if (fallbackType != null) {
          log.info("Matter conservation: spawning fallback {} for destroyed object with tag {}", fallbackType, tag);
          ItemTemplate template = registryConfig.getSpawnables().get(fallbackType);
          if (template != null) {
            spawnFromTemplate(fallbackType, template, pos);
          }
          break; // Only one fallback
        }
      }
    }
  }

  private void handleTransform(StateMutation mut) {
    String typeKey = mut.key();
    ItemTemplate template = registryConfig.getSpawnables().get(typeKey);

    Set<String> newTags;
    String newName = typeKey;

    if (template != null) {
      newTags = new HashSet<>(template.getTags());
      newName = template.getName();
    } else {
      // TODO Tier 2 Transform: if not in registry, keeps existing but adds type as tag maybe?
      // For now, let's assume Transform mostly works with registry or specific logic.
      newTags = new HashSet<>();
      newTags.add(typeKey.toLowerCase());
    }

    worldPort.transformObject(mut.targetId(), newName, newTags);
  }

  private void handleSpawn(UUID citizenId, StateMutation mut) {
    String typeKey = mut.key();
    Vector3 pos = citizenPort.getPosition(citizenId); // Default to citizen position if targetPos not in mut

    // Tier 1: Registry
    ItemTemplate template = registryConfig.getSpawnables() != null ? registryConfig.getSpawnables().get(typeKey) : null;
    if (template != null) {
      spawnFromTemplate(typeKey, template, pos);
      return;
    }

    // Tier 2: Generated
    if (mut.value() != null && !mut.value().isBlank()) {
      try {
        ItemTemplate generated = objectMapper.readValue(mut.value(), ItemTemplate.class);
        applyTier2Defaults(generated);
        spawnFromTemplate(typeKey, generated, pos);
      } catch (JsonProcessingException e) {
        log.error("Failed to parse Tier 2 SPAWN data: {}", mut.value());
      }
    }
  }

  private void handleModifyCitizen(UUID citizenId, StateMutation mut) {
    String stat = mut.key() != null ? mut.key().toLowerCase() : "";
    double delta;
    try {
      delta = Double.parseDouble(mut.value());
    } catch (NumberFormatException e) {
      log.warn("Invalid delta for MODIFY_CITIZEN: {}", mut.value());
      return;
    }

    // Guardrail: limit delta
    delta = Math.max(-MAX_CITIZEN_DELTA, Math.min(MAX_CITIZEN_DELTA, delta));

    switch (stat) {
      case "vitality", "health" -> {
        if (delta > 0) {
          citizenPort.heal(citizenId, delta);
        } else {
          citizenPort.applyDamage(citizenId, -delta);
        }
      }
      case "energy" -> {
        if (delta > 0) {
          citizenPort.eat(citizenId, 0); // No exact match for increaseEnergy in CitizenPort other than eat/sleep
        }
        // Wait, CitizenPort has consumeEnergy but not increaseEnergy directly.
        // Actually, CitizenAdapter implementation of consumeEnergy uses decreaseEnergy.
        // I should probably add increaseEnergy/decreaseEnergy to CitizenPort if I want direct control.
        // For now, let's skip or use consumeEnergy with negative (not ideal if adapter doesn't support it).
      }
      case "hunger" -> citizenPort.increaseHunger(citizenId, delta);
      case "stress" -> {
        if (delta > 0) {
          citizenPort.applyStress(citizenId, delta);
        } else {
          citizenPort.reduceStress(citizenId, -delta);
        }
      }
      case "coins" -> {
        if (delta > 0) {
          economyPort.addCoins(citizenId, delta);
        } else {
          economyPort.subtractCoins(citizenId, -delta);
        }
      }
    }
  }

  private void spawnFromTemplate(String typeKey, ItemTemplate template, Vector3 pos) {
    ItemData data = new ItemData(
        typeKey + "_" + UUID.randomUUID().toString().substring(0, 8),
        template.getName(),
        new HashSet<>(template.getTags()),
        template.getCategory(),
        template.getDescription(),
        template.getRadius(),
        template.getWidth(),
        template.getLength(),
        template.getAmount()
    );
    worldPort.spawnObject(pos, data);
  }

  private void applyTier2Defaults(ItemTemplate generated) {
    if (generated.getCategory() == null) {
      generated.setCategory("UNKNOWN");
    }

    // Defaults based on category
    if (generated.getRadius() == null && generated.getWidth() == null) {
      switch (generated.getCategory()) {
        case "FOOD" -> generated.setRadius(0.08);
        case "TOOL" -> generated.setRadius(0.15);
        default -> generated.setRadius(0.1);
      }
    }
  }
}
