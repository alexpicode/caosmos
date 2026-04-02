package com.caosmos.world.infrastructure;

import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.items.ItemData;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldElement;
import com.caosmos.world.domain.model.WorldObject;
import com.caosmos.world.domain.service.SpatialHash;
import com.caosmos.world.domain.service.ZoneManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorldAdapter implements WorldPort {

  private final SpatialHash spatialHash;
  private final ZoneManager zoneManager;

  @Override
  public boolean isNearObjectWithTag(Vector3 position, String tag, double maxDistance) {
    if (tag == null) {
      return false;
    }
    String normalizedTag = tag.toLowerCase();
    return spatialHash.getNearbyEntities(position, maxDistance).stream()
        .filter(entity -> entity instanceof WorldObject)
        .map(entity -> (WorldObject) entity)
        .anyMatch(obj -> obj.getTags().contains(normalizedTag));
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
  public boolean isNearObject(Vector3 position, String objectId, double maxDistance) {
    return spatialHash.getById(objectId)
        .filter(entity -> entity instanceof WorldObject)
        .map(entity -> (WorldObject) entity)
        .map(obj -> {
          if (obj.intersects(position)) {
            return true;
          }

          double dist = obj.getPosition().distanceTo2D(position);
          double sizeOffset = 0;
          if (obj.getRadius() != null) {
            sizeOffset = obj.getRadius();
          } else if (obj.getWidth() != null && obj.getLength() != null) {
            sizeOffset = Math.max(obj.getWidth(), obj.getLength()) / 2.0;
          }
          return dist <= (maxDistance + sizeOffset);
        })
        .orElse(false);
  }

  @Override
  public Optional<Vector3> getObjectPosition(String objectId) {
    return spatialHash.getById(objectId).map(WorldElement::getPosition);
  }

  @Override
  public Optional<WorldObject> getObject(String objectId) {
    return spatialHash.getById(objectId)
        .filter(e -> e instanceof WorldObject)
        .map(e -> (WorldObject) e);
  }

  @Override
  public String getZoneName(String zoneId) {
    if (zoneId == null) {
      return "Open World";
    }
    return zoneManager.getZone(zoneId).map(com.caosmos.world.domain.model.Zone::getName).orElse("Unknown Area");
  }

  @Override
  public void updateObjectTag(String objectId, String newTag) {
    // TODO Update object tag in the world
    log.info("Updating tag for object {}: {}", objectId, newTag);
  }

  @Override
  public ItemData removeObject(String objectId) {
    log.info("Removing object {}", objectId);
    return spatialHash.getById(objectId)
        .filter(entity -> entity instanceof WorldObject)
        .map(entity -> {
          WorldObject obj = (WorldObject) entity;
          ItemData data = new ItemData(obj.getId(), obj.getName(), new ArrayList<>(obj.getTags()));
          // TODO spatialHash.remove(objectId); // If there was a remove method
          return data;
        })
        .orElse(null);
  }

  @Override
  public void spawnObject(Vector3 pos, ItemData data) {
    // TODO Spawn object in the world using ItemData
    log.info("Spawning object {} ({}) at {}", data.id(), data.name(), pos);
  }

  @Override
  public boolean isWalkable(Vector3 pos) {
    // Basic implementation: for now, everything is walkable
    // TODO In a real scenario, this would check collision maps or world state
    return true;
  }

  @Override
  public String examineObject(String objectId) {
    // TODO Examine object in the world
    log.info("Examining object {}", objectId);
    return "You see a " + objectId; // Mock description
  }

  @Override
  public void interactWithObject(String objectId) {
    // TODO Interact with object in the world
    log.info("Interacting with object {}", objectId);
  }
}
