package com.caosmos.common.domain.service;

import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.ActionIntent;
import com.caosmos.common.domain.model.world.EntityType;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SanityChecker {

  // Default proximity for USE if not available locally
  private static final double PROXIMITY_USE = 2.5;

  public Optional<String> validate(ActionIntent intent, CitizenPort citizenPort, WorldPort worldPort) {
    if (intent.targetId() == null || intent.targetId().isBlank()) {
      return Optional.of("Target ID is required for " + intent.verb());
    }

    // 1. If the item is already equipped or in inventory, it's inherently near
    // (distance 0)
    if (citizenPort.isItemEquipped(intent.citizenId(), intent.targetId()) ||
        citizenPort.isItemInInventory(intent.citizenId(), intent.targetId())) {
      return Optional.empty();
    }

    // 2. Otherwise, check world proximity
    if (!citizenPort.isNear(intent.citizenId(), intent.targetId(), PROXIMITY_USE)) {
      return Optional.of("You are too far from " + intent.targetId() + " to " + intent.verb().toLowerCase() + ".");
    }

    var worldObject = worldPort.getObject(intent.targetId());
    if (worldObject.isEmpty()) {
      // Check if it's a zone to give a better error message
      String zoneName = worldPort.getZoneName(intent.targetId());
      if (zoneName != null && !zoneName.isBlank()) {
        if ("EXAMINE".equalsIgnoreCase(intent.verb())) {
          return Optional.of("You cannot EXAMINE a zone. Move into it or use EXPLORE to learn more about locations.");
        }
      }
      return Optional.of("Object doesn't exist.");
    }

    // 3. EXAMINE restrictions (No citizens)
    if ("EXAMINE".equalsIgnoreCase(intent.verb())) {
      if (EntityType.CITIZEN.equals(worldObject.get().getType())) {
        return Optional.of(
            "You cannot EXAMINE another person. Use TALK to interact with them and learn about them through conversation.");
      }
    }

    // Additional checks for energy can be added here
    return Optional.empty();
  }
}
