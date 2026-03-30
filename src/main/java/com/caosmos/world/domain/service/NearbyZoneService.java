package com.caosmos.world.domain.service;

import com.caosmos.common.domain.model.world.NearbyZone;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.world.domain.model.Zone;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NearbyZoneService {

  private final ZoneManager zoneManager;
  private final DirectionCalculator directionCalculator;

  private static final int MAX_REPETITIVE_ZONES = 3;

  public List<NearbyZone> getNearbyZones(Vector3 position, String currentZoneId, double radius) {
    Map<String, Zone> allZones = zoneManager.getZoneMap();
    Zone currentZone = allZones.get(currentZoneId);

    List<Zone> filteredZones = zoneManager.getAllZones()
        .stream()
        .filter(z -> !z.getId().equals(currentZoneId)) // Exclude current zone
        .filter(z -> position.distanceTo2D(z.getCenter()) <= radius) // Distance filter
        .filter(z -> isZoneVisible(z, currentZone, allZones)) // Visibility filter
        .sorted(Comparator.comparingDouble(z -> position.distanceTo2D(z.getCenter())))
        .toList();

    return limitRedundancy(filteredZones, position);
  }

  public boolean isZoneVisible(String targetZoneId, String currentZoneId) {
    if (targetZoneId == null) {
      return currentZoneId == null;
    }
    if (targetZoneId.equals(currentZoneId)) {
      return true;
    }

    Map<String, Zone> allZones = zoneManager.getZoneMap();
    Zone targetZone = allZones.get(targetZoneId);
    Zone currentZone = allZones.get(currentZoneId);

    if (targetZone == null) {
      return false;
    }

    return isZoneVisible(targetZone, currentZone, allZones);
  }

  private boolean isZoneVisible(Zone targetZone, Zone currentZone, Map<String, Zone> allZones) {
    return isHierarchicallyRelevant(targetZone, currentZone, allZones)
        && isAllowedByInteriorRule(targetZone, currentZone);
  }

  private boolean isHierarchicallyRelevant(Zone zone, Zone currentZone, Map<String, Zone> allZones) {
    if (currentZone == null) {
      // If no current zone, only show root zones
      return zone.getParentId() == null;
    }

    // Direct Parent
    if (currentZone.getParentId() != null && currentZone.getParentId().equals(zone.getId())) {
      return true;
    }

    // Direct Children
    if (zone.getParentId() != null && zone.getParentId().equals(currentZone.getId())) {
      return true;
    }

    // Siblings: Only visible if we are in an EXTERIOR zone (Open Sight rule)
    if ("EXTERIOR".equals(currentZone.getType())) {
      if (currentZone.getParentId() == null && zone.getParentId() == null) {
        return true;
      }
      return currentZone.getParentId() != null && currentZone.getParentId().equals(zone.getParentId());
    }

    return false;
  }

  private boolean isAllowedByInteriorRule(Zone zone, Zone currentZone) {
    if (currentZone == null || !"INTERIOR".equals(currentZone.getType())) {
      return true;
    }

    // If current is INTERIOR, hide all EXTERIOR except parent
    if ("EXTERIOR".equals(zone.getType())) {
      return currentZone.getParentId() != null && currentZone.getParentId().equals(zone.getId());
    }

    return true;
  }

  private List<NearbyZone> limitRedundancy(List<Zone> zones, Vector3 position) {
    // Group by Type + Tags to identify repetitive zones
    Map<String, List<Zone>> grouped = zones.stream()
        .collect(Collectors.groupingBy(z -> z.getType() + "_" + z.getTags()
            .stream()
            .sorted()
            .collect(Collectors.joining(","))));

    List<NearbyZone> result = new ArrayList<>();
    for (List<Zone> group : grouped.values()) {
      group.stream().limit(MAX_REPETITIVE_ZONES).forEach(z -> result.add(mapToNearbyZone(z, position)));
    }

    return result.stream().sorted(Comparator.comparingDouble(NearbyZone::distance)).toList();
  }

  public Optional<Zone> findNearestZoneWithTag(Vector3 position, String tag) {
    return zoneManager.getAllZones()
        .stream()
        .filter(z -> z.getTags() != null && z.getTags().stream().anyMatch(t -> t.equalsIgnoreCase(tag)))
        .min(Comparator.comparingDouble(z -> position.distanceTo2D(z.getCenter())));
  }

  public Optional<Zone> findNearestCity(Vector3 position) {
    return findNearestZoneWithTag(position, "urban");
  }

  private NearbyZone mapToNearbyZone(Zone zone, Vector3 position) {
    double distance = position.distanceTo2D(zone.getCenter());
    String direction = directionCalculator.getCardinalDirection(position, zone.getCenter());

    return new NearbyZone(
        zone.getId(),
        zone.getName(),
        zone.getType(),
        Math.round(distance * 100.0) / 100.0,
        direction,
        zone.getTags()
    );
  }
}
