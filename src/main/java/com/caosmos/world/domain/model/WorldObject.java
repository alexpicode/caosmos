package com.caosmos.world.domain.model;

import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldEntity;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WorldObject implements WorldEntity {

  public WorldObject(
      String id,
      String name,
      Vector3 position,
      Set<String> tags,
      String parentZoneId,
      Double radius,
      Double width,
      Double length
  ) {
    this.id = id;
    this.name = name;
    this.position = position;
    setTags(tags);
    this.parentZoneId = parentZoneId;
    this.radius = radius;
    this.width = width;
    this.length = length;
  }

  private String id;
  private String name;
  private Vector3 position;
  private Set<String> tags;

  public void setTags(Set<String> tags) {
    if (tags == null) {
      this.tags = java.util.Collections.emptySet();
    } else {
      this.tags = tags.stream()
          .filter(t -> t != null)
          .map(String::toLowerCase)
          .collect(java.util.stream.Collectors.toSet());
    }
  }

  private String parentZoneId;

  // Collision properties
  private Double radius; // Circular collision
  private Double width;  // Rectangular collision (AABB)
  private Double length;

  @Override
  public String getType() {
    return "OBJECT";
  }

  @Override
  public String getDisplayName() {
    return name;
  }

  @Override
  public String getZoneId() {
    return parentZoneId;
  }

  public boolean intersects(Vector3 point) {
    if (point == null) {
      return false;
    }

    // 1. Circular collision check
    if (radius != null && radius > 0) {
      return position.distanceTo2D(point) <= radius;
    }

    // 2. Rectangular collision check (AABB)
    if (width != null && length != null && width > 0 && length > 0) {
      double halfWidth = width / 2.0;
      double halfLength = length / 2.0;
      return point.x() >= position.x() - halfWidth && point.x() <= position.x() + halfWidth &&
          point.z() >= position.z() - halfLength && point.z() <= position.z() + halfLength;
    }

    // Default: Point-based (no collision volume)
    return position.distanceTo2D(point) < 0.1;
  }
}
