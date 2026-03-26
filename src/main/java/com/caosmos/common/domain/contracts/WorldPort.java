package com.caosmos.common.domain.contracts;

import com.caosmos.common.domain.model.items.ItemData;
import com.caosmos.common.domain.model.world.Vector3;
import java.util.Optional;

public interface WorldPort {

  void updateObjectTag(String objectId, String newTag);

  ItemData removeObject(String objectId);

  void spawnObject(Vector3 pos, ItemData data);

  boolean isWalkable(Vector3 pos);

  String examineObject(String objectId);

  void interactWithObject(String objectId);

  Optional<Vector3> getObjectPosition(String objectId);
}