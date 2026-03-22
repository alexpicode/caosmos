package com.caosmos.common.domain.contracts;

import com.caosmos.common.domain.model.world.Vector3;
import java.util.Optional;

public interface WorldPort {

  void updateObjectTag(String objectId, String newTag);

  void removeObject(String objectId);

  void spawnObject(Vector3 pos, String templateId);

  boolean isWalkable(Vector3 pos);

  String examineObject(String objectId);

  void interactWithObject(String objectId);

  Optional<Vector3> getObjectPosition(String objectId);
}