package com.caosmos.common.domain.contracts;

import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldElement;
import java.util.Collection;

public interface WorldRegistry {

  void register(WorldElement entity);

  void updatePosition(WorldElement entity, Vector3 newPosition);

  void remove(String entityId);

  Collection<WorldElement> getEntitiesInBoundingBox(double minX, double minZ, double maxX, double maxZ);
}
