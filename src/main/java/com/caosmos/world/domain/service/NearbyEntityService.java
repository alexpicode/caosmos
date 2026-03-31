package com.caosmos.world.domain.service;

import com.caosmos.common.domain.model.world.NearbyEntity;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldEntity;
import com.caosmos.world.domain.model.WorldObject;
import com.caosmos.world.domain.model.Zone;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NearbyEntityService {

  private final SpatialHash spatialHash;
  private final DirectionCalculator directionCalculator;
  private final ZoneManager zoneManager;
  private final NearbyZoneService nearbyZoneService;

  public List<NearbyEntity> getNearbyEntitiesOrdered(
      Vector3 position, double radius, String currentZoneId, Predicate<WorldEntity> filter
  ) {
    List<NearbyEntity> nearbyEntities = new ArrayList<>();
    Set<WorldEntity> nearbyObjects = spatialHash.getNearbyEntities(position, radius);

    Map<String, Zone> zoneMap = zoneManager.getZoneMap();

    for (WorldEntity obj : nearbyObjects) {
      if (filter != null && !filter.test(obj)) {
        continue;
      }

      if (!isEntityVisible(obj, currentZoneId, zoneMap)) {
        continue;
      }

      double distance = position.distanceTo2D(obj.getPosition());
      String direction = directionCalculator.getCardinalDirection(position, obj.getPosition());

      NearbyEntity entity = new NearbyEntity(
          obj.getId(),
          obj.getDisplayName(),
          obj.getCategory(),
          Math.round(distance * 100.0) / 100.0,
          direction,
          (obj instanceof WorldObject wo) ? wo.getTags() : Set.of()
      );
      nearbyEntities.add(entity);
    }

    nearbyEntities.sort(Comparator.comparingDouble(NearbyEntity::distance));
    return nearbyEntities;
  }

  private boolean isEntityVisible(WorldEntity entity, String currentZoneId, Map<String, Zone> zoneMap) {
    String entityZoneId = entity.getZoneId();

    // Same zone: Always visible
    if (entityZoneId == null) {
      return currentZoneId == null;
    }
    if (entityZoneId.equals(currentZoneId)) {
      return true;
    }

    // Check if the entity's zone is perceivable from the current zone
    if (!nearbyZoneService.isZoneVisible(entityZoneId, currentZoneId)) {
      return false;
    }

    // Isolation: Cannot see INSIDE an interior from the outside
    Zone entityZone = zoneMap.get(entityZoneId);
    if (entityZone != null && "INTERIOR".equals(entityZone.getType())) {
      // Since it's not the same zone (checked above), it must be hidden
      return false;
    }

    return true;
  }
}
