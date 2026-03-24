package com.caosmos.world.domain.model;

import com.caosmos.common.domain.model.world.Vector3;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Zone {

  private String id;
  private String name;
  private String parentId; // For nested zones
  private String type; // e.g. INTERIOR, EXTERIOR
  private Set<String> tags = new HashSet<>();
  private boolean isEntryRestricted; // Need specific entry
  private Vector3 center;
  private double width;
  private double length;

  public boolean contains(Vector3 position) {
    double halfWidth = width / 2.0;
    double halfLength = length / 2.0;
    return position.x() >= center.x() - halfWidth && position.x() <= center.x() + halfWidth &&
        position.z() >= center.z() - halfLength && position.z() <= center.z() + halfLength;
  }

  public Set<String> getEffectiveTags(Map<String, Zone> allZones) {
    Set<String> effectiveTags = new HashSet<>(this.tags != null ? this.tags : Set.of());
    if (parentId != null && allZones.containsKey(parentId)) {
      effectiveTags.addAll(allZones.get(parentId).getEffectiveTags(allZones));
    }
    return effectiveTags;
  }

  public int getHierarchyDepth(Map<String, Zone> allZones) {
    if (parentId == null || !allZones.containsKey(parentId)) {
      return 0;
    }
    return 1 + allZones.get(parentId).getHierarchyDepth(allZones);
  }
}
