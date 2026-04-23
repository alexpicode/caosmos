package com.caosmos.world.domain.model;

import com.caosmos.common.domain.model.world.EntityType;
import com.caosmos.common.domain.model.world.NearbyElement;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldElement;
import com.caosmos.common.domain.model.world.ZoneType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Zone implements WorldElement {

  private String id;
  private String name;
  private String parentZoneId; // For nested zones
  @JsonProperty("type")
  private ZoneType zoneType; // e.g. INTERIOR, EXTERIOR
  private Set<String> physicalTags = new HashSet<>();
  private Set<String> contextualTags = new HashSet<>();

  @JsonProperty("isEntryRestricted")
  private boolean isEntryRestricted; // Need specific entry
  private String category;
  private Vector3 center;
  private double width;
  private double length;

  public Zone(
      String id,
      String name,
      String parentZoneId,
      ZoneType zoneType,
      String category,
      Set<String> physicalTags,
      Set<String> contextualTags,
      boolean isEntryRestricted,
      Vector3 center,
      double width,
      double length
  ) {
    this.id = id;
    this.name = name;
    this.parentZoneId = parentZoneId;
    this.zoneType = zoneType;
    this.category = category;
    setPhysicalTags(physicalTags);
    setContextualTags(contextualTags);
    this.isEntryRestricted = isEntryRestricted;
    this.center = center;
    this.width = width;
    this.length = length;
  }

  @JsonIgnore
  @Override
  public EntityType getType() {
    return EntityType.ZONE;
  }

  @Override
  public Vector3 getPosition() {
    return center;
  }

  @Override
  public String getZoneId() {
    return id;
  }

  @Override
  public String getParentZoneId() {
    return parentZoneId;
  }

  @Override
  public NearbyElement toNearbyElement(double distance, String direction) {
    return new NearbyElement(
        id,
        name,
        category,
        EntityType.ZONE,
        zoneType,
        Math.round(distance * 100.0) / 100.0,
        direction,
        getTags(),
        null, null, null
    );
  }

  @Override
  public boolean contains(Vector3 point) {
    double halfWidth = width / 2.0;
    double halfLength = length / 2.0;
    return point.x() >= center.x() - halfWidth && point.x() <= center.x() + halfWidth &&
        point.z() >= center.z() - halfLength && point.z() <= center.z() + halfLength;
  }

  /**
   * Calculates the distance to the zone's perimeter. If the point is inside, the distance is 0.
   */
  @Override
  public double distanceTo2D(Vector3 point) {
    double dx = Math.max(0, Math.abs(point.x() - center.x()) - width / 2.0);
    double dz = Math.max(0, Math.abs(point.z() - center.z()) - length / 2.0);
    return Math.sqrt(dx * dx + dz * dz);
  }

  public synchronized void setPhysicalTags(Set<String> physicalTags) {
    this.physicalTags = normalizeTags(physicalTags);
  }

  public synchronized void setContextualTags(Set<String> contextualTags) {
    this.contextualTags = normalizeTags(contextualTags);
  }

  public synchronized void addTag(String tag) {
    if (tag == null) {
      return;
    }
    this.contextualTags.add(tag.toLowerCase());
  }

  public synchronized void removeTag(String tag) {
    if (tag == null) {
      return;
    }
    String normalized = tag.toLowerCase();
    this.contextualTags.remove(normalized);
    this.physicalTags.remove(normalized);
  }

  public Set<String> getEffectiveTags(Map<String, Zone> allZones) {
    Set<String> effectiveTags = getTags();
    if (parentZoneId != null && allZones.containsKey(parentZoneId)) {
      Zone parent = allZones.get(parentZoneId);
      if (ZoneType.INTERIOR == this.zoneType) {
        // In interiors, we only inherit contextual tags that pass through walls
        effectiveTags.addAll(parent.getEffectiveContextualTags(allZones));
      } else {
        // In exteriors, we inherit everything openly
        effectiveTags.addAll(parent.getEffectiveTags(allZones));
      }
    }
    return effectiveTags;
  }

  public Set<String> getEffectiveContextualTags(Map<String, Zone> allZones) {
    Set<String> effective = new HashSet<>(this.contextualTags != null ? this.contextualTags : Set.of());
    if (parentZoneId != null && allZones.containsKey(parentZoneId)) {
      effective.addAll(allZones.get(parentZoneId).getEffectiveContextualTags(allZones));
    }
    return effective;
  }

  public Set<String> getTags() {
    Set<String> all = new HashSet<>();
    if (physicalTags != null) {
      all.addAll(physicalTags);
    }
    if (contextualTags != null) {
      all.addAll(contextualTags);
    }
    return all;
  }

  public int getHierarchyDepth(Map<String, Zone> allZones) {
    if (parentZoneId == null || !allZones.containsKey(parentZoneId)) {
      return 0;
    }
    return 1 + allZones.get(parentZoneId).getHierarchyDepth(allZones);
  }

  private Set<String> normalizeTags(Set<String> tags) {
    if (tags == null) {
      return new HashSet<>();
    }
    return tags.stream()
        .filter(t -> t != null)
        .map(String::toLowerCase)
        .collect(java.util.stream.Collectors.toSet());
  }
}
