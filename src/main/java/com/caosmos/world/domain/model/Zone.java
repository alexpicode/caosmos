package com.caosmos.world.domain.model;

import com.caosmos.common.domain.model.world.Vector3;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Zone {

  private String id;
  private String name;
  private String parentId; // For nested zones
  private String type; // e.g. INTERIOR, EXTERIOR
  private Set<String> physicalTags = new HashSet<>();
  private Set<String> contextualTags = new HashSet<>();

  private boolean isEntryRestricted; // Need specific entry
  private Vector3 center;
  private double width;
  private double length;

  public Zone(
      String id,
      String name,
      String parentId,
      String type,
      Set<String> physicalTags,
      Set<String> contextualTags,
      boolean isEntryRestricted,
      Vector3 center,
      double width,
      double length
  ) {
    this.id = id;
    this.name = name;
    this.parentId = parentId;
    this.type = type;
    setPhysicalTags(physicalTags);
    setContextualTags(contextualTags);
    this.isEntryRestricted = isEntryRestricted;
    this.center = center;
    this.width = width;
    this.length = length;
  }

  public void setPhysicalTags(Set<String> physicalTags) {
    this.physicalTags = normalizeTags(physicalTags);
  }

  public void setContextualTags(Set<String> contextualTags) {
    this.contextualTags = normalizeTags(contextualTags);
  }

  public boolean contains(Vector3 position) {
    double halfWidth = width / 2.0;
    double halfLength = length / 2.0;
    return position.x() >= center.x() - halfWidth && position.x() <= center.x() + halfWidth &&
        position.z() >= center.z() - halfLength && position.z() <= center.z() + halfLength;
  }

  public Set<String> getEffectiveTags(Map<String, Zone> allZones) {
    Set<String> effectiveTags = getTags();
    if (parentId != null && allZones.containsKey(parentId)) {
      Zone parent = allZones.get(parentId);
      if ("INTERIOR".equals(this.type)) {
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
    if (parentId != null && allZones.containsKey(parentId)) {
      effective.addAll(allZones.get(parentId).getEffectiveContextualTags(allZones));
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
    if (parentId == null || !allZones.containsKey(parentId)) {
      return 0;
    }
    return 1 + allZones.get(parentId).getHierarchyDepth(allZones);
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
