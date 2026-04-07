package com.caosmos.world.infrastructure;

import com.caosmos.common.domain.contracts.WorldPerceptionProvider;
import com.caosmos.common.domain.model.world.Environment;
import com.caosmos.common.domain.model.world.EntityType;
import com.caosmos.common.domain.model.world.Location;
import com.caosmos.common.domain.model.world.NearbyElement;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldDate;
import com.caosmos.common.domain.model.world.WorldElement;
import com.caosmos.common.domain.model.world.WorldPerception;
import com.caosmos.common.domain.model.world.ZoneType;
import com.caosmos.world.application.WorldObjectInitializer;
import com.caosmos.world.domain.model.PeripheralPerception;
import com.caosmos.world.domain.model.WorldObject;
import com.caosmos.world.domain.model.Zone;
import com.caosmos.world.domain.service.EnvironmentService;
import com.caosmos.world.domain.service.NearbyPerceptionService;
import com.caosmos.world.domain.service.SpatialHash;
import com.caosmos.world.domain.service.WorldTimeService;
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
  private final WorldTimeService worldTimeService;
  private final EnvironmentService environmentService;
  private final NearbyPerceptionService nearbyPerceptionService;
  private final WorldObjectInitializer worldObjectInitializer;

  @Value("${caosmos.world.max-vision-distance}")
  private double maxVisionDistance;

  @Value("${caosmos.world.explore-search-radius:250.0}")
  private double exploreSearchRadius;

  @PostConstruct
  public void initialize() {
    worldObjectInitializer.initializeDefaultObjects();
  }

  @Override
  public WorldPerception getPerceptionAt(Vector3 position, String currentZoneId, Predicate<WorldElement> filter) {
    Optional<Zone> currentZone = currentZoneId != null ? zoneManager.getZone(currentZoneId) : Optional.empty();

    Optional<Zone> zoneOpt;
    if (currentZone.isPresent() && currentZone.get().isEntryRestricted()) {
      zoneOpt = currentZone; // Logical Wall enforced
    } else {
      zoneOpt = zoneManager.findZoneAt(position, currentZoneId);
    }
    String zoneName = zoneOpt.map(Zone::getName).orElse("Unknown Territory");
    ZoneType zoneType = zoneOpt.map(Zone::getZoneType).orElse(ZoneType.EXTERIOR);

    Map<String, Zone> allZones = zoneManager.getZoneMap();
    Set<String> tags = zoneOpt.map(z -> z.getEffectiveTags(allZones)).orElse(Set.of());
    String parentZoneName = zoneOpt.flatMap(z -> Optional.ofNullable(z.getParentZoneId())).flatMap(zoneManager::getZone)
        .map(Zone::getName).orElse(null);

    WorldDate worldDate = worldTimeService.getWorldDate();
    Environment globalEnv = environmentService.getCurrentEnvironment();

    String effectiveLightLevel;
    List<String> effectiveEnvTags;

    if (ZoneType.INTERIOR == zoneType) {
      effectiveLightLevel = "Artificial";
      effectiveEnvTags = List.of();
    } else {
      effectiveLightLevel = globalEnv.lightLevel();
      effectiveEnvTags = globalEnv.tags();
    }

    Environment perceivedEnv = new Environment(globalEnv.terrainType(), effectiveEnvTags, effectiveLightLevel);

    Set<String> finalTags = new HashSet<>(tags);
    if (ZoneType.EXTERIOR == zoneType) {
      finalTags.addAll(effectiveEnvTags);
    }

    PeripheralPerception peripheralPerception = nearbyPerceptionService.getPeripheralPerception(
        position,
        maxVisionDistance,
        zoneOpt.map(Zone::getId).orElse(null),
        filter
    );
    var nearbyElements = peripheralPerception.elements();

    Set<String> categoriesForExplore = getCategoriesInRadius(position, exploreSearchRadius);

    String currentLocation = getCurrentLocation(nearbyElements);

    Location location = new Location(
        zoneName,
        zoneType.name(),
        zoneOpt.map(Zone::getCategory).orElse("UNKNOWN"),
        currentLocation,
        finalTags,
        parentZoneName,
        zoneOpt.map(Zone::getId).orElse(null)
    );

    return new WorldPerception(worldDate, location, perceivedEnv, nearbyElements, categoriesForExplore);
  }

  private Set<String> getCategoriesInRadius(Vector3 position, double radius) {
    Set<String> categories = new java.util.HashSet<>();

    // We only need to consult SpatialHash as it now contains both WorldObjects and Zones
    spatialHash.getNearbyEntities(position, radius).stream()
        .forEach(e -> {
          if (e.getCategory() != null) {
            categories.add(e.getCategory());
          }
        });

    return categories;
  }

  private static String getCurrentLocation(List<NearbyElement> nearbyElements) {
    if (CollectionUtils.isNotEmpty(nearbyElements)) {
      var closest = nearbyElements.stream()
          .filter(e -> EntityType.OBJECT == e.type())
          .findFirst();

      if (closest.isPresent() && closest.get().distance() <= 2.0) {
        // Remove the current location entity from nearbyElements
        nearbyElements.remove(closest.get());
        return closest.get().name();
      }
    }
    return "Open Area";
  }

  public void addWorldObject(WorldObject worldObject) {
    worldObjectInitializer.addCustomObject(worldObject);
  }

  public void removeWorldObject(String objectId) {
    WorldElement toRemove = null;
    Set<WorldElement> allObjects = spatialHash.getNearbyEntities(new Vector3(0, 0, 0), Double.MAX_VALUE);

    for (WorldElement obj : allObjects) {
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
