package com.caosmos.world.domain.service;

import com.caosmos.common.domain.model.world.EntityType;
import com.caosmos.common.domain.model.world.NearbyElement;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldElement;
import com.caosmos.common.domain.model.world.ZoneType;
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

      // 1. Visibility rules
      if (element.isLimitedToZone()) {
        if (!Objects.equals(element.getZoneId(), currentZoneId)) {
          continue;
        }
      } else {
        if (!isElementVisible(element, currentZone, zoneMap)) {
          continue;
        }
      }

      double distance = position.distanceTo2D(element.getPosition());
      String direction = directionCalculator.getCardinalDirection(position, element.getPosition());

      elements.add(element.toNearbyElement(distance, direction));
    }

    // Process zones to limit repetitive ones
    List<NearbyElement> zoneElements = elements.stream()
        .filter(e -> EntityType.ZONE == e.type())
        .toList();
    List<NearbyElement> objectElements = elements.stream()
        .filter(e -> (EntityType.OBJECT == e.type() || EntityType.SPEECH == e.type() || EntityType.CITIZEN == e.type()))
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

    // 1. Same zone: Always visible
    if (Objects.equals(elementZoneId, currentZoneId)) {
      return true;
    }

    // Perception Bridge (Gateway Visibility) - Gateways are always visible
    if (element instanceof WorldObject obj && Objects.equals(obj.getTargetZoneId(), currentZoneId)) {
      return true;
    }

    // 2. Navigation: If it's a zone, we only care about hierarchy/connectivity
    if (element instanceof Zone targetZone) {
      return isZoneVisible(targetZone, currentZone, zoneMap);
    }

    // 3. Occlusion rules for Objects/Entities
    Zone elementZone = zoneMap.get(elementZoneId);
    if (elementZone == null) {
      return false;
    }

    // Must be in a visible connected zone in general terms
    if (!isZoneVisible(elementZone, currentZone, zoneMap)) {
      return false;
    }

    // Rule: Cannot see INSIDE an interior if we are not in it
    if (ZoneType.INTERIOR == elementZone.getZoneType() && !Objects.equals(elementZoneId, currentZoneId)) {
      if (elementZone.isEntryRestricted()) {
        return false;
      }
      // If not restricted, can only see inside from its direct parent
      if (!Objects.equals(elementZone.getParentZoneId(), currentZoneId)) {
        return false;
      }
    }

    // Rule: Cannot see OUTSIDE from an interior (strict isolation from exterior)
    if (currentZone != null && ZoneType.INTERIOR == currentZone.getZoneType()
        && ZoneType.EXTERIOR == elementZone.getZoneType()) {
      if (currentZone.isEntryRestricted()) {
        return false;
      }
      // If not restricted, can only see outside the direct parent
      if (!Objects.equals(currentZone.getParentZoneId(), elementZoneId)) {
        return false;
      }
    }

    return true;
  }

  public boolean isZoneVisible(Zone targetZone, Zone currentZone, Map<String, Zone> allZones) {
    if (currentZone == null) {
      return targetZone.getParentZoneId() == null;
    }

    if (Objects.equals(targetZone.getId(), currentZone.getId())) {
      return true;
    }

    boolean isTargetInterior = ZoneType.INTERIOR == targetZone.getZoneType();
    boolean isCurrentInterior = ZoneType.INTERIOR == currentZone.getZoneType();

    if (isTargetInterior) {
      // Rule: Citizen should only see the interior zone if they are INSIDE it.
      if (isObserverInside(targetZone, currentZone, allZones)) {
        return true;
      }

      // Exception: If the interior zone does not have restricted entry, 
      // they can see the zone (e.g. from its doorway). Only visible from its direct parent.
      if (!targetZone.isEntryRestricted()) {
        return Objects.equals(targetZone.getParentZoneId(), currentZone.getId());
      }
      return false;
    } else {
      if (isCurrentInterior) {
        // Rule: From an interior, citizen can only see the exterior parent if the interior isn't restricted
        if (currentZone.isEntryRestricted()) {
          return false;
        }
        return Objects.equals(currentZone.getParentZoneId(), targetZone.getId());
      } else {
        // From an exterior, citizen can see hierarchically relevant exterior zones
        return isHierarchicallyRelevantExteriors(targetZone, currentZone);
      }
    }
  }

  private boolean isHierarchicallyRelevantExteriors(Zone targetZone, Zone currentZone) {
    // Direct Parent
    if (Objects.equals(currentZone.getParentZoneId(), targetZone.getId())) {
      return true;
    }
    // Direct Children
    if (Objects.equals(targetZone.getParentZoneId(), currentZone.getId())) {
      return true;
    }
    // Siblings
    return Objects.equals(currentZone.getParentZoneId(), targetZone.getParentZoneId());
  }

  private boolean isObserverInside(Zone targetZone, Zone currentZone, Map<String, Zone> allZones) {
    String parentId = currentZone.getParentZoneId();
    while (parentId != null) {
      if (Objects.equals(targetZone.getId(), parentId)) {
        return true;
      }
      Zone parent = allZones.get(parentId);
      parentId = parent != null ? parent.getParentZoneId() : null;
    }
    return false;
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

}
