package com.caosmos.world.infrastructure;

import com.caosmos.common.domain.contracts.WorldPerceptionProvider;
import com.caosmos.common.domain.model.world.Location;
import com.caosmos.common.domain.model.world.NearbyEntity;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldDate;
import com.caosmos.common.domain.model.world.WorldEntity;
import com.caosmos.common.domain.model.world.WorldPerception;
import com.caosmos.world.application.WorldObjectInitializer;
import com.caosmos.world.domain.model.WorldObject;
import com.caosmos.world.domain.model.Zone;
import com.caosmos.world.domain.service.EnvironmentService;
import com.caosmos.world.domain.service.NearbyEntityService;
import com.caosmos.world.domain.service.SpatialHash;
import com.caosmos.world.domain.service.TimeService;
import com.caosmos.world.domain.service.ZoneManager;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpatialWorldPerceptionProvider implements WorldPerceptionProvider {

  private final SpatialHash spatialHash;
  private final ZoneManager zoneManager;
  private final TimeService timeService;
  private final EnvironmentService environmentService;
  private final NearbyEntityService nearbyEntityService;
  private final WorldObjectInitializer worldObjectInitializer;

  @Value("${caosmos.world.max-vision-distance}")
  private double maxVisionDistance;

  @PostConstruct
  public void initialize() {
    worldObjectInitializer.initializeDefaultObjects();
  }

  @Override
  public WorldPerception getPerceptionAt(Vector3 position, Predicate<WorldEntity> filter) {
    Optional<Zone> zoneOpt = zoneManager.findZoneAt(position);
    String zoneName = zoneOpt.map(Zone::getName).orElse("Unknown Territory");
    String zoneType = zoneOpt.map(Zone::getType).orElse("EXTERIOR");

    WorldDate worldDate = timeService.getCurrentWorldDate();
    var environment = environmentService.getCurrentEnvironment();
    var nearbyEntities = nearbyEntityService.getNearbyEntitiesOrdered(position, maxVisionDistance, filter);

    String currentLocation = getCurrentLocation(nearbyEntities);

    Location location = new Location(
        zoneName,
        zoneType,
        currentLocation
    );

    return new WorldPerception(worldDate, location, environment, nearbyEntities);
  }

  private static String getCurrentLocation(List<NearbyEntity> nearbyEntities) {
    if (CollectionUtils.isNotEmpty(nearbyEntities)) {
      var closest = nearbyEntities.getFirst();
      if (closest.distance() <= 2.0) {
        // Remove the current location entity from nearbyEntities
        nearbyEntities.removeFirst();
        return closest.name();
      }
    }
    return "Open Area";
  }

  public void addWorldObject(WorldObject worldObject) {
    worldObjectInitializer.addCustomObject(worldObject);
  }

  public void removeWorldObject(String objectId) {
    WorldEntity toRemove = null;
    Set<WorldEntity> allObjects = spatialHash.getNearbyEntities(new Vector3(0, 0, 0), Double.MAX_VALUE);

    for (WorldEntity obj : allObjects) {
      if (obj.getId().equals(objectId)) {
        toRemove = obj;
        break;
      }
    }

    if (toRemove != null) {
      spatialHash.remove(toRemove.getId());
    }
  }
}
