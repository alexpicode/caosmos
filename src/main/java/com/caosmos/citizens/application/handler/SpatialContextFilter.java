package com.caosmos.citizens.application.handler;

import com.caosmos.citizens.domain.BiologyManager;
import com.caosmos.citizens.domain.ExplorationTracker;
import com.caosmos.citizens.domain.model.perception.ZoneMemorySummary;
import com.caosmos.common.domain.model.world.EntityType;
import com.caosmos.common.domain.model.world.NearbyElement;
import com.caosmos.common.domain.model.world.WorldPerception;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Filters known zones to inject only the relevant ones into the LLM context.
 */
@Component
public class SpatialContextFilter {

  private static final int MAX_HISTORICAL_ZONES = 5;

  /**
   * Filters known zones based on situational relevance.
   */
  public List<ZoneMemorySummary> filterRelevantZones(
      ExplorationTracker tracker,
      WorldPerception currentPerception,
      BiologyManager biology
  ) {

    List<ZoneMemorySummary> allSummaries = tracker.getKnownZonesSummary();

    // Criterion 1: Zones currently in perception (adjacent)
    List<String> visibleZoneIds = currentPerception.nearbyElements().stream()
        .filter(e -> EntityType.ZONE == e.type())
        .map(NearbyElement::id)
        .collect(Collectors.toList());

    // Criterion 2: Zones with POIs matching needs (e.g., Food if hungry)
    // (Simplified for this phase: prioritize most recently visited that are not 100% explored)

    return allSummaries.stream()
        .filter(z -> !visibleZoneIds.contains(z.zoneId())) // Avoid duplicating those already seen
        .sorted(Comparator.comparing(ZoneMemorySummary::fullyExplored)
            .thenComparing(Comparator.comparing(ZoneMemorySummary::explorationPercentage).reversed()))
        .limit(MAX_HISTORICAL_ZONES)
        .collect(Collectors.toList());
  }
}
