package com.caosmos.world.infrastructure;

import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.items.ItemData;
import com.caosmos.common.domain.model.world.CollisionResult;
import com.caosmos.common.domain.model.world.EntityType;
import com.caosmos.common.domain.model.world.EnvironmentImpactTag;
import com.caosmos.common.domain.model.world.GatewayTransition;
import com.caosmos.common.domain.model.world.SpeechElement;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldConstants;
import com.caosmos.common.domain.model.world.WorldElement;
import com.caosmos.common.domain.model.world.ZoneMetadata;
import com.caosmos.common.domain.model.world.ZoneType;
import com.caosmos.world.domain.model.WorldObject;
import com.caosmos.world.domain.model.Zone;
import com.caosmos.world.domain.service.EnvironmentNormalizer;
import com.caosmos.world.domain.service.EnvironmentService;
import com.caosmos.world.domain.service.SpatialHash;
import com.caosmos.world.domain.service.SpeechManager;
import com.caosmos.world.domain.service.VisualCoverageCalculator;
import com.caosmos.world.domain.service.ZoneCollisionService;
import com.caosmos.world.domain.service.ZoneManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorldAdapter implements WorldPort {

  private final SpatialHash spatialHash;
  private final ZoneManager zoneManager;
  private final SpeechManager speechManager;
  private final EnvironmentService environmentService;
  private final EnvironmentNormalizer environmentNormalizer;
  private final ZoneCollisionService zoneCollisionService;
  private final VisualCoverageCalculator visualCoverageCalculator;

  @Override
  public boolean isNearObjectWithTag(Vector3 position, String currentZoneId, String tag, double maxDistance) {
    if (tag == null) {
      return false;
    }
    String normalizedTag = tag.toLowerCase();
    return spatialHash.getNearbyEntities(position, maxDistance).stream()
        .filter(entity -> EntityType.OBJECT == entity.getType() || EntityType.CITIZEN == entity.getType()
            || EntityType.ZONE == entity.getType())
        .filter(entity -> isAccessible(entity, currentZoneId))
        .anyMatch(entity -> entity.getTags().contains(normalizedTag));
  }

  @Override
  public boolean checkCollision(Vector3 position) {
    // Check nearby objects first for performance
    return spatialHash.getNearbyEntities(position, 5.0).stream()
        .filter(entity -> entity instanceof WorldObject)
        .map(entity -> (WorldObject) entity)
        .anyMatch(obj -> obj.intersects(position));
  }

  @Override
  public List<String> getZoneTagsAt(Vector3 position) {
    return zoneManager.findZoneAt(position, null)
        .map(zone -> (List<String>) new ArrayList<>(zone.getEffectiveTags(zoneManager.getZoneMap())))
        .orElse(Collections.emptyList());
  }

  @Override
  public boolean isNearObject(Vector3 position, String currentZoneId, String objectId, double maxDistance) {
    return spatialHash.getById(objectId)
        .filter(entity -> EntityType.OBJECT == entity.getType() || EntityType.CITIZEN == entity.getType()
            || EntityType.ZONE == entity.getType())
        .filter(entity -> isAccessible(entity, currentZoneId))
        .map(entity -> {
          if (entity instanceof WorldObject obj) {
            if (obj.intersects(position)) {
              return true;
            }
            return obj.distanceTo2D(position) <= maxDistance;
          }
          // Generic distance check for non-WorldObjects (Zones, Citizens)
          return entity.distanceTo2D(position) <= maxDistance;
        })
        .orElse(false);
  }

  private boolean isAccessible(WorldElement target, String observerZoneId) {
    String elementZoneId = target.getZoneId();

    // 1. Same zone or context: Always accessible
    if (Objects.equals(target.getZoneId(), observerZoneId) || Objects.equals(
        target.getParentZoneId(),
        observerZoneId
    )) {
      return true;
    }

    // 2. Gateway Bridge: Gateways are accessible from both linked zones
    if (target instanceof WorldObject obj && obj.getTargetZoneId() != null) {
      if (Objects.equals(obj.getTargetZoneId(), observerZoneId)) {
        return true;
      }
    }

    // 3. Strict Interior isolation: If target is in an Interior, observer MUST be in the same zone
    if (elementZoneId != null) {
      Optional<Zone> zone = zoneManager.getZone(elementZoneId);
      if (zone.isPresent() && ZoneType.INTERIOR == zone.get().getZoneType()) {
        return false;
      }
    }

    // 4. Rule: Cannot reach OUTSIDE from an interior if they don't match (unless it was a gateway handled above)
    if (observerZoneId != null) {
      Optional<Zone> observerZone = zoneManager.getZone(observerZoneId);
      if (observerZone.isPresent() && ZoneType.INTERIOR == observerZone.get().getZoneType()) {
        // From inside an Interior, you can't reach generic Exterior objects
        return false;
      }
    }

    return true; // Both are exterior and don't match? Potentially possible (siblings)
  }

  @Override
  public Optional<Vector3> getObjectPosition(String objectId) {
    return spatialHash.getById(objectId).map(WorldElement::getPosition);
  }

  @Override
  public Optional<WorldElement> getObject(String objectId) {
    return spatialHash.getById(objectId)
        .filter(entity -> EntityType.OBJECT == entity.getType() || EntityType.CITIZEN == entity.getType());
  }

  @Override
  public Optional<WorldElement> getElement(String elementId) {
    return spatialHash.getById(elementId);
  }

  @Override
  public Optional<GatewayTransition> getGatewayTransition(String gatewayId, String currentZoneId) {
    return spatialHash.getById(gatewayId)
        .filter(entity -> entity instanceof WorldObject)
        .map(entity -> (WorldObject) entity)
        .map(obj -> {
          if (obj.getTargetZoneId() == null) {
            return null;
          }
          if (Objects.equals(currentZoneId, obj.getParentZoneId())) {
            return new GatewayTransition(obj.getTargetZoneId());
          } else if (Objects.equals(currentZoneId, obj.getTargetZoneId())) {
            return new GatewayTransition(obj.getParentZoneId());
          }
          return null;
        })
        .filter(Objects::nonNull);
  }

  @Override
  public String getZoneName(String zoneId) {
    if (zoneId == null) {
      return "Open World";
    }
    return zoneManager.getZone(zoneId).map(com.caosmos.world.domain.model.Zone::getName).orElse("Unknown Area");
  }

  @Override
  public void addTag(String elementId, String tag) {
    if (elementId == null || tag == null) {
      return;
    }
    spatialHash.getById(elementId).ifPresent(entity -> {
      if (entity instanceof WorldObject obj) {
        obj.addTag(tag);
        log.debug("Added tag '{}' to object {}", tag, elementId);
      } else if (entity instanceof Zone zone) {
        zone.addTag(tag);
        log.debug("Added tag '{}' to zone {}", tag, elementId);
      }
    });
  }

  @Override
  public void updateObjectDescription(String objectId, String description) {
    spatialHash.getById(objectId).ifPresent(entity -> {
      if (entity instanceof WorldObject obj) {
        obj.setDescription(description);
        log.debug("Updated description for object {}", objectId);
      }
    });
  }

  @Override
  public void removeTag(String elementId, String tag) {
    if (elementId == null || tag == null) {
      return;
    }
    spatialHash.getById(elementId).ifPresent(entity -> {
      if (entity instanceof WorldObject obj) {
        obj.removeTag(tag);
        log.debug("Removed tag '{}' from object {}", tag, elementId);
      } else if (entity instanceof Zone zone) {
        zone.removeTag(tag);
        log.debug("Removed tag '{}' from zone {}", tag, elementId);
      }
    });
  }

  @Override
  public void transformObject(String objectId, String newType, Set<String> newTags) {
    spatialHash.getById(objectId).ifPresent(entity -> {
      if (entity instanceof WorldObject obj) {
        obj.transform(newType, newTags);
        log.info("Transformed object {} into {}", objectId, newType);
      }
    });
  }

  @Override
  public ItemData removeObject(String objectId) {
    log.debug("Removing object {}", objectId);
    return spatialHash.getById(objectId)
        .filter(entity -> entity instanceof WorldObject)
        .map(entity -> {
          WorldObject obj = (WorldObject) entity;
          ItemData data = new ItemData(
              obj.getId(),
              obj.getName(),
              obj.getTags(),
              obj.getCategory(),
              obj.getDescription(),
              obj.getRadius(),
              obj.getWidth(),
              obj.getLength(),
              obj.getAmount()
          );
          spatialHash.remove(objectId);
          return data;
        })
        .orElse(null);
  }

  @Override
  public void spawnObject(Vector3 pos, String currentZoneId, ItemData data) {
    if (data == null) {
      return;
    }
    log.info("Spawning object {} ({}) at {} in zone {}", data.id(), data.name(), pos, currentZoneId);

    // 1. We prioritize the passed currentZoneId to ensure logical consistency (e.g. dropping inside a house)
    // If null, we try to detect it, but it's better to pass it from the citizen context.

    Set<String> tags = new HashSet<>(data.tags());

    // Ownership Inheritance Logic: only in INTERIOR zones (private property/businesses)
    boolean alreadyHasOwner = tags.stream().anyMatch(t -> t.startsWith(WorldConstants.PREFIX_OWNER));
    if (!alreadyHasOwner && currentZoneId != null) {
      zoneManager.getZone(currentZoneId).ifPresent(zone -> {
        if ("INTERIOR".equalsIgnoreCase(zone.getCategory())) {
          zone.getTags().stream()
              .filter(t -> t.startsWith(WorldConstants.PREFIX_OWNER))
              .findFirst()
              .ifPresent(tags::add);
        }
      });
    }

    WorldObject newObj = new WorldObject(
        data.id(),
        data.name(),
        data.category(),
        pos,
        tags,
        data.description(),
        currentZoneId,
        null, // targetZoneId (not a gateway)
        data.radius(),
        data.width(),
        data.length(),
        data.amount()
    );

    // 3. Register in the spatial hash
    spatialHash.register(newObj);
    log.info("Spawned object {} ({}) at {} in zone {}", data.name(), data.id(), pos, currentZoneId);
  }

  @Override
  public boolean isWalkable(Vector3 pos) {
    // Basic implementation: for now, everything is walkable
    // TODO In a real scenario, this would check collision maps or world state
    return true;
  }

  @Override
  public void interactWithObject(String objectId) {
    // TODO Interact with object in the world
    log.debug("Interacting with object {}", objectId);
  }

  @Override
  public void spawnSpeech(SpeechElement speech) {
    speechManager.register(speech);
  }

  @Override
  public void consumeSpeech(String speechId) {
    speechManager.consumeEarly(speechId);
  }

  @Override
  public SortedSet<EnvironmentImpactTag> getNormalizedEnvironmentTags(String zoneId) {
    ZoneType zoneType = zoneId != null ?
        zoneManager.getZone(zoneId).map(Zone::getZoneType).orElse(ZoneType.EXTERIOR)
        : ZoneType.EXTERIOR;

    return environmentNormalizer.normalize(environmentService.getEffectiveEnvironment(zoneType));
  }

  @Override
  public Set<String> getObjectTags(String objectId) {
    if (objectId == null) {
      return Collections.emptySet();
    }
    return spatialHash.getById(objectId)
        .map(WorldElement::getTags)
        .orElse(Collections.emptySet());
  }

  @Override
  public CollisionResult validateMovement(Vector3 from, Vector3 to, String zoneId) {
    if (zoneId == null) {
      return new CollisionResult(to, false);
    }
    return zoneManager.getZone(zoneId)
        .map(zone -> zoneCollisionService.validateMovement(from, to, zone))
        .orElse(new CollisionResult(to, false));
  }

  @Override
  public boolean canSeeEntireZone(Vector3 observerPos, String zoneId, double visionRadius) {
    if (zoneId == null) {
      return false;
    }
    return zoneManager.getZone(zoneId)
        .map(zone -> visualCoverageCalculator.canSeeEntireZone(observerPos, zone, visionRadius))
        .orElse(false);
  }

  @Override
  public Collection<WorldElement> getElementsInZone(String zoneId) {
    if (zoneId == null) {
      return Collections.emptyList();
    }
    return spatialHash.getAllEntities().stream()
        .filter(e -> zoneId.equals(e.getParentZoneId()))
        .toList();
  }

  @Override
  public Optional<ZoneMetadata> getZoneMetadata(String zoneId) {
    if (zoneId == null) {
      return Optional.of(new ZoneMetadata(null, "Open World", ZoneType.EXTERIOR.name(), "WORLD", 0.0, 0.0));
    }
    return zoneManager.getZone(zoneId)
        .map(zone -> new ZoneMetadata(
            zone.getId(),
            zone.getName(),
            zone.getZoneType().name(),
            zone.getCategory(),
            zone.getWidth(),
            zone.getLength()
        ));
  }
}
