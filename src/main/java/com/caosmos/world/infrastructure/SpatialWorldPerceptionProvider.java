package com.caosmos.world.infrastructure;

import com.caosmos.common.domain.contracts.WorldPerceptionProvider;
import com.caosmos.common.domain.model.world.Environment;
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
import com.caosmos.world.domain.service.NearbyZoneService;
import com.caosmos.world.domain.service.SpatialHash;
import com.caosmos.world.domain.service.TimeService;
import com.caosmos.world.domain.service.ZoneManager;
import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
  private final NearbyZoneService nearbyZoneService;
  private final WorldObjectInitializer worldObjectInitializer;

  @Value("${caosmos.world.max-vision-distance}")
  private double maxVisionDistance;

  @PostConstruct
  public void initialize() {
    worldObjectInitializer.initializeDefaultObjects();
  }

  @Override
  public WorldPerception getPerceptionAt(Vector3 position, String currentZoneId, Predicate<WorldEntity> filter) {
    Optional<Zone> zoneOpt = zoneManager.findZoneAt(position, currentZoneId);
    String zoneName = zoneOpt.map(Zone::getName).orElse("Unknown Territory");
    String zoneType = zoneOpt.map(Zone::getType).orElse("EXTERIOR");

    Map<String, Zone> allZones = zoneManager.getZoneMap();
    Set<String> tags = zoneOpt.map(z -> z.getEffectiveTags(allZones)).orElse(Set.of());
    String parentZoneName = zoneOpt.flatMap(z -> Optional.ofNullable(z.getParentId())).flatMap(zoneManager::getZone)
        .map(Zone::getName).orElse(null);

    WorldDate worldDate = timeService.getCurrentWorldDate();
    Environment globalEnv = environmentService.getCurrentEnvironment();

    String effectiveLightLevel;
    List<String> effectiveEnvTags;

    if ("INTERIOR".equals(zoneType)) {
      effectiveLightLevel = "Artificial";
      effectiveEnvTags = List.of();
    } else {
      effectiveLightLevel = globalEnv.lightLevel();
      effectiveEnvTags = globalEnv.tags();
    }

    Environment perceivedEnv = new Environment(globalEnv.terrainType(), effectiveEnvTags, effectiveLightLevel);

    Set<String> finalTags = new HashSet<>(tags);
    if ("EXTERIOR".equals(zoneType)) {
      finalTags.addAll(effectiveEnvTags);
    }

    var nearbyEntities = nearbyEntityService.getNearbyEntitiesOrdered(
        position,
        maxVisionDistance,
        zoneOpt.map(Zone::getId).orElse(null),
        filter
    );
    var nearbyZones = nearbyZoneService.getNearbyZones(
        position,
        zoneOpt.map(Zone::getId).orElse(null),
        maxVisionDistance
    );

    String currentLocation = getCurrentLocation(nearbyEntities);

    Location location = new Location(
        zoneName,
        zoneType,
        currentLocation,
        finalTags,
        parentZoneName,
        zoneOpt.map(Zone::getId).orElse(null)
    );

    return new WorldPerception(worldDate, location, perceivedEnv, nearbyEntities, nearbyZones);
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
