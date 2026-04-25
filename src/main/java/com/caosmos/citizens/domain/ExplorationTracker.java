package com.caosmos.citizens.domain;

import com.caosmos.citizens.domain.model.perception.RememberedPOI;
import com.caosmos.citizens.domain.model.perception.ZoneMemorySummary;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.ZoneMetadata;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manager for a citizen's exploration persistence.
 */
public class ExplorationTracker {

  private static final double CELL_SIZE = 10.0;

  // zoneId -> Detailed exploration state
  private final Map<String, ZoneExplorationState> exploredZones = new ConcurrentHashMap<>();

  /**
   * Registers entry to a zone. Creates the state if it doesn't exist. If it's INTERIOR and instant coverage is
   * detected, it's marked as explored.
   */
  public void enterZone(ZoneMetadata metadata, boolean instantFullCoverage) {
    ZoneExplorationState state = exploredZones.computeIfAbsent(
        metadata.zoneId(),
        id -> new ZoneExplorationState(metadata)
    );

    if (instantFullCoverage && "INTERIOR".equals(metadata.zoneType())) {
      state.markAsFullyExplored();
    }
  }

  /**
   * Registers a zone as known (glimpsed/seen) without physical entry. Creates a 0% exploration entry only if the zone
   * is not already tracked.
   */
  public void registerZoneAsKnown(ZoneMetadata metadata) {
    exploredZones.putIfAbsent(
        metadata.zoneId(),
        new ZoneExplorationState(metadata)
    );
  }

  /**
   * Updates exploration based on vision sweep. Marks cells covered by the vision radius.
   */
  public void updateExploration(
      String zoneId, Vector3 position, double visionRadius,
      double zoneWidth, double zoneLength
  ) {
    ZoneExplorationState state = exploredZones.get(zoneId);
    if (state == null || state.toStatus().fullyExplored()) {
      return;
    }

    int totalCells = (int) (Math.ceil(zoneWidth / CELL_SIZE) * Math.ceil(zoneLength / CELL_SIZE));

    // Mark cells within the vision radius
    double minX = position.x() - visionRadius;
    double minZ = position.z() - visionRadius;
    double maxX = position.x() + visionRadius;
    double maxZ = position.z() + visionRadius;

    for (double x = minX; x <= maxX; x += CELL_SIZE) {
      for (double z = minZ; z <= maxZ; z += CELL_SIZE) {
        // Check if the point is within the actual vision circle
        if (position.distanceTo2D(new Vector3(x, position.y(), z)) <= visionRadius) {
          state.recordCellVisit(getCellKey(x, z), totalCells);
        }
      }
    }
  }

  /**
   * Registers a discovered POI in a zone.
   */
  public void registerPOI(String zoneId, RememberedPOI poi) {
    ZoneExplorationState state = exploredZones.get(zoneId);
    if (state != null) {
      state.registerPOI(poi);
    }
  }

  /**
   * Returns a summary of all known zones.
   */
  public List<ZoneMemorySummary> getKnownZonesSummary() {
    return exploredZones.values().stream()
        .map(ZoneExplorationState::toSummary)
        .collect(Collectors.toList());
  }

  /**
   * Gets the detailed state of a zone.
   */
  public Optional<ZoneExplorationState> getExplorationState(String zoneId) {
    return Optional.ofNullable(exploredZones.get(zoneId));
  }

  /**
   * Verifies if a zone has been visited.
   */
  public boolean isZoneVisited(String zoneId) {
    return exploredZones.containsKey(zoneId);
  }

  public java.util.Set<String> getVisitedZoneIds() {
    return exploredZones.keySet();
  }

  private long getCellKey(double x, double z) {
    long xi = (long) Math.floor(x / CELL_SIZE);
    long zi = (long) Math.floor(z / CELL_SIZE);
    return (xi << 32) | (zi & 0xFFFFFFFFL);
  }
}
