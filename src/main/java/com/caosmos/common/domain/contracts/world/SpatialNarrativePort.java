package com.caosmos.common.domain.contracts.world;

import com.caosmos.common.domain.model.world.NamedLocation;
import com.caosmos.common.domain.model.world.Vector3;
import java.util.Optional;

/**
 * Interface providing spatial context translated into semantic narrative labels.
 */
public interface SpatialNarrativePort {

  /**
   * Calculates the cardinal direction from one point to another.
   */
  String getCardinalDirection(Vector3 from, Vector3 to);

  /**
   * Maps a physical distance into a semantic label (e.g., "Near", "Distant").
   */
  String getSemanticDistance(double distance);

  /**
   * Finds the nearest city to the given position.
   */
  Optional<NamedLocation> findNearestCity(Vector3 position);
}
