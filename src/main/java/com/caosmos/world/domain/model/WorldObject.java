package com.caosmos.world.domain.model;

import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldEntity;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorldObject implements WorldEntity {

  private String id;
  private String name;
  private Vector3 position;
  private Set<String> tags;
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
