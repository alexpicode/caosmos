package com.caosmos.citizens.application.handler;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.perception.CognitiveAnchor;
import com.caosmos.citizens.domain.model.perception.ExplorationStatus;
import com.caosmos.citizens.domain.model.perception.MentalMap;
import com.caosmos.citizens.domain.model.perception.ZoneMemory;
import com.caosmos.citizens.domain.model.perception.ZoneMemorySummary;
import com.caosmos.common.domain.contracts.WorldPort;
import com.caosmos.common.domain.contracts.world.SpatialNarrativePort;
import com.caosmos.common.domain.model.world.Vector3;
import com.caosmos.common.domain.model.world.WorldPerception;
import com.caosmos.common.domain.model.world.ZoneMetadata;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Specialized component for calculating a citizen's mental map. Translates physical world data (coordinates, zones)
 * into narrative cognitive anchors.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CitizenMentalMapper {

  private final SpatialNarrativePort spatialNarrativePort;
  private final SpatialContextFilter spatialContextFilter;
  private final WorldPort worldPort;

  /**
   * Calculates the mental map for a citizen based on their current position and profile.
   */
  public MentalMap calculate(Citizen citizen, Vector3 currentPosition, WorldPerception perception) {
    // 1. Home Anchor
    CognitiveAnchor homeAnchor = null;
    if (citizen.getCitizenProfile().baseLocation() != null) {
      Vector3 homePos = new Vector3(
          citizen.getCitizenProfile().baseLocation().x(),
          citizen.getCitizenProfile().baseLocation().y(),
          citizen.getCitizenProfile().baseLocation().z()
      );
      double distance = currentPosition.distanceTo2D(homePos);
      homeAnchor = new CognitiveAnchor(
          "Home",
          Math.round(distance * 100.0) / 100.0,
          spatialNarrativePort.getSemanticDistance(distance),
          spatialNarrativePort.getCardinalDirection(currentPosition, homePos)
      );
    }

    // 2. Nearest City Anchor
    CognitiveAnchor cityAnchor = null;
    var nearestCityOpt = spatialNarrativePort.findNearestCity(currentPosition);
    if (nearestCityOpt.isPresent()) {
      var city = nearestCityOpt.get();
      double distance = currentPosition.distanceTo2D(city.center());
      cityAnchor = new CognitiveAnchor(
          city.name(),
          Math.round(distance * 100.0) / 100.0,
          spatialNarrativePort.getSemanticDistance(distance),
          spatialNarrativePort.getCardinalDirection(currentPosition, city.center())
      );
    }

    // 3. Current Zone Memory
    ZoneMemory currentMemory = null;
    String currentZoneId = citizen.getCurrentState().getCurrentZoneId();
    if (currentZoneId != null) {
      currentMemory = citizen.exploration().getExplorationState(currentZoneId)
          .map(state -> new ZoneMemory(
              state.getZoneId(),
              state.getName(),
              state.getZoneType(),
              state.getCategory(),
              state.toStatus(),
              state.getRememberedPOIs()
          ))
          .orElseGet(() -> {
            // Fallback: build from WorldPort if not in tracker yet
            ZoneMetadata meta = worldPort.getZoneMetadata(currentZoneId).orElse(null);
            if (meta == null) {
              return null;
            }
            return new ZoneMemory(
                meta.zoneId(), meta.name(), meta.zoneType(), meta.category(),
                new ExplorationStatus(0, false), List.of()
            );
          });
    }

    // 4. Filtered Known Zones
    List<ZoneMemorySummary> relevantZones = spatialContextFilter.filterRelevantZones(
        citizen.exploration(), perception, citizen.biology());

    return new MentalMap(homeAnchor, cityAnchor, currentMemory, relevantZones);
  }
}
