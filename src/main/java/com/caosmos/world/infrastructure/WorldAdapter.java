package com.caosmos.world.infrastructure;

import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.items.ItemData;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldEntity;
import com.caosmos.world.domain.model.WorldObject;
import com.caosmos.world.domain.service.SpatialHash;
import java.util.ArrayList;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorldAdapter implements WorldPort {

  private final SpatialHash spatialHash;

  @Override
  public Optional<Vector3> getObjectPosition(String objectId) {
    return spatialHash.getById(objectId).map(WorldEntity::getPosition);
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
