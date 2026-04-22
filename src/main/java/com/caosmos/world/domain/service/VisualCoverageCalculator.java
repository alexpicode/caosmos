package com.caosmos.world.domain.service;

import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.ZoneType;
import com.caosmos.world.domain.model.Zone;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VisualCoverageCalculator {

  @Value("${caosmos.world.max-instant-coverage-area:100.0}")
  private double maxInstantCoverageArea;

  /**
   * Calculates if the citizen at 'observerPos' can see the entire INTERIOR zone. If the area is very small
   * (configurable), assumes total coverage. Otherwise, verifies if all 4 corners are within the vision radius.
   */
  public boolean canSeeEntireZone(Vector3 observerPos, Zone zone, double visionRadius) {
    if (zone == null || zone.getZoneType() != ZoneType.INTERIOR) {
      return false;
    }

    // 1. Check for small area (configurable in application.yml)
    double area = zone.getWidth() * zone.getLength();
    if (area <= maxInstantCoverageArea) {
      return true;
    }

    // 2. Check by corner visibility
    double halfWidth = zone.getWidth() / 2.0;
    double halfLength = zone.getLength() / 2.0;
    Vector3 center = zone.getCenter();

    List<Vector3> corners = List.of(
        new Vector3(center.x() - halfWidth, center.y(), center.z() - halfLength),
        new Vector3(center.x() + halfWidth, center.y(), center.z() - halfLength),
        new Vector3(center.x() - halfWidth, center.y(), center.z() + halfLength),
        new Vector3(center.x() + halfWidth, center.y(), center.z() + halfLength)
    );

    for (Vector3 corner : corners) {
      if (observerPos.distanceTo2D(corner) > visionRadius) {
        return false;
      }
    }

    return true;
  }
}
