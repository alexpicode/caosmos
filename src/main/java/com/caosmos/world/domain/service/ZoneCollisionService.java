package com.caosmos.world.domain.service;

import com.caosmos.common.domain.model.world.CollisionResult;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.ZoneType;
import com.caosmos.world.domain.model.Zone;
import org.springframework.stereotype.Service;

@Service
public class ZoneCollisionService {

  /**
   * Validates a movement from 'from' to 'to' within a zone. If the zone is INTERIOR, restricts movement to the zone
   * boundaries. If the zone is EXTERIOR or null, allows free movement.
   */
  public CollisionResult validateMovement(Vector3 from, Vector3 to, Zone zone) {
    if (zone == null || zone.getZoneType() != ZoneType.INTERIOR) {
      return new CollisionResult(to, false);
    }

    double halfWidth = zone.getWidth() / 2.0;
    double halfLength = zone.getLength() / 2.0;
    double minX = zone.getCenter().x() - halfWidth;
    double maxX = zone.getCenter().x() + halfWidth;
    double minZ = zone.getCenter().z() - halfLength;
    double maxZ = zone.getCenter().z() + halfLength;

    // If target is already inside, no collision
    if (to.x() >= minX && to.x() <= maxX && to.z() >= minZ && to.z() <= maxZ) {
      return new CollisionResult(to, false);
    }

    // Segment clipping algorithm (simplified Liang-Barsky for XZ plane)
    double dx = to.x() - from.x();
    double dz = to.z() - from.z();

    double tmin = 0.0;
    double tmax = 1.0;

    // Validate against X boundaries
    if (dx != 0.0) {
      double t1 = (minX - from.x()) / dx;
      double t2 = (maxX - from.x()) / dx;
      tmin = Math.max(tmin, Math.min(t1, t2));
      tmax = Math.min(tmax, Math.max(t1, t2));
    } else if (from.x() < minX || from.x() > maxX) {
      return new CollisionResult(from, true); // Should not happen if 'from' is valid
    }

    // Validate against Z boundaries
    if (dz != 0.0) {
      double t1 = (minZ - from.z()) / dz;
      double t2 = (maxZ - from.z()) / dz;
      tmin = Math.max(tmin, Math.min(t1, t2));
      tmax = Math.min(tmax, Math.max(t1, t2));
    } else if (from.z() < minZ || from.z() > maxZ) {
      return new CollisionResult(from, true);
    }

    if (tmax < tmin) {
      // No valid intersection or segment outside (should not occur if 'from' is inside)
      return new CollisionResult(from, true);
    }

    // Get exact boundary intersection point
    Vector3 intersection = from.lerp(to, tmax);

    // Clamp coordinates strictly within the box to prevent float inaccuracies
    // and ensure the entity stays slightly inside the bounds.
    double epsilon = 0.01;
    double clampedX = Math.max(minX + epsilon, Math.min(maxX - epsilon, intersection.x()));
    double clampedZ = Math.max(minZ + epsilon, Math.min(maxZ - epsilon, intersection.z()));

    Vector3 clampedPos = new Vector3(clampedX, intersection.y(), clampedZ);
    return new CollisionResult(clampedPos, true);
  }
}
