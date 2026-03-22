package com.caosmos.world.infrastructure;

import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldEntity;
import com.caosmos.world.domain.service.SpatialHash;
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
    log.info("Updating tag for object {}: {}", objectId, newTag);
  }

  @Override
  public void removeObject(String objectId) {
    log.info("Removing object {}", objectId);
  }

  @Override
  public void spawnObject(Vector3 pos, String templateId) {
    log.info("Spawning object {} at {}", templateId, pos);
  }

  @Override
  public boolean isWalkable(Vector3 pos) {
    // Basic implementation: for now, everything is walkable
    // In a real scenario, this would check collision maps or world state
    return true;
  }

  @Override
  public String examineObject(String objectId) {
    log.info("Examining object {}", objectId);
    return "You see a " + objectId; // Mock description
  }

  @Override
  public void interactWithObject(String objectId) {
    log.info("Interacting with object {}", objectId);
  }
}
