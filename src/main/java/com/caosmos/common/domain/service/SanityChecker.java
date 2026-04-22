package com.caosmos.common.domain.service;

import com.caosmos.common.domain.contracts.CitizenPort;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.actions.ActionIntent;
import com.caosmos.common.domain.model.world.EntityType;
import com.caosmos.common.domain.model.world.WorldConstants;
import com.caosmos.common.domain.model.world.WorldElement;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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

    // 4. Semantic Locks (Physical Protection)
    // We only hard-block actions if the target is explicitly LOCKED.
    // If it's just OWNED but NOT LOCKED, we allow it (emergence/stealing).
    if (isProtectedAction(intent.verb())) {
      var element = worldPort.getElement(intent.targetId());
      if (element.isPresent() && element.get().getTags().contains(WorldConstants.TAG_LOCKED)) {
        Optional<String> ownershipError = validateOwnership(intent.citizenId(), intent.targetId(), worldPort);
        if (ownershipError.isPresent()) {
          return Optional.of("This is locked.");
        }
      }
    }

    // Additional checks for energy can be added here
    return Optional.empty();
  }

  // Helper methods for future use (e.g. Crime system or explicit locks)
  private boolean isProtectedAction(String verb) {
    if (verb == null) {
      return false;
    }
    String v = verb.toUpperCase();
    return v.equals("USE") || v.equals("INTERACT");
  }

  private Optional<String> validateOwnership(UUID citizenId, String targetId, WorldPort worldPort) {
    var elementOpt = worldPort.getElement(targetId);
    if (elementOpt.isEmpty()) {
      return Optional.empty();
    }

    WorldElement element = elementOpt.get();
    String citizenOwnerTag = WorldConstants.PREFIX_OWNER + citizenId.toString();

    // 1. Direct ownership
    Set<String> tags = element.getTags();
    boolean hasOwner = tags.stream().anyMatch(t -> t.startsWith(WorldConstants.PREFIX_OWNER));
    if (hasOwner && !tags.contains(citizenOwnerTag)) {
      return Optional.of("This belongs to someone else.");
    }

    // 2. Zone ownership (Inherited protection)
    String zoneId = element.getZoneId();
    if (zoneId != null) {
      Set<String> zoneTags = worldPort.getObjectTags(zoneId);
      boolean zoneHasOwner = zoneTags.stream().anyMatch(t -> t.startsWith(WorldConstants.PREFIX_OWNER));
      if (zoneHasOwner && !zoneTags.contains(citizenOwnerTag)) {
        return Optional.of("You don't have permission to do that in this property.");
      }
    }

    return Optional.empty();
  }
}
