package com.caosmos.common.domain.contracts;

import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldEntity;
import java.util.Collection;

public interface WorldRegistry {

  void register(WorldEntity entity);

  void updatePosition(WorldEntity entity, Vector3 newPosition);

  void remove(String entityId);

  Collection<WorldEntity> getEntitiesInBoundingBox(double minX, double minZ, double maxX, double maxZ);
}
