package com.caosmos.world.domain.service;

import com.caosmos.common.domain.model.world.NearbyElement;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldElement;
import com.caosmos.world.domain.model.PeripheralPerception;
import com.caosmos.world.domain.model.WorldObject;
import com.caosmos.world.domain.model.Zone;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NearbyPerceptionService {

  private final SpatialHash spatialHash;
  private final DirectionCalculator directionCalculator;
  private final ZoneManager zoneManager;

  private static final int MAX_REPETITIVE_ZONES = 3;

  public PeripheralPerception getPeripheralPerception(
      Vector3 position,
      double radius,
      String currentZoneId,
      Predicate<WorldElement> filter
  ) {
    Set<WorldElement> nearbyElements = spatialHash.getNearbyEntities(position, radius);
    Map<String, Zone> zoneMap = zoneManager.getZoneMap();
    Zone currentZone = zoneMap.get(currentZoneId);

    List<NearbyElement> elements = new ArrayList<>();

    for (WorldElement element : nearbyElements) {
      if (filter != null && !filter.test(element)) {
        continue;
      }

      if (!isElementVisible(element, currentZone, zoneMap)) {
        continue;
      }

      double distance = position.distanceTo2D(element.getPosition());
      String direction = directionCalculator.getCardinalDirection(position, element.getPosition());

      if (element instanceof Zone zone) {
        if (!zone.getId().equals(currentZoneId)) {
          elements.add(mapToNearbyElement(zone, distance, direction));
        }
      } else {
        elements.add(new NearbyElement(
            element.getId(),
            element.getName(),
            element.getCategory(),
            "OBJECT",
            null,
            Math.round(distance * 100.0) / 100.0,
            direction,
            (element instanceof WorldObject wo) ? wo.getTags() : Set.of()
        ));
      }
    }

    // Process zones to limit repetitive ones
    List<NearbyElement> zoneElements = elements.stream()
        .filter(e -> "ZONE".equals(e.type()))
        .toList();
    List<NearbyElement> objectElements = elements.stream()
        .filter(e -> "OBJECT".equals(e.type()))
        .toList();

    List<NearbyElement> processedZones = processZones(zoneElements);

    List<NearbyElement> finalElements = new ArrayList<>();
    finalElements.addAll(objectElements);
    finalElements.addAll(processedZones);

    finalElements.sort(Comparator.comparingDouble(NearbyElement::distance));

    return new PeripheralPerception(finalElements);
  }

  private boolean isElementVisible(WorldElement element, Zone currentZone, Map<String, Zone> zoneMap) {
    String elementZoneId = element.getZoneId();
    String currentZoneId = currentZone != null ? currentZone.getId() : null;

    // 1. Same zone (or both at root): Always visible
    if (Objects.equals(elementZoneId, currentZoneId)) {
      return true;
    }

    // 2. Navigation: If it's a zone, we only care about hierarchy/connectivity
    if (element instanceof Zone targetZone) {
      return isZoneVisible(targetZone, currentZone, zoneMap);
    }

    // 3. Oclusion rules for Objects (Entities)
    Zone elementZone = zoneMap.get(elementZoneId);
    if (elementZone == null) {
      return false;
    }

    // Must be in a connected zone
    if (!isZoneVisible(elementZone, currentZone, zoneMap)) {
      return false;
    }

    // Rule: Cannot see INSIDE an interior if we are not in it
    if ("INTERIOR".equals(elementZone.getZoneType())) {
      return false;
    }

    // Rule: Cannot see OUTSIDE from an interior
    if (currentZone != null && "INTERIOR".equals(currentZone.getZoneType())
        && "EXTERIOR".equals(elementZone.getZoneType())) {
      return false;
    }

    return true;
  }

  public boolean isZoneVisible(Zone targetZone, Zone currentZone, Map<String, Zone> allZones) {
    if (!isHierarchicallyRelevant(targetZone, currentZone, allZones)) {
      return false;
    }
    return isAllowedByInteriorRule(targetZone, currentZone);
  }

  private boolean isHierarchicallyRelevant(Zone zone, Zone currentZone, Map<String, Zone> allZones) {
    if (currentZone == null) {
      return zone.getParentZoneId() == null;
    }

    // Direct Parent
    if (currentZone.getParentZoneId() != null && currentZone.getParentZoneId().equals(zone.getId())) {
      return true;
    }

    // Direct Children
    if (zone.getParentZoneId() != null && zone.getParentZoneId().equals(currentZone.getId())) {
      return true;
    }

    // Siblings: Only if Exterior (Open Sight)
    if ("EXTERIOR".equals(currentZone.getZoneType())) {
      if (currentZone.getParentZoneId() == null && zone.getParentZoneId() == null) {
        return true;
      }
      return currentZone.getParentZoneId() != null && currentZone.getParentZoneId().equals(zone.getParentZoneId());
    }

    return false;
  }

  private boolean isAllowedByInteriorRule(Zone zone, Zone currentZone) {
    if (currentZone == null || !"INTERIOR".equals(currentZone.getZoneType())) {
      return true;
    }

    // If current is INTERIOR, hide EXTERIOR except parent
    if ("EXTERIOR".equals(zone.getZoneType())) {
      return currentZone.getParentZoneId() != null && currentZone.getParentZoneId().equals(zone.getId());
    }

    return true;
  }

  private List<NearbyElement> processZones(List<NearbyElement> zoneElements) {
    // Group by zoneType + Tags
    Map<String, List<NearbyElement>> grouped = zoneElements.stream()
        .collect(Collectors.groupingBy(z -> z.zoneType() + "_" + (z.tags() != null ? z.tags()
            .stream()
            .sorted()
            .collect(Collectors.joining(",")) : "")));

    List<NearbyElement> result = new ArrayList<>();
    for (List<NearbyElement> group : grouped.values()) {
      group.stream()
          .limit(MAX_REPETITIVE_ZONES)
          .forEach(result::add);
    }
    return result;
  }

  private NearbyElement mapToNearbyElement(Zone zone, double distance, String direction) {
    return new NearbyElement(
        zone.getId(),
        zone.getName(),
        zone.getCategory(),
        "ZONE",
        zone.getZoneType(),
        Math.round(distance * 100.0) / 100.0,
        direction,
        zone.getTags()
    );
  }
}
