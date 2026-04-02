package com.caosmos.world.domain.service;

import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.world.domain.model.Zone;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ZoneManager {

  private final SpatialHash spatialHash;
  private final List<Zone> zones = new ArrayList<>();

  public void addZone(Zone zone) {
    zones.add(zone);
    spatialHash.register(zone);
  }

  public Optional<Zone> findZoneAt(Vector3 position, String currentZoneId) {
    Map<String, Zone> zoneMap = getZoneMap();

    return spatialHash.getNearbyEntities(position, 0.0)
        .stream()
        .filter(e -> e instanceof Zone)
        .map(e -> (Zone) e)
        .filter(z -> z.contains(position))
        .sorted(Comparator.comparingInt((Zone z) -> z.getHierarchyDepth(zoneMap)).reversed())
        .filter(z -> !z.isEntryRestricted() || z.getId().equals(currentZoneId))
        .findFirst();
  }

  public String getZoneName(Vector3 position, String currentZoneId) {
    return findZoneAt(position, currentZoneId).map(Zone::getName).orElse("Unknown Territory");
  }

  public Map<String, Zone> getZoneMap() {
    return zones.stream().collect(Collectors.toMap(Zone::getId, Function.identity()));
  }

  public Optional<Zone> getZone(String id) {
    return zones.stream().filter(z -> z.getId().equals(id)).findFirst();
  }

  public List<Zone> getAllZones() {
    return new ArrayList<>(zones);
  }

  public void clearZones() {
    zones.clear();
    spatialHash.clear();
  }

  public Optional<Zone> findNearestZoneWithTag(Vector3 position, String tag) {
    return zones.stream()
        .filter(z -> z.getTags() != null && z.getTags().stream().anyMatch(t -> t.equalsIgnoreCase(tag)))
        .min(Comparator.comparingDouble(z -> position.distanceTo2D(z.getCenter())));
  }

  public Optional<Zone> findNearestCity(Vector3 position) {
    return findNearestZoneWithTag(position, "urban");
  }
}
