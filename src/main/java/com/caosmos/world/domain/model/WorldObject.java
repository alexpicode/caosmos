package com.caosmos.world.domain.model;

import com.caosmos.common.domain.model.world.EntityType;
import com.caosmos.common.domain.model.world.NearbyElement;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WorldObject implements WorldElement {

  public WorldObject(
      String id,
      String name,
      String category,
      Vector3 position,
      Set<String> tags,
      String description,
      String parentZoneId,
      String targetZoneId,
      Double radius,
      Double width,
      Double length,
      Double amount
  ) {
    this.id = id;
    this.name = name;
    this.category = category;
    this.description = description;
    this.position = position;
    setTags(tags);
    this.parentZoneId = parentZoneId;
    this.targetZoneId = targetZoneId;
    this.radius = radius;
    this.width = width;
    this.length = length;
    this.amount = amount;
  }

  private String id;
  private String name;
  private String category;
  private String description;
  private Vector3 position;
  private Set<String> tags;
  private Double amount;

  protected synchronized void setTags(Set<String> tags) {
    if (tags == null) {
      this.tags = java.util.Collections.emptySet();
    } else {
      this.tags = tags.stream()
          .filter(t -> t != null)
          .map(String::toLowerCase)
          .collect(java.util.stream.Collectors.toSet());
    }
  }

  public synchronized void addTag(String tag) {
    Set<String> updated = new HashSet<>(this.tags);
    updated.add(tag.toLowerCase());
    this.tags = Collections.unmodifiableSet(updated);
  }

  public synchronized void removeTag(String tag) {
    Set<String> updated = new HashSet<>(this.tags);
    updated.remove(tag.toLowerCase());
    this.tags = Collections.unmodifiableSet(updated);
  }

  /**
   * Atomically replaces the name and all tags of this object. Both changes happen under the same lock to prevent
   * observers seeing an intermediate state.
   */
  public synchronized void transform(String newName, Set<String> newTags) {
    this.name = newName;
    setTags(newTags);
    this.description = null;
  }

  public synchronized void setDescription(String description) {
    this.description = description;
  }

  private String parentZoneId;
  private String targetZoneId;

  // Collision properties
  private Double radius; // Circular collision
  private Double width;  // Rectangular collision (AABB)
  private Double length;

  @Override
  public EntityType getType() {
    return EntityType.OBJECT;
  }

  @Override
  public NearbyElement toNearbyElement(double distance, String direction) {
    return new NearbyElement(
        id,
        name,
        category,
        EntityType.OBJECT,
        null, // WorldObject doesn't have a zoneType (Zones do)
        Math.round(distance * 100.0) / 100.0,
        direction,
        tags,
        getZoneId(),
        null, null, null
    );
  }

  @Override
  public String getZoneId() {
    return parentZoneId;
  }

  @Override
  public boolean contains(Vector3 point) {
    return (radius != null && radius > 0) || (width != null && length != null && width > 0 && length > 0);
  }

  @Override
  public double distanceTo2D(Vector3 point) {
    // 1. Circular collision
    if (radius != null && radius > 0) {
      return Math.max(0, position.distanceTo2D(point) - radius);
    }

    // 2. Rectangular collision (AABB)
    if (width != null && length != null && width > 0 && length > 0) {
      double dx = Math.max(0, Math.abs(point.x() - position.x()) - width / 2.0);
      double dz = Math.max(0, Math.abs(point.z() - position.z()) - length / 2.0);
      return Math.sqrt(dx * dx + dz * dz);
    }

    // Default: Point-based
    return position.distanceTo2D(point);
  }

  public boolean intersects(Vector3 point) {
    if (point == null) {
      return false;
    }
    return distanceTo2D(point) < 0.1;
  }
}
