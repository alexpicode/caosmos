package com.caosmos.common.domain.model.world;

import java.util.Collections;
import java.util.Set;

public interface WorldElement {

  String getId();

  EntityType getType();

  Vector3 getPosition();

  String getName();

  String getZoneId();

  String getCategory();

  default Set<String> getTags() {
    return Collections.emptySet();
  }

  default String getDescription() {
    return null;
  }

  NearbyElement toNearbyElement(double distance, String direction);

  /**
   * Returns true if the given point is within the physical boundaries of this element.
   */
  default boolean contains(Vector3 point) {
    return false;
  }

  /**
   * Returns the 2D distance from the given point to the nearest physical boundary of this element. Default
   * implementation returns the distance to the center position.
   */
  default double distanceTo2D(Vector3 point) {
    return point.distanceTo2D(getPosition());
  }

  /**
   * Returns true if this element can only be perceived from within its own zone.
   */
  default boolean isLimitedToZone() {
    return false;
  }
}
