package com.caosmos.world.domain.service;

import com.caosmos.common.domain.model.world.NearbyEntity;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldEntity;
import com.caosmos.world.domain.model.WorldObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NearbyEntityService {

  private final SpatialHash spatialHash;
  private final DirectionCalculator directionCalculator;

  public List<NearbyEntity> getNearbyEntitiesOrdered(Vector3 position, double radius, Predicate<WorldEntity> filter) {
    List<NearbyEntity> nearbyEntities = new ArrayList<>();
    Set<WorldEntity> nearbyObjects = spatialHash.getNearbyEntities(position, radius);

    for (WorldEntity obj : nearbyObjects) {
      if (filter != null && !filter.test(obj)) {
        continue;
      }
      double distance = position.distanceTo2D(obj.getPosition());
      String direction = directionCalculator.getCardinalDirection(position, obj.getPosition());

      NearbyEntity entity = new NearbyEntity(
          obj.getId(),
          obj.getDisplayName(),
          Math.round(distance * 100.0) / 100.0,
          direction,
          (obj instanceof WorldObject wo) ? wo.getTags() : List.of()
      );
      nearbyEntities.add(entity);
    }

    nearbyEntities.sort(Comparator.comparingDouble(NearbyEntity::distance));
    return nearbyEntities;
  }
}
