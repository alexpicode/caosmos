package com.caosmos.citizens.domain;

import com.caosmos.citizens.domain.model.perception.ExplorationStatus;
import com.caosmos.citizens.domain.model.perception.RememberedPOI;
import com.caosmos.citizens.domain.model.perception.ZoneMemorySummary;
import com.caosmos.common.domain.model.world.ZoneMetadata;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Persistent exploration state of a specific zone for a citizen.
 */
public class ZoneExplorationState {

  private final String zoneId;
  private final String name;
  private final String zoneType;
  private final String category;

  private int percentage;
  private boolean fullyExplored;
  private final Set<Long> visitedCells = new HashSet<>();
  private final List<RememberedPOI> rememberedPOIs = new ArrayList<>();

  public ZoneExplorationState(ZoneMetadata metadata) {
    this.zoneId = metadata.zoneId();
    this.name = metadata.name();
    this.zoneType = metadata.zoneType();
    this.category = metadata.category();
  }

  public synchronized void markAsFullyExplored() {
    this.percentage = 100;
    this.fullyExplored = true;
  }

  public synchronized void recordCellVisit(long cellId, int totalZoneCells) {
    if (fullyExplored) {
      return;
    }

    visitedCells.add(cellId);
    if (totalZoneCells > 0) {
      this.percentage = Math.min(100, (int) ((double) visitedCells.size() / totalZoneCells * 100));
      if (this.percentage == 100) {
        this.fullyExplored = true;
      }
    }
  }

  public synchronized void registerPOI(RememberedPOI poi) {
    // Avoid duplicates by ID
    rememberedPOIs.removeIf(p -> p.id().equals(poi.id()));
    rememberedPOIs.add(poi);
  }

  public synchronized ExplorationStatus toStatus() {
    return new ExplorationStatus(percentage, fullyExplored);
  }

  public synchronized ZoneMemorySummary toSummary() {
    return new ZoneMemorySummary(zoneId, name, zoneType, category, percentage, fullyExplored);
  }

  public String getZoneId() {
    return zoneId;
  }

  public String getName() {
    return name;
  }

  public String getZoneType() {
    return zoneType;
  }

  public String getCategory() {
    return category;
  }

  public List<RememberedPOI> getRememberedPOIs() {
    return new ArrayList<>(rememberedPOIs);
  }
}
