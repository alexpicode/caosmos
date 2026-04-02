package com.caosmos.citizens.application.handler;

import com.caosmos.citizens.domain.Citizen;
import com.caosmos.citizens.domain.model.perception.CognitiveAnchor;
import com.caosmos.citizens.domain.model.perception.MentalMap;
import com.caosmos.common.domain.contracts.world.SpatialNarrativePort;
import com.caosmos.common.domain.model.world.Vector3;
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

  /**
   * Calculates the mental map for a citizen based on their current position and profile.
   */
  public MentalMap calculate(Citizen citizen, Vector3 currentPosition) {
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

    return new MentalMap(homeAnchor, cityAnchor);
  }
}
