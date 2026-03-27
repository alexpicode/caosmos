package com.caosmos.common.domain.contracts;

import com.caosmos.common.domain.model.items.ItemData;
import com.caosmos.common.domain.model.world.Vector3;
import java.util.List;
import java.util.Optional;

public interface WorldPort {

  void updateObjectTag(String objectId, String newTag);

  ItemData removeObject(String objectId);

  void spawnObject(Vector3 pos, ItemData data);

  boolean isWalkable(Vector3 pos);

  String examineObject(String objectId);

  void interactWithObject(String objectId);

  Optional<Vector3> getObjectPosition(String objectId);

  boolean isNearObjectWithTag(Vector3 position, String tag, double maxDistance);

  boolean checkCollision(Vector3 position);

  List<String> getZoneTagsAt(Vector3 position);

  boolean isNearObject(Vector3 position, String objectId, double maxDistance);
}